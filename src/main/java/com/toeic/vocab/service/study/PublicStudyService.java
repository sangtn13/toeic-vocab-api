package com.toeic.vocab.service.study;

import com.toeic.vocab.dto.study.AnswerResultDto;
import com.toeic.vocab.dto.study.RestartUnitResultDto;
import com.toeic.vocab.dto.study.StudyActivityDto;
import com.toeic.vocab.dto.study.StudyProgressResolutionDto;
import com.toeic.vocab.dto.study.StudySetCardDto;
import com.toeic.vocab.dto.study.StudySetDetailDto;
import com.toeic.vocab.dto.study.StudyUnitProgressDto;
import com.toeic.vocab.enums.PracticeMode;
import com.toeic.vocab.request.study.ResolveStudyProgressRequest;
import com.toeic.vocab.request.study.SubmitAnswerRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PublicStudyService {

    StudyProgressResolutionDto resolveStudyProgress(ResolveStudyProgressRequest request);

    Page<StudySetCardDto> getPublishedStudySets(String progressToken, Pageable pageable);

    StudySetDetailDto getStudySetDetail(String slug, String progressToken);

    Page<StudyUnitProgressDto> getStudySetUnits(String slug, String progressToken, Pageable pageable);

    StudyActivityDto getStudyActivity(String slug, UUID unitId, PracticeMode mode, String progressToken);

    RestartUnitResultDto restartUnit(String progressToken, String slug, UUID unitId);

    AnswerResultDto submitAnswer(String progressToken, SubmitAnswerRequest request);
}
