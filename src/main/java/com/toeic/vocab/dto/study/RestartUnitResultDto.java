package com.toeic.vocab.dto.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;

public record RestartUnitResultDto(
        ProgressSummaryDto unitProgress,
        ProgressSummaryDto studySetProgress,
        StudyProgressContextDto progress) {
}