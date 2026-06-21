package com.toeic.vocab.repository.studyset;

import static org.assertj.core.api.Assertions.assertThat;

import com.toeic.vocab.config.JpaAuditingConfig;
import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.model.studyset.StudySet;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class StudySetRepositoryTest {

    @Autowired
    private StudySetRepository studySetRepository;

    @Test
    void shouldReturnStudySetsWhenKeywordIsNull() {
        StudySet studySet = StudySet.builder()
            .title("600 TOEIC Words")
            .slug("600-toeic-words")
            .description("Core TOEIC vocabulary")
            .thumbnailUrl("https://example.com/thumb.png")
            .displayOrder(1)
            .status(StudySetStatus.PUBLISHED)
            .build();
        studySet.setCreatedAt(LocalDateTime.now());
        studySet.setUpdatedAt(LocalDateTime.now());
        studySetRepository.save(studySet);

        var result = studySetRepository.search(null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSlug()).isEqualTo("600-toeic-words");
    }
}
