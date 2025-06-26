package com.mahiberawi.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupPrivacy {
    PRIVATE,
    PUBLIC;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static GroupPrivacy fromString(String value) {
        if (value == null) {
            return PRIVATE; // Default to private
        }
        
        String normalized = value.toUpperCase();
        
        try {
            return GroupPrivacy.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Handle common variations
            switch (normalized) {
                case "PUBLIC":
                case "OPEN":
                    return PUBLIC;
                case "PRIVATE":
                case "CLOSED":
                default:
                    return PRIVATE;
            }
        }
    }
} 