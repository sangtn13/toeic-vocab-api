package com.toeic.vocab.service.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;
import com.toeic.vocab.enums.StudySetLearningStatus;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StudyProgressSupport {

    public ProgressSummaryDto buildProgressSummary(
            List<Vocabulary> vocabularies,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        int totalWords = vocabularies.size();
        int learnedWords = 0;
        int masteredWords = 0;
        for (Vocabulary vocabulary : vocabularies) {
            StudyProgressSnapshot progress = progressByVocabularyId.get(vocabulary.getId());
            if (progress == null) {
                continue;
            }
            if (progress.attemptCount() > 0) {
                learnedWords++;
            }
            if (progress.mastered()) {
                masteredWords++;
            }
        }
        return toSummary(totalWords, learnedWords, masteredWords);
    }

    public ProgressSummaryDto buildProgressSummaryFromVocabularyIds(
            List<UUID> vocabularyIds,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        int totalWords = vocabularyIds.size();
        int learnedWords = 0;
        int masteredWords = 0;
        for (UUID vocabularyId : vocabularyIds) {
            StudyProgressSnapshot progress = progressByVocabularyId.get(vocabularyId);
            if (progress == null) {
                continue;
            }
            if (progress.attemptCount() > 0) {
                learnedWords++;
            }
            if (progress.mastered()) {
                masteredWords++;
            }
        }
        return toSummary(totalWords, learnedWords, masteredWords);
    }

    public boolean isUnitCompleted(ProgressSummaryDto unitProgress) {
        return unitProgress.totalWords() > 0 && unitProgress.masteredWords() == unitProgress.totalWords();
    }

    public StudySetLearningStatus resolveStudySetLearningStatus(ProgressSummaryDto progress) {
        if (progress.totalWords() > 0 && progress.masteredWords() == progress.totalWords()) {
            return StudySetLearningStatus.COMPLETED;
        }
        if (progress.learnedWords() > 0) {
            return StudySetLearningStatus.IN_PROGRESS;
        }
        return StudySetLearningStatus.NOT_STARTED;
    }

    public ProgressSummaryDto toSummary(int totalWords, int learnedWords, int masteredWords) {
        int percentage = totalWords == 0 ? 0 : (int) Math.round(masteredWords * 100.0 / totalWords);
        return new ProgressSummaryDto(totalWords, learnedWords, masteredWords, percentage);
    }

    public ProgressSummaryDto toSummary(ProgressSummaryAggregate aggregate) {
        return toSummary(
                Math.toIntExact(aggregate.totalWords()),
                Math.toIntExact(aggregate.learnedWords()),
                Math.toIntExact(aggregate.masteredWords()));
    }
}
