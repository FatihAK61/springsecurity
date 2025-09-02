package com.authservice.springsecurity.utils;

public interface IEmailService {

    void sendVerificationEmail(String to, String subject, String text) throws MessagingException;

}
