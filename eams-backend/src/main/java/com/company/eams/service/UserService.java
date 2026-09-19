package com.company.eams.service;

import com.company.eams.dto.request.UserCreateAdminRequest;
import com.company.eams.dto.request.UserRoleUpdateRequest;
import com.company.eams.dto.response.PageResponse;
import com.company.eams.dto.response.UserResponseDto;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponseDto> getAllUsers(Pageable pageable, String search);

    UserResponseDto createUser(UserCreateAdminRequest request);

    UserResponseDto updateUserRole(Long userId, UserRoleUpdateRequest request);

    void deactivateUser(Long userId);
}
