package com.mahiberawi.dto.group;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupInvitationRequest {
    
    @NotBlank(message = "Group ID is required")
    private String groupId;
    
    // For email invitations
    @Email(message = "Invalid email format")
    private String email;
    
    // For SMS invitations
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phone;
    
    // For code generation
    private Boolean generateCode = false;
    
    // Expiration time in hours (default 24)
    @NotNull(message = "Expiration hours is required")
    private Integer expirationHours = 24;
    
    // Custom message for the invitation
    private String message;
    
    // Validate that at least one invitation method is specified
    public boolean isValid() {
        return (email != null && !email.trim().isEmpty()) ||
               (phone != null && !phone.trim().isEmpty()) ||
               (generateCode != null && generateCode);
    }
} 