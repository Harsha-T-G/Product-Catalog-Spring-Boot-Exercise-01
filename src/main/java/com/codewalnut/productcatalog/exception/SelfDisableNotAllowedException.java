package com.codewalnut.productcatalog.exception;

public class SelfDisableNotAllowedException extends RuntimeException {

    public SelfDisableNotAllowedException() {
        super("Administrators cannot disable their own account");
    }
}
