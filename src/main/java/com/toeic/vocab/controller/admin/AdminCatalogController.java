package com.toeic.vocab.controller.admin;

import com.toeic.vocab.dto.admin.AdminStudySetDto;
import com.toeic.vocab.dto.admin.AdminStudyUnitDto;
import com.toeic.vocab.dto.admin.AdminVocabularyDto;
import com.toeic.vocab.request.admin.UpsertStudySetRequest;
import com.toeic.vocab.request.admin.UpsertStudyUnitRequest;
import com.toeic.vocab.request.admin.UpsertVocabularyRequest;
import com.toeic.vocab.response.ApiResponse;
import com.toeic.vocab.response.PagedResponse;
import com.toeic.vocab.service.admin.AdminCatalogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix:/api/v1}/admin")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;

    @GetMapping("/study-sets")
    public ResponseEntity<ApiResponse<PagedResponse<AdminStudySetDto>>> getStudySets(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "displayOrder") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study sets fetched successfully",
                PagedResponse.from(adminCatalogService.getStudySets(keyword, pageable))));
    }

    @GetMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<AdminStudySetDto>> getStudySet(@PathVariable UUID studySetId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study set fetched successfully",
                adminCatalogService.getStudySet(studySetId)));
    }

    @PostMapping("/study-sets")
    public ResponseEntity<ApiResponse<AdminStudySetDto>> createStudySet(
            @Valid @RequestBody UpsertStudySetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Study set created successfully",
                        adminCatalogService.createStudySet(request)));
    }

    @PutMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<AdminStudySetDto>> updateStudySet(
            @PathVariable UUID studySetId,
            @Valid @RequestBody UpsertStudySetRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study set updated successfully",
                adminCatalogService.updateStudySet(studySetId, request)));
    }

    @DeleteMapping("/study-sets/{studySetId}")
    public ResponseEntity<ApiResponse<Void>> deleteStudySet(@PathVariable UUID studySetId) {
        adminCatalogService.deleteStudySet(studySetId);
        return ResponseEntity.ok(ApiResponse.success("Study set deleted successfully", null));
    }

    @GetMapping("/study-sets/{studySetId}/units")
    public ResponseEntity<ApiResponse<List<AdminStudyUnitDto>>> getUnits(@PathVariable UUID studySetId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study units fetched successfully",
                adminCatalogService.getUnits(studySetId)));
    }

    @PostMapping("/study-sets/{studySetId}/units")
    public ResponseEntity<ApiResponse<AdminStudyUnitDto>> createUnit(
            @PathVariable UUID studySetId,
            @Valid @RequestBody UpsertStudyUnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Study unit created successfully",
                        adminCatalogService.createUnit(studySetId, request)));
    }

    @PutMapping("/units/{unitId}")
    public ResponseEntity<ApiResponse<AdminStudyUnitDto>> updateUnit(
            @PathVariable UUID unitId,
            @Valid @RequestBody UpsertStudyUnitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Study unit updated successfully",
                adminCatalogService.updateUnit(unitId, request)));
    }

    @DeleteMapping("/units/{unitId}")
    public ResponseEntity<ApiResponse<Void>> deleteUnit(@PathVariable UUID unitId) {
        adminCatalogService.deleteUnit(unitId);
        return ResponseEntity.ok(ApiResponse.success("Study unit deleted successfully", null));
    }

    @GetMapping("/units/{unitId}/vocabularies")
    public ResponseEntity<ApiResponse<PagedResponse<AdminVocabularyDto>>> getVocabularies(
            @PathVariable UUID unitId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "displayOrder") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vocabularies fetched successfully",
                PagedResponse.from(adminCatalogService.getVocabularies(unitId, keyword, pageable))));
    }

    @PostMapping("/units/{unitId}/vocabularies")
    public ResponseEntity<ApiResponse<AdminVocabularyDto>> createVocabulary(
            @PathVariable UUID unitId,
            @Valid @RequestBody UpsertVocabularyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vocabulary created successfully",
                        adminCatalogService.createVocabulary(unitId, request)));
    }

    @PutMapping("/vocabularies/{vocabularyId}")
    public ResponseEntity<ApiResponse<AdminVocabularyDto>> updateVocabulary(
            @PathVariable UUID vocabularyId,
            @Valid @RequestBody UpsertVocabularyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vocabulary updated successfully",
                adminCatalogService.updateVocabulary(vocabularyId, request)));
    }

    @DeleteMapping("/vocabularies/{vocabularyId}")
    public ResponseEntity<ApiResponse<Void>> deleteVocabulary(@PathVariable UUID vocabularyId) {
        adminCatalogService.deleteVocabulary(vocabularyId);
        return ResponseEntity.ok(ApiResponse.success("Vocabulary deleted successfully", null));
    }
}
