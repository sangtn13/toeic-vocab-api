package com.toeic.vocab.model.vocabulary;

import com.toeic.vocab.enums.PartOfSpeech;
import com.toeic.vocab.enums.VocabularyLevel;
import com.toeic.vocab.model.base.BaseEntity;
import com.toeic.vocab.model.studyunit.StudyUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "vocabularies")
public class Vocabulary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private StudyUnit unit;

    @Column(nullable = false, length = 150)
    private String word;

    @Column(nullable = false, length = 255)
    private String meaning;

    @Column(length = 1000)
    private String definition;

    @Column(name = "example_sentence", length = 1000)
    private String exampleSentence;

    @Column(name = "example_translation", length = 1000)
    private String exampleTranslation;

    @Column(name = "phonetic_us", length = 100)
    private String phoneticUs;

    @Column(name = "phonetic_uk", length = 100)
    private String phoneticUk;

    @Column(name = "pronunciation_us_url", length = 500)
    private String pronunciationUsUrl;

    @Column(name = "pronunciation_uk_url", length = 500)
    private String pronunciationUkUrl;

    @Column(length = 255)
    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech", nullable = false, length = 50)
    private PartOfSpeech partOfSpeech;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 50)
    private VocabularyLevel difficultyLevel;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean active;
}
