package com.mahiberawi.dto.message;

import com.mahiberawi.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {
    @NotBlank(message = "Content is required")
    private String content;

    @NotNull(message = "Message type is required")
    private MessageType type;

    private String recipientId;
    private String groupId;
    private String eventId;
    private String parentMessageId;
} 