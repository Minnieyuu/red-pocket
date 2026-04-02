package com.example.yumi.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.yumi.Enum.FailedMessageStatus;
import com.example.yumi.entity.FailedMessage;

@Repository
public interface FailedMessageDao extends JpaRepository<FailedMessage, Long> {

	List<FailedMessage> findByStatus(FailedMessageStatus pending);

}
