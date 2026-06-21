package com.toeic.vocab.dto.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;

public record StudySetDetailDto(
        String title,
        String description,
        ProgressSummaryDto progress) {
}