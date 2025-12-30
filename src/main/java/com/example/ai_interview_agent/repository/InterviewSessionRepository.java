package com.example.ai_interview_agent.repository;

import com.example.ai_interview_agent.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    Optional<InterviewSession> findByUserIdAndStatus(Long userId, InterviewSession.InterviewStatus status);
}
