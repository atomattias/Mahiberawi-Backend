package com.mahiberawi.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupType {
    ROSA, // Changed from EQUB to ROSA (Rotating Savings Association)
    EDIR,
    SPIRITUAL,
    EDUCATIONAL,
    SPORT,
    OTHER;

    @JsonValue
    public String getValue() {
        // Return user-friendly display names
        switch (this) {
            case ROSA:
                return "Rotating Savings Association";
            case EDIR:
                return "Edir";
            case SPIRITUAL:
                return "Spiritual";
            case EDUCATIONAL:
                return "Educational";
            case SPORT:
                return "Sport";
            case OTHER:
                return "Other";
            default:
                return this.name().toLowerCase().replace('_', ' ');
        }
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
                case "ROSA":
                case "ROTATING_SAVINGS_ASSOCIATION":
                case "EQUB": // Keep backward compatibility
                case "EQUIB":
                    return ROSA;
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