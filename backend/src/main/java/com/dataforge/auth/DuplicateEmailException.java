package com.dataforge.auth;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A user already exists for email: " + email);
    }
}
