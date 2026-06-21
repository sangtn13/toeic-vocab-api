package com.toeic.vocab.dto.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;
import com.toeic.vocab.enums.PracticeMode;
import java.util.List;

public record StudyActivityDto(
        PracticeMode mode,
        String studySetTitle,
        String unitTitle,
        ProgressSummaryDto studySetProgress,
        ProgressSummaryDto unitProgress,
        List<StudyItemDto> items) {
}