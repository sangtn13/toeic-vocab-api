package com.toeic.vocab.service.study;

import com.toeic.vocab.dto.study.StudyChoiceDto;
import com.toeic.vocab.dto.study.StudyItemDto;
import com.toeic.vocab.enums.PracticeMode;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.util.StudyTextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StudyItemFactory {

    public StudyItemDto toStudyItemDto(
            PracticeMode mode,
            Vocabulary vocabulary,
            List<Vocabulary> studySetPool,
            boolean mastered) {
        List<StudyChoiceDto> choices = switch (mode) {
            case MULTIPLE_CHOICE -> buildChoices(vocabulary, studySetPool, true);
            case REVERSE_MULTIPLE_CHOICE -> buildChoices(vocabulary, studySetPool, false);
            default -> List.of();
        };

        return new StudyItemDto(
                vocabulary.getId(),
                mastered,
                mode == PracticeMode.FLASHCARD || mode == PracticeMode.REVERSE_MULTIPLE_CHOICE ? vocabulary.getWord()
                        : null,
                mode == PracticeMode.FLASHCARD || mode == PracticeMode.GUESS_WORD
                        || mode == PracticeMode.MULTIPLE_CHOICE
                                ? vocabulary.getMeaning()
                                : null,
                vocabulary.getDefinition(),
                mode == PracticeMode.FLASHCARD ? vocabulary.getExampleSentence() : null,
                mode == PracticeMode.FLASHCARD ? null
                        : StudyTextUtils.maskWord(vocabulary.getExampleSentence(), vocabulary.getWord()),
                vocabulary.getExampleTranslation(),
                vocabulary.getPhoneticUs(),
                vocabulary.getPhoneticUk(),
                vocabulary.getPronunciationUsUrl(),
                vocabulary.getPronunciationUkUrl(),
                vocabulary.getHint(),
                vocabulary.getPartOfSpeech(),
                choices);
    }

    private List<StudyChoiceDto> buildChoices(Vocabulary vocabulary, List<Vocabulary> studySetPool,
            boolean wordChoices) {
        List<String> distractors = studySetPool.stream()
                .filter(item -> !item.getId().equals(vocabulary.getId()))
                .map(item -> wordChoices ? item.getWord() : item.getMeaning())
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        List<String> shuffled = new ArrayList<>(distractors);
        Collections.shuffle(shuffled);
        List<String> options = new ArrayList<>();
        options.add(wordChoices ? vocabulary.getWord() : vocabulary.getMeaning());
        options.addAll(shuffled.stream().limit(3).toList());
        Collections.shuffle(options);
        return options.stream().map(value -> new StudyChoiceDto(value, value)).toList();
    }
}