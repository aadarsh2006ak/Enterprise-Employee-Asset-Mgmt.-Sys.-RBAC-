package com.company.eams.service.impl;

import com.company.eams.audit.annotation.Auditable;
import com.company.eams.dto.request.UserCreateAdminRequest;
import com.company.eams.dto.request.UserRoleUpdateRequest;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.dto.response.UserResponseDto;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.AuditAction;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.exception.ConflictException;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.RefreshTokenRepository;
import com.company.eams.repository.RoleRepository;
import com.company.eams.repository.UserRepository;
import com.company.eams.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponseDto> getAllUsers(Pageable pageable, String search) {
        Page<User> userPage;
        if (StringUtils.hasText(search)) {
            userPage = userRepository.findAll(pageable); // Can be filtered with search specification
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Page<UserResponseDto> dtoPage = userPage.map(this::mapToDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional
    @Auditable(entityName = "User", action = AuditAction.CREATE)
    public UserResponseDto createUser(UserCreateAdminRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email address '" + request.getEmail() + "' is already registered");
        }

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role '" + request.getRole() + "' not found"));

        User user = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Admin created system user account ID: {}, username: {}", savedUser.getId(), savedUser.getUsername());
        return mapToDto(savedUser);
    }

    @Override
    @Transactional
    @Auditable(entityName = "User", action = AuditAction.UPDATE)
    public UserResponseDto updateUserRole(Long userId, UserRoleUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Role newRole = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new ResourceNotFoundException("Role '" + request.getRole() + "' not found"));

        user.setRole(newRole);
        User updated = userRepository.save(user);

        // Invalidate active refresh tokens on role changes to force re-authentication with new claims
        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("Updated role for user ID: {} to {}", userId, request.getRole());
        return mapToDto(updated);
    }

    @Override
    @Transactional
    @Auditable(entityName = "User", action = AuditAction.DELETE)
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Super admin account cannot be deactivated");
        }

        user.setIsActive(false);
        userRepository.save(user);

        // Revoke active sessions
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("Soft-deleted / deactivated user ID: {}", userId);
    }

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().getName() : RoleType.EMPLOYEE)
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .version(user.getVersion())
                .build();
    }
}
