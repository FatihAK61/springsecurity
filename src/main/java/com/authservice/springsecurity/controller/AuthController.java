package com.authservice.springsecurity.controller;

import com.authservice.springsecurity.controller.response.ApiResponse;
import com.authservice.springsecurity.controller.response.LoginResponse;
import com.authservice.springsecurity.dto.LoginUserDto;
import com.authservice.springsecurity.dto.RegisterUserDto;
import com.authservice.springsecurity.dto.request.VerifyRequest;
import com.authservice.springsecurity.service.IAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authenticationService;

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDto loginUserDto) {
        LoginResponse loginResponse = authenticationService.login(loginUserDto);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> signup(@RequestBody RegisterUserDto registerUserDto) {
        authenticationService.signup(registerUserDto);
        ApiResponse apiResponse = new ApiResponse("Code sent");
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PutMapping("/verify")
    public ResponseEntity<ApiResponse> verifyUser(@RequestBody VerifyRequest request) {
        authenticationService.verifyUser(request.getEmail(), request.getVerificationCode());
        ApiResponse apiResponse = new ApiResponse("Account verified successfully");
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/resend-code")
    public ResponseEntity<ApiResponse> resendVerificationCode(@RequestParam String email) {
        authenticationService.resendVerificationCode(email);
        ApiResponse apiResponse = new ApiResponse("Verification code resent");
        return ResponseEntity.ok().body(apiResponse);
    }

    @GetMapping("/reset-password-request")
    public ResponseEntity<ApiResponse> requestPasswordReset(@RequestParam String email) {
        authenticationService.requestPasswordReset(email);
        ApiResponse apiResponse = new ApiResponse("Password reset email sent");
        return ResponseEntity.ok().body(apiResponse);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestParam String email, @RequestParam String verificationCode, @RequestParam String newPassword) {
        authenticationService.resetPassword(email, verificationCode, newPassword);
        ApiResponse apiResponse = new ApiResponse("Password reset successful");
        return ResponseEntity.ok().body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse("Invalid token"));
        }
        String token = authorizationHeader.substring(7);
        authenticationService.logout(token);
        return ResponseEntity.ok(new ApiResponse("Logout successful"));
    }

}
