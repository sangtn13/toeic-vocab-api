package com.toeic.vocab.dto.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;
import com.toeic.vocab.enums.PracticeMode;
import java.util.UUID;

public record AnswerResultDto(
        UUID vocabularyId,
        PracticeMode practiceMode,
        boolean correct,
        String correctAnswer,
        boolean unitCompleted,
        ProgressSummaryDto studySetProgress,
        ProgressSummaryDto unitProgress,
        StudyProgressContextDto progress,
        StudyActivityDto studyActivity,
        UnitCompletionDto unitCompletion) {
}