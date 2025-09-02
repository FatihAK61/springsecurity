package com.authservice.springsecurity.controller.advice;

import com.authservice.springsecurity.controller.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExistsException(AuthException.UserAlreadyExistsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUsernameAlreadyExistsException(AuthException.UsernameAlreadyExistsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFoundException(AuthException.UserNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthException.AccountNotVerifiedException.class)
    public ResponseEntity<ApiResponse> handleAccountNotVerifiedException(AuthException.AccountNotVerifiedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthException.VerificationCodeExpiredException.class)
    public ResponseEntity<ApiResponse> handleVerificationCodeExpiredException(AuthException.VerificationCodeExpiredException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.InvalidVerificationCodeException.class)
    public ResponseEntity<ApiResponse> handleInvalidVerificationCodeException(AuthException.InvalidVerificationCodeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        return buildResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse> buildResponse(String message, HttpStatus status) {
        ApiResponse apiResponse = new ApiResponse(message);
        return new ResponseEntity<>(apiResponse, status);
    }
}
