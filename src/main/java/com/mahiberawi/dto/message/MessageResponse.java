package com.mahiberawi.dto.message;

import com.mahiberawi.entity.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MessageResponse {
    private String id;
    private String content;
    private MessageType type;
    private String senderId;
    private String senderName;
    private String senderProfilePicture;
    private String recipientId;
    private String recipientName;
    private String groupId;
    private String groupName;
    private String eventId;
    private String eventTitle;
    private String parentMessageId;
    private List<MessageResponse> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isRead;
} 