package com.toeic.vocab.repository.progress;

import static org.assertj.core.api.Assertions.assertThat;

import com.toeic.vocab.config.JpaAuditingConfig;
import com.toeic.vocab.enums.PartOfSpeech;
import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.enums.UserRole;
import com.toeic.vocab.enums.VocabularyLevel;
import com.toeic.vocab.model.progress.StudyProgress;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.model.studyunit.StudyUnit;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class StudyProgressRepositoryTest {

    @Autowired
    private StudyProgressRepository studyProgressRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldDeleteProgressByUserAndUnitAndReturnDeletedCount() {
        StudySet studySet = entityManager.persistAndFlush(StudySet.builder()
                .title("600 TOEIC")
                .slug("600-toeic")
                .displayOrder(1)
                .status(StudySetStatus.PUBLISHED)
                .build());

        StudyUnit unit = entityManager.persistAndFlush(StudyUnit.builder()
                .studySet(studySet)
                .title("Unit 1")
                .unitOrder(1)
                .active(true)
                .build());

        Vocabulary vocabulary = entityManager.persistAndFlush(Vocabulary.builder()
                .unit(unit)
                .word("abide by")
                .meaning("tuan thu")
                .partOfSpeech(PartOfSpeech.PHRASAL_VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build());

        AppUser user = entityManager.persistAndFlush(AppUser.builder()
                .email("learner@test.local")
                .passwordHash("secret")
                .fullName("Learner")
                .role(UserRole.USER)
                .active(true)
                .build());

        entityManager.persistAndFlush(StudyProgress.builder()
                .user(user)
                .vocabulary(vocabulary)
                .attemptCount(1)
                .correctCount(1)
                .mastered(true)
                .build());

        long deletedCount = studyProgressRepository.deleteByUserIdAndUnitId(user.getId(), unit.getId());

        assertThat(deletedCount).isEqualTo(1);
        assertThat(studyProgressRepository.findByUserId(user.getId())).isEmpty();
    }
}
