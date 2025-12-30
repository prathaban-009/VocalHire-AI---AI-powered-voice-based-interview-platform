package com.example.ai_interview_agent.repository;

import com.example.ai_interview_agent.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByCandidateIdAndLevel(Long candidateId, String level);

    List<InterviewQuestion> findByCandidateId(Long candidateId);

    List<InterviewQuestion> findBySessionId(Long sessionId);
}