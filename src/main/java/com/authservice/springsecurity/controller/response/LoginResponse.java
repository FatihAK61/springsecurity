package com.authservice.springsecurity.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class LoginResponse {

    private String token;

    private long expiresIn;

}
