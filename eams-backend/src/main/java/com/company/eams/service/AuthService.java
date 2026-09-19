package com.company.eams.service;

import com.company.eams.dto.request.ForgotPasswordVerifyRequest;
import com.company.eams.dto.request.LoginRequest;
import com.company.eams.dto.request.ResetPasswordRequest;
import com.company.eams.dto.response.AuthResponse;
import com.company.eams.dto.response.ForgotPasswordVerifyResponse;
import com.company.eams.dto.response.UserSummaryDto;
import com.company.eams.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request, HttpServletResponse response);

    AuthResponse refreshToken(String rawRefreshToken, HttpServletResponse response);

    void logout(String rawRefreshToken, HttpServletResponse response);

    UserSummaryDto getCurrentUser(UserPrincipal userPrincipal);

    ForgotPasswordVerifyResponse verifyForPasswordReset(ForgotPasswordVerifyRequest request);

    void resetPassword(ResetPasswordRequest request);
}

