package com.mahiberawi.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupType {
    EQUB,
    EDIR,
    SPIRITUAL,
    EDUCATIONAL,
    SPORT,
    OTHER;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase().replace('_', ' ');
    }

    @JsonCreator
    public static GroupType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        // Handle various input formats
        String normalized = value.toUpperCase().replace(' ', '_');
        
        try {
            return GroupType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            // Handle common variations
            switch (normalized) {
                case "EQUB":
                case "EQUIB":
                    return EQUB;
                case "EDIR":
                case "IDIR":
                    return EDIR;
                case "SPIRITUAL":
                case "SPIRITUAL_GROUP":
                    return SPIRITUAL;
                case "EDUCATIONAL":
                case "EDUCATION":
                case "EDUCATION_GROUP":
                    return EDUCATIONAL;
                case "SPORT":
                case "SPORTS":
                case "SPORTS_GROUP":
                    return SPORT;
                default:
                    return OTHER;
            }
        }
    }
} 