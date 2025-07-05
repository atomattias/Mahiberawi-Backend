package com.mahiberawi.service;

import com.mahiberawi.dto.message.MessageRequest;
import com.mahiberawi.dto.message.MessageResponse;
import com.mahiberawi.dto.message.ReactionRequest;
import com.mahiberawi.dto.message.ReactionResponse;
import com.mahiberawi.entity.Event;
import com.mahiberawi.entity.Group;
import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.PostReaction;
import com.mahiberawi.entity.User;
import com.mahiberawi.entity.MessageType;
import com.mahiberawi.exception.ResourceNotFoundException;
import com.mahiberawi.repository.EventRepository;
import com.mahiberawi.repository.GroupRepository;
import com.mahiberawi.repository.MessageRepository;
import com.mahiberawi.repository.PostReactionRepository;
import com.mahiberawi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final EventRepository eventRepository;
    private final PostReactionRepository postReactionRepository;
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
                    .orElseThrow(() -> new ResourceNotFoundException("Parent message", "id", request.getParentMessageId()));
            
            if (request.getGroupId() != null && parentMessage.getGroup() != null) {
                if (!parentMessage.getGroup().getId().equals(request.getGroupId())) {
                    throw new IllegalStateException("Parent message must belong to the same group");
                }
            }
            
            message.setParentMessage(parentMessage);
        }

        Message savedMessage = messageRepository.save(message);
        notifyRecipients(savedMessage);
        return mapToMessageResponse(savedMessage, sender);
    }

    public MessageResponse getMessage(String id, User currentUser) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));
        return mapToMessageResponse(message, currentUser);
    }

    public List<MessageResponse> getDirectMessages(String userId, User currentUser) {
        List<Message> messages = messageRepository.findByTypeAndRecipientIdOrSenderId(
                MessageType.DIRECT, userId, userId);
        return messages.stream()
                .map(message -> mapToMessageResponse(message, currentUser))
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getGroupMessages(String groupId, User currentUser) {
        List<Message> messages = messageRepository.findByTypeAndGroupId(MessageType.GROUP, groupId);
        return messages.stream()
                .map(message -> mapToMessageResponse(message, currentUser))
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getEventMessages(String eventId, User currentUser) {
        List<Message> messages = messageRepository.findByTypeAndEventId(MessageType.EVENT, eventId);
        return messages.stream()
                .map(message -> mapToMessageResponse(message, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse markAsRead(String id, User user) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (message.getRecipient() != null && message.getRecipient().getId().equals(user.getId())) {
            message.setRead(true);
            Message updatedMessage = messageRepository.save(message);
            return mapToMessageResponse(updatedMessage, user);
        }

        throw new IllegalStateException("User is not authorized to mark this message as read");
    }

    @Transactional
    public void deleteMessage(String id, User user) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", id));

        if (message.getSender() == null || !message.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("User is not authorized to delete this message");
        }

        messageRepository.delete(message);
    }

    @Transactional
    public ReactionResponse addReaction(String postId, ReactionRequest request, User user) {
        Message post = messageRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        postReactionRepository.findByPostIdAndUserId(postId, user.getId())
                .ifPresent(existingReaction -> {
                    throw new IllegalStateException("User already has a reaction to this post");
                });

        PostReaction reaction = PostReaction.builder()
                .postId(postId)
                .userId(user.getId())
                .reactionType(request.getReactionType())
                .createdAt(LocalDateTime.now())
                .build();

        postReactionRepository.save(reaction);

        return getReactionResponse(postId, user);
    }

    @Transactional
    public ReactionResponse removeReaction(String postId, String reactionType, User user) {
        Message post = messageRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        PostReaction reaction = postReactionRepository.findByPostIdAndUserIdAndReactionType(postId, user.getId(), reactionType)
                .orElseThrow(() -> new ResourceNotFoundException("Reaction not found"));

        postReactionRepository.delete(reaction);

        return getReactionResponse(postId, user);
    }

    public ReactionResponse getReactions(String postId, User user) {
        Message post = messageRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        return getReactionResponse(postId, user);
    }

    private ReactionResponse getReactionResponse(String postId, User user) {
        List<Object[]> reactionCounts = postReactionRepository.getReactionCountsByPost(postId);
        Map<String, Integer> reactions = new HashMap<>();
        
        for (Object[] result : reactionCounts) {
            String reactionType = (String) result[0];
            Long count = (Long) result[1];
            reactions.put(reactionType, count.intValue());
        }

        String userReaction = postReactionRepository.getUserReaction(postId, user.getId()).orElse(null);

        return ReactionResponse.builder()
                .postId(postId)
                .reactions(reactions)
                .userReaction(userReaction)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void notifyRecipients(Message message) {
        switch (message.getType()) {
            case DIRECT:
                if (message.getRecipient() != null) {
                    notificationService.createMessageNotification(message.getRecipient(), message);
                }
                break;
            case GROUP:
                if (message.getGroup() != null && message.getGroup().getMembers() != null) {
                    message.getGroup().getMembers().forEach(member -> {
                        if (member != null && member.getUser() != null) {
                            notificationService.createMessageNotification(member.getUser(), message);
                        }
                    });
                }
                break;
            case EVENT:
                if (message.getEvent() != null && message.getEvent().getParticipants() != null) {
                    message.getEvent().getParticipants().forEach(participant -> {
                        if (participant != null && participant.getUser() != null) {
                            notificationService.createMessageNotification(participant.getUser(), message);
                        }
                    });
                }
                break;
        }
    }

    private MessageResponse mapToMessageResponse(Message message, User currentUser) {
        List<MessageResponse> replies = message.getReplies().stream()
                .map(reply -> mapToMessageResponse(reply, currentUser))
                .collect(Collectors.toList());

        List<Object[]> reactionCounts = postReactionRepository.getReactionCountsByPost(message.getId());
        Map<String, Integer> reactions = new HashMap<>();
        
        for (Object[] result : reactionCounts) {
            String reactionType = (String) result[0];
            Long count = (Long) result[1];
            reactions.put(reactionType, count.intValue());
        }

        String userReaction = postReactionRepository.getUserReaction(message.getId(), currentUser.getId()).orElse(null);

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .type(message.getType())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderName(message.getSender() != null ? message.getSender().getName() : "Unknown")
                .senderProfilePicture(message.getSender() != null ? message.getSender().getProfilePicture() : null)
                .recipientId(message.getRecipient() != null ? message.getRecipient().getId() : null)
                .recipientName(message.getRecipient() != null ? message.getRecipient().getName() : null)
                .groupId(message.getGroup() != null ? message.getGroup().getId() : null)
                .groupName(message.getGroup() != null ? message.getGroup().getName() : null)
                .eventId(message.getEvent() != null ? message.getEvent().getId() : null)
                .eventTitle(message.getEvent() != null ? message.getEvent().getTitle() : null)
                .parentMessageId(message.getParentMessage() != null ? message.getParentMessage().getId() : null)
                .replies(replies)
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .isRead(message.isRead())
                .reactions(reactions)
                .userReaction(userReaction)
                .build();
    }
} 