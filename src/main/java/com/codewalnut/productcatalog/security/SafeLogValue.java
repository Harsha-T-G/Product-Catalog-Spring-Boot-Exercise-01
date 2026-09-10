package com.codewalnut.productcatalog.security;

public final class SafeLogValue {

    private static final int MAX_LENGTH = 200;

    private SafeLogValue() {
    }

    public static String of(String value) {
        if (value == null || value.isBlank()) {
            return "anonymous";
        }
        StringBuilder safe = new StringBuilder(Math.min(value.length(), MAX_LENGTH));
        for (int index = 0; index < value.length() && safe.length() < MAX_LENGTH; index++) {
            char current = value.charAt(index);
            safe.append(Character.isISOControl(current) ? '_' : current);
        }
        return safe.toString();
    }
}
