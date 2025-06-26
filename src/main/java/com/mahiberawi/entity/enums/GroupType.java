package com.mahiberawi.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GroupType {
    EDIR,
    CHURCH,
    SAVING_CLUB,
    FOOTBALL_TEAM,
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
                case "FOOTBALL_TEAM":
                case "FOOTBALLTEAM":
                case "SPORTS_TEAM":
                case "SPORTSTEAM":
                    return FOOTBALL_TEAM;
                case "SAVING_CLUB":
                case "SAVINGCLUB":
                case "SAVINGS_CLUB":
                case "SAVINGSCLUB":
                    return SAVING_CLUB;
                case "EDIR":
                case "IDIR":
                    return EDIR;
                case "CHURCH":
                case "CHURCH_GROUP":
                    return CHURCH;
                default:
                    return OTHER;
            }
        }
    }
} 