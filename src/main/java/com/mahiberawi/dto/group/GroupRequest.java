package com.mahiberawi.dto.group;

import com.mahiberawi.entity.enums.GroupType;
import com.mahiberawi.entity.enums.GroupPrivacy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank(message = "Group name is required")
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Group type is required")
    private GroupType type;

    private GroupPrivacy privacy = GroupPrivacy.PRIVATE;

    private String profilePicture;

    // Group settings
    private GroupSettings settings;

    @Data
    public static class GroupSettings {
        private Boolean allowEventCreation = true;
        private Boolean allowMemberInvites = true;
        private Boolean allowMessagePosting = true;
        private Boolean paymentRequired = false;
        private Boolean requireApproval = false;
        private Double monthlyDues;
    }
} 