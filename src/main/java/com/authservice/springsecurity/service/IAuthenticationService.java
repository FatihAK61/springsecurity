package com.authservice.springsecurity.service;

import com.authservice.springsecurity.controller.response.LoginResponse;
import com.authservice.springsecurity.dto.LoginUserDto;
import com.authservice.springsecurity.dto.RegisterUserDto;
import com.authservice.springsecurity.entity.User;

public interface IAuthenticationService {

    LoginResponse login(LoginUserDto loginUserDto);

    void logout(String token);

    User signup(RegisterUserDto registerUserDto);

    void verifyUser(String email, String verificationCode);

    void resendVerificationCode(String email);

    void requestPasswordReset(String email);

    void resetPassword(String email, String verificationCode, String newPassword);

}
