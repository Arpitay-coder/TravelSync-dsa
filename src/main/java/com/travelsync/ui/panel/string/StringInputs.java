package com.travelsync.ui.panel.string;

import com.travelsync.util.ValidationException;

/** Shared validation helpers of the string panels. */
final class StringInputs {

    private StringInputs() { }

    static String requireText(String value, String what) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Please enter the " + what + " first.");
        }
        return value;
    }
}
