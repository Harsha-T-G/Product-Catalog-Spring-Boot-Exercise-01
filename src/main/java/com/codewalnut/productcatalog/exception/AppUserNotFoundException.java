package com.codewalnut.productcatalog.exception;

public class AppUserNotFoundException extends RuntimeException {

    public AppUserNotFoundException(String username) {
        super("User not found: " + username);
    }
}
