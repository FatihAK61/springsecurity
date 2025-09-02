package com.authservice.springsecurity.service.impl;

import com.authservice.springsecurity.controller.advice.AuthException;
import com.authservice.springsecurity.controller.response.LoginResponse;
import com.authservice.springsecurity.dto.LoginUserDto;
import com.authservice.springsecurity.dto.RegisterUserDto;
import com.authservice.springsecurity.entity.Role;
import com.authservice.springsecurity.entity.User;
import com.authservice.springsecurity.entity.UserRole;
import com.authservice.springsecurity.repository.IRoleRepository;
import com.authservice.springsecurity.repository.IUserRepository;
import com.authservice.springsecurity.security.jwt.CustomUserDetails;
import com.authservice.springsecurity.security.jwt.JwtService;
import com.authservice.springsecurity.security.jwt.TokenBlacklistService;
import com.authservice.springsecurity.service.IAuthenticationService;
import com.authservice.springsecurity.service.IEmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public LoginResponse login(LoginUserDto loginUserDto) {
        validateLoginCredentials(loginUserDto);

        Authentication authentication = authenticateUser(loginUserDto);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        verifyAccountStatus(customUserDetails);

        String token = jwtService.generateToken(customUserDetails);
        return new LoginResponse(token, jwtService.getExpirationTime());
    }

    @Override
    public void logout(String token) {
        tokenBlacklistService.blacklistToken(token);
        SecurityContextHolder.clearContext();
    }

    @Override
    public User signup(RegisterUserDto registerUserDto) {
        checkIfUserExists(registerUserDto);

        User newUser = createNewUser(registerUserDto);
        String verificationCode = generateVerificationCode();
        newUser.setVerificationCode(verificationCode);
        newUser.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));

        userRepository.save(newUser);
        sendVerificationEmail(newUser);

        return newUser;
    }

    @Override
    public void verifyUser(String email, String verificationCode) {
        User user = getUserByEmailOrThrow(email);
        validateVerificationCode(user, verificationCode);
        enableUserAccount(user);
    }

    @Override
    public void resendVerificationCode(String email) {
        User user = getUserByEmailOrThrow(email);
        if (user.isEnabled()) {
            throw new RuntimeException("Account is already verified");
        }
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
        sendVerificationEmail(user);
        userRepository.save(user);
    }

    @Override
    public void requestPasswordReset(String email) {
        User user = getUserByEmailOrThrow(email);
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        sendVerificationEmail(user);
        userRepository.save(user);
    }

    @Override
    public void resetPassword(String email, String verificationCode, String newPassword) {
        User user = getUserByEmailOrThrow(email);
        validateVerificationCode(user, verificationCode);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    private void validateLoginCredentials(LoginUserDto loginUserDto) {
        if (loginUserDto == null || loginUserDto.getUsername() == null || loginUserDto.getPassword() == null) {
            throw new AuthException.UserNotFoundException("Username and password must not be null");
        }
    }

    private Authentication authenticateUser(LoginUserDto loginUserDto) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(), loginUserDto.getPassword())
            );
        } catch (Exception e) {
            throw new AuthException.UserNotFoundException("Invalid username or password");
        }
    }

    private void verifyAccountStatus(CustomUserDetails customUserDetails) {
        if (!customUserDetails.getUser().isEnabled()) {
            throw new AuthException.AccountNotVerifiedException("Account not verified. Please verify your account.");
        }
    }

    private void checkIfUserExists(RegisterUserDto registerUserDto) {
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            throw new AuthException.UserAlreadyExistsException("Email already registered");
        }

        if (userRepository.findByUsername(registerUserDto.getUsername()).isPresent()) {
            throw new AuthException.UsernameAlreadyExistsException("Username already taken");
        }
    }

    private User createNewUser(RegisterUserDto registerUserDto) {
        User newUser = new User();
        newUser.setEmail(registerUserDto.getEmail());
        newUser.setUsername(registerUserDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        newUser.setEnabled(false);

        Role role = roleRepository.findByName("GUEST")
                .orElseThrow(() -> new RuntimeException("User role not found"));

        UserRole userRole = new UserRole();
        userRole.setRole(role);
        userRole.setUser(newUser);
        userRole.setStatus(true);

        newUser.getUserRoles().add(userRole);
        return newUser;
    }

    private void sendVerificationEmail(User user) {
        String subject = "Please verify your email";
        String htmlMessage = "<html><body>Please use the following code to verify your email: " + user.getVerificationCode() + "</body></html>";
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending verification email.");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(900000) + 100000);
    }

    private User getUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateVerificationCode(User user, String verificationCode) {
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code has expired");
        }
        if (!user.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("Invalid verification code");
        }
    }

    private void enableUserAccount(User user) {
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

}
