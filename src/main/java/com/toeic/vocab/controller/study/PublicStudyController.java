package com.toeic.vocab.controller.study;

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
import com.toeic.vocab.response.ApiResponse;
import com.toeic.vocab.response.PagedResponse;
import com.toeic.vocab.service.study.PublicStudyService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/api/v1}/public")
public class PublicStudyController {

    private final PublicStudyService publicStudyService;

    @PostMapping("/progress")
    public ResponseEntity<ApiResponse<StudyProgressResolutionDto>> resolveProgress(
            @Valid @RequestBody(required = false) ResolveStudyProgressRequest request) {
        ResolveStudyProgressRequest safeRequest = request == null ? new ResolveStudyProgressRequest(null, null, null)
                : request;
        StudyProgressResolutionDto result = publicStudyService.resolveStudyProgress(safeRequest);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        String message = result.created() ? "Study progress initialized successfully"
                : "Study progress resolved successfully";
        return ResponseEntity.status(status)
                .body(ApiResponse.success(message, result));
    }

    @GetMapping("/study-sets")
    public ResponseEntity<ApiResponse<PagedResponse<StudySetCardDto>>> getStudySets(
            @RequestParam(required = false) String progressToken,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study sets fetched successfully",
                PagedResponse.from(publicStudyService.getPublishedStudySets(progressToken, pageable))));
    }

    @GetMapping("/study-sets/{slug}")
    public ResponseEntity<ApiResponse<StudySetDetailDto>> getStudySetDetail(
            @PathVariable String slug,
            @RequestParam(required = false) String progressToken) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study set detail fetched successfully",
                publicStudyService.getStudySetDetail(slug, progressToken)));
    }

    @GetMapping("/study-sets/{slug}/units")
    public ResponseEntity<ApiResponse<PagedResponse<StudyUnitProgressDto>>> getStudySetUnits(
            @PathVariable String slug,
            @RequestParam(required = false) String progressToken,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study units fetched successfully",
                PagedResponse.from(publicStudyService.getStudySetUnits(slug, progressToken, pageable))));
    }

    @GetMapping("/study-sets/{slug}/units/{unitId}/activities/{mode}")
    public ResponseEntity<ApiResponse<StudyActivityDto>> getStudyActivity(
            @PathVariable String slug,
            @PathVariable UUID unitId,
            @PathVariable PracticeMode mode,
            @RequestParam(required = false) String progressToken) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study activity fetched successfully",
                publicStudyService.getStudyActivity(slug, unitId, mode, progressToken)));
    }

    @PostMapping("/progress/{progressToken}/answers")
    public ResponseEntity<ApiResponse<AnswerResultDto>> submitAnswer(
            @PathVariable String progressToken,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Answer submitted successfully",
                publicStudyService.submitAnswer(progressToken, request)));
    }

    @PostMapping("/progress/{progressToken}/study-sets/{slug}/units/{unitId}/restart")
    public ResponseEntity<ApiResponse<RestartUnitResultDto>> restartUnit(
            @PathVariable String progressToken,
            @PathVariable String slug,
            @PathVariable UUID unitId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Unit restarted successfully",
                publicStudyService.restartUnit(progressToken, slug, unitId)));
    }
}
