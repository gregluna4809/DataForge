package com.dataforge.datasets;

public class AuthenticatedUserNotFoundException extends RuntimeException {

    public AuthenticatedUserNotFoundException(String email) {
        super("Authenticated user was not found: " + email);
    }
}
