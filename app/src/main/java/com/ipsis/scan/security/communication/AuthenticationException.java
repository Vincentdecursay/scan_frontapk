package com.ipsis.scan.security.communication;

/**
 * Created by pobouteau on 9/29/16.
 */

public class AuthenticationException extends Exception {
    private int mCode;

    public AuthenticationException(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
