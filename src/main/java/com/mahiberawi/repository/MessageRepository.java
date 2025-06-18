package com.mahiberawi.repository;

import com.mahiberawi.entity.Message;
import com.mahiberawi.entity.MessageType;
import com.mahiberawi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByTypeAndRecipientIdOrSenderId(MessageType type, String recipientId, String senderId);
    List<Message> findByTypeAndGroupId(MessageType type, String groupId);
    List<Message> findByTypeAndEventId(MessageType type, String eventId);
    List<Message> findByRecipientAndReadFalse(User recipient);
    List<Message> findByGroupId(String groupId);
} 