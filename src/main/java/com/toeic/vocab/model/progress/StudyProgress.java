package com.toeic.vocab.model.progress;

import com.toeic.vocab.enums.PracticeMode;
import com.toeic.vocab.model.base.BaseEntity;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "study_progress")
public class StudyProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(nullable = false)
    private Boolean mastered;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_mode", length = 50)
    private PracticeMode lastMode;

    @Column(name = "last_answer", length = 255)
    private String lastAnswer;

    @Column(name = "last_correct_at")
    private LocalDateTime lastCorrectAt;
}
