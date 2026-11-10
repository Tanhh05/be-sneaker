package com.sneakerstore.common.validation;

public final class ValidationPatterns {
    private ValidationPatterns() {
    }

    public static final String PHONE_REGEX = "^[0-9+\\-()\\s]{8,20}$";
    public static final String CODE_REGEX = "^[A-Z0-9\\-_]{2,30}$";
}

