package com.toeic.vocab.dto.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;
import java.util.List;

public record UnitCompletionDto(
        ProgressSummaryDto unitProgress,
        ProgressSummaryDto studySetProgress,
        StudyUnitActionDto nextUnit,
        List<StudyWordReviewDto> vocabularies) {
}