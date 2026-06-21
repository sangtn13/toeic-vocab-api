package com.toeic.vocab.service.admin;

import com.toeic.vocab.dto.admin.AdminStudySetDto;
import com.toeic.vocab.dto.admin.AdminStudyUnitDto;
import com.toeic.vocab.dto.admin.AdminVocabularyDto;
import com.toeic.vocab.request.admin.UpsertStudySetRequest;
import com.toeic.vocab.request.admin.UpsertStudyUnitRequest;
import com.toeic.vocab.request.admin.UpsertVocabularyRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCatalogService {

    Page<AdminStudySetDto> getStudySets(String keyword, Pageable pageable);

    AdminStudySetDto getStudySet(UUID studySetId);

    AdminStudySetDto createStudySet(UpsertStudySetRequest request);

    AdminStudySetDto updateStudySet(UUID studySetId, UpsertStudySetRequest request);

    void deleteStudySet(UUID studySetId);

    List<AdminStudyUnitDto> getUnits(UUID studySetId);

    AdminStudyUnitDto createUnit(UUID studySetId, UpsertStudyUnitRequest request);

    AdminStudyUnitDto updateUnit(UUID unitId, UpsertStudyUnitRequest request);

    void deleteUnit(UUID unitId);

    Page<AdminVocabularyDto> getVocabularies(UUID unitId, String keyword, Pageable pageable);

    AdminVocabularyDto createVocabulary(UUID unitId, UpsertVocabularyRequest request);

    AdminVocabularyDto updateVocabulary(UUID vocabularyId, UpsertVocabularyRequest request);

    void deleteVocabulary(UUID vocabularyId);
}
