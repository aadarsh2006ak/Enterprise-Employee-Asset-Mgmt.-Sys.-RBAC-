package com.company.eams.service;

import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.dto.response.UserSummaryDto;
import com.company.eams.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String rawRefreshToken, HttpServletResponse response);

    void logout(String rawRefreshToken, HttpServletResponse response);

    UserSummaryDto getCurrentUser(UserPrincipal userPrincipal);
}
