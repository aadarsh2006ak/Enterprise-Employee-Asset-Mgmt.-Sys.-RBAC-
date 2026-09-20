package com.company.eams.service.impl;

import com.company.eams.dto.request.UserCreateAdminRequest;
import com.company.eams.dto.request.UserRoleUpdateRequest;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.dto.response.UserResponseDto;
import com.company.eams.entity.Role;
import com.company.eams.entity.User;
import com.company.eams.entity.enums.RoleType;
import com.company.eams.exception.ConflictException;
import com.company.eams.exception.ResourceNotFoundException;
import com.company.eams.repository.RefreshTokenRepository;
import com.company.eams.repository.RoleRepository;
import com.company.eams.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = Role.builder()
                .id(1L)
                .name(RoleType.EMPLOYEE)
                .description("Regular employee")
                .build();

        sampleUser = User.builder()
                .id(10L)
                .username("john.doe")
                .email("john.doe@company.com")
                .passwordHash("encodedPassword")
                .role(sampleRole)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Get all users paginated")
    void testGetAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(sampleUser), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(page);

        PageResponse<UserResponseDto> response = userService.getAllUsers(pageable, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("john.doe", response.getContent().get(0).getUsername());
        assertEquals(RoleType.EMPLOYEE, response.getContent().get(0).getRole());
    }

    @Test
    @DisplayName("Create user successfully")
    void testCreateUserSuccess() {
        UserCreateAdminRequest request = new UserCreateAdminRequest();
        request.setUsername("jane.doe");
        request.setEmail("jane.doe@company.com");
        request.setPassword("Password@123");
        request.setRole(RoleType.MANAGER);

        Role managerRole = Role.builder().id(2L).name(RoleType.MANAGER).build();

        when(userRepository.existsByUsernameIgnoreCase("jane.doe")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("jane.doe@company.com")).thenReturn(false);
        when(roleRepository.findByName(RoleType.MANAGER)).thenReturn(Optional.of(managerRole));
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenReturn(
                User.builder()
                        .id(11L)
                        .username("jane.doe")
                        .email("jane.doe@company.com")
                        .role(managerRole)
                        .isActive(true)
                        .build()
        );

        UserResponseDto result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("jane.doe", result.getUsername());
        assertEquals(RoleType.MANAGER, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create user throws ConflictException when username taken")
    void testCreateUserDuplicateUsernameThrows() {
        UserCreateAdminRequest request = new UserCreateAdminRequest();
        request.setUsername("john.doe");
        request.setEmail("john.other@company.com");

        when(userRepository.existsByUsernameIgnoreCase("john.doe")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Update user role and invalidate active refresh tokens")
    void testUpdateUserRoleSuccess() {
        UserRoleUpdateRequest request = new UserRoleUpdateRequest();
        request.setRole(RoleType.ADMIN);

        Role adminRole = Role.builder().id(3L).name(RoleType.ADMIN).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByName(RoleType.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        UserResponseDto response = userService.updateUserRole(10L, request);

        assertNotNull(response);
        assertEquals(RoleType.ADMIN, response.getRole());
        verify(refreshTokenRepository).revokeAllByUserId(10L);
    }

    @Test
    @DisplayName("Deactivate user and revoke refresh tokens")
    void testDeactivateUserSuccess() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));

        userService.deactivateUser(10L);

        assertFalse(sampleUser.getIsActive());
        verify(userRepository).save(sampleUser);
        verify(refreshTokenRepository).revokeAllByUserId(10L);
    }

    @Test
    @DisplayName("Deactivating super admin account throws IllegalArgumentException")
    void testDeactivateSuperAdminThrows() {
        User adminUser = User.builder().id(1L).username("admin").isActive(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> userService.deactivateUser(1L));
        verify(userRepository, never()).save(any(User.class));
    }
}
