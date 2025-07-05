package com.mahiberawi.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReactionRequest {
    @NotBlank(message = "Reaction type is required")
    @Pattern(regexp = "^(like|love|laugh|wow|sad|angry)$", 
             message = "Reaction type must be one of: like, love, laugh, wow, sad, angry")
    private String reactionType;
} 