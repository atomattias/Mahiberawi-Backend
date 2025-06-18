package com.mahiberawi.service;

import com.mahiberawi.dto.message.MessageRequest;
import com.mahiberawi.dto.message.MessageResponse;
import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.MessageType;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.EventRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.MessageRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final NotificationService notificationService;

    @Transactional
    public MessageResponse sendMessage(MessageRequest request, User sender) {
        Message message = new Message();
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setSender(sender);

        switch (request.getType()) {
            case DIRECT:
                User recipient = userRepository.findById(request.getRecipientId())
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getRecipientId()));
                message.setRecipient(recipient);
                break;
            case GROUP:
                Group group = groupRepository.findById(request.getGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException("Group", "id", request.getGroupId()));
                message.setGroup(group);
                break;
            case EVENT:
                Event event = eventRepository.findById(request.getEventId())
                        .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.getEventId()));
                message.setEvent(event);
                break;
        }

        if (request.getParentMessageId() != null) {
            Message parentMessage = messageRepository.findById(request.getParentMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Message", "id", request.getParentMessageId()));
            message.setParentMessage(parentMessage);
        }

        Message savedMessage = messageRepository.save(message);
        notifyRecipients(savedMessage);
        return mapToMessageResponse(savedMessage);
    }

    public MessageResponse getMessage(String id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
        return mapToMessageResponse(message);
    }

    public List<MessageResponse> getDirectMessages(String userId) {
        List<Message> messages = messageRepository.findByTypeAndRecipientIdOrSenderId(
                MessageType.DIRECT, userId, userId);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getGroupMessages(String groupId) {
        List<Message> messages = messageRepository.findByTypeAndGroupId(MessageType.GROUP, groupId);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getEventMessages(String eventId) {
        List<Message> messages = messageRepository.findByTypeAndEventId(MessageType.EVENT, eventId);
        return messages.stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse markAsRead(String id, User user) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (message.getRecipient() != null && message.getRecipient().getId().equals(user.getId())) {
            message.setRead(true);
            Message updatedMessage = messageRepository.save(message);
            return mapToMessageResponse(updatedMessage);
        }

        throw new IllegalStateException("User is not authorized to mark this message as read");
    }

    @Transactional
    public void deleteMessage(String id, User user) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("User is not authorized to delete this message");
        }

        messageRepository.delete(message);
    }

    private void notifyRecipients(Message message) {
        switch (message.getType()) {
            case DIRECT:
                notificationService.createMessageNotification(message.getRecipient(), message);
                break;
            case GROUP:
                message.getGroup().getMembers().forEach(member ->
                        notificationService.createMessageNotification(member.getUser(), message));
                break;
            case EVENT:
                message.getEvent().getParticipants().forEach(participant ->
                        notificationService.createMessageNotification(participant.getUser(), message));
                break;
        }
    }

    private MessageResponse mapToMessageResponse(Message message) {
        List<MessageResponse> replies = message.getReplies().stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .type(message.getType())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderProfilePicture(message.getSender().getProfilePicture())
                .recipientId(message.getRecipient() != null ? message.getRecipient().getId() : null)
                .recipientName(message.getRecipient() != null ? message.getRecipient().getFullName() : null)
                .groupId(message.getGroup() != null ? message.getGroup().getId() : null)
                .groupName(message.getGroup() != null ? message.getGroup().getName() : null)
                .eventId(message.getEvent() != null ? message.getEvent().getId() : null)
                .eventTitle(message.getEvent() != null ? message.getEvent().getTitle() : null)
                .parentMessageId(message.getParentMessage() != null ? message.getParentMessage().getId() : null)
                .replies(replies)
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .isRead(message.isRead())
                .build();
    }
} 