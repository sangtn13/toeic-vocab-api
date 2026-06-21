package com.toeic.vocab.service.admin;

import com.toeic.vocab.dto.admin.AdminStudySetDto;
import com.toeic.vocab.dto.admin.AdminStudyUnitDto;
import com.toeic.vocab.dto.admin.AdminVocabularyDto;
import com.toeic.vocab.exception.studyset.StudySetNotFoundException;
import com.toeic.vocab.exception.studyunit.StudyUnitNotFoundException;
import com.toeic.vocab.exception.vocabulary.VocabularyNotFoundException;
import com.toeic.vocab.mapper.AdminCatalogMapper;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.model.studyunit.StudyUnit;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.repository.studyset.StudySetRepository;
import com.toeic.vocab.repository.studyunit.StudyUnitRepository;
import com.toeic.vocab.repository.vocabulary.VocabularyRepository;
import com.toeic.vocab.request.admin.UpsertStudySetRequest;
import com.toeic.vocab.request.admin.UpsertStudyUnitRequest;
import com.toeic.vocab.request.admin.UpsertVocabularyRequest;
import com.toeic.vocab.util.StringNormalizer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCatalogServiceImpl implements AdminCatalogService {

    private final StudySetRepository studySetRepository;
    private final StudyUnitRepository studyUnitRepository;
    private final VocabularyRepository vocabularyRepository;
    private final AdminCatalogMapper adminCatalogMapper;
    private final StudySetSlugGenerator studySetSlugGenerator;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminStudySetDto> getStudySets(String keyword, Pageable pageable) {
        String normalizedKeyword = StringNormalizer.trimToNull(keyword);
        return studySetRepository.search(normalizedKeyword, pageable).map(adminCatalogMapper::toStudySetDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStudySetDto getStudySet(UUID studySetId) {
        return adminCatalogMapper.toStudySetDto(findStudySet(studySetId));
    }

    @Override
    @Transactional
    public AdminStudySetDto createStudySet(UpsertStudySetRequest request) {
        String normalizedTitle = request.title().trim();
        String slug = studySetSlugGenerator.resolveUniqueSlug(normalizedTitle, null);
        StudySet studySet = adminCatalogMapper.toStudySet(request, normalizedTitle, slug);
        return adminCatalogMapper.toStudySetDto(studySetRepository.save(studySet));
    }

    @Override
    @Transactional
    public AdminStudySetDto updateStudySet(UUID studySetId, UpsertStudySetRequest request) {
        StudySet studySet = findStudySet(studySetId);
        String normalizedTitle = request.title().trim();
        adminCatalogMapper.updateStudySetFromRequest(request, normalizedTitle,
                studySetSlugGenerator.resolveUniqueSlug(normalizedTitle, studySetId), studySet);
        return adminCatalogMapper.toStudySetDto(studySetRepository.save(studySet));
    }

    @Override
    @Transactional
    public void deleteStudySet(UUID studySetId) {
        studySetRepository.delete(findStudySet(studySetId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminStudyUnitDto> getUnits(UUID studySetId) {
        findStudySet(studySetId);
        return studyUnitRepository.findByStudySetIdOrderByUnitOrderAscIdAsc(studySetId)
                .stream()
                .map(adminCatalogMapper::toStudyUnitDto)
                .toList();
    }

    @Override
    @Transactional
    public AdminStudyUnitDto createUnit(UUID studySetId, UpsertStudyUnitRequest request) {
        StudySet studySet = findStudySet(studySetId);
        StudyUnit studyUnit = adminCatalogMapper.toStudyUnit(request, studySet, request.title().trim());
        return adminCatalogMapper.toStudyUnitDto(studyUnitRepository.save(studyUnit));
    }

    @Override
    @Transactional
    public AdminStudyUnitDto updateUnit(UUID unitId, UpsertStudyUnitRequest request) {
        StudyUnit studyUnit = findStudyUnit(unitId);
        adminCatalogMapper.updateStudyUnitFromRequest(request, request.title().trim(), studyUnit);
        return adminCatalogMapper.toStudyUnitDto(studyUnitRepository.save(studyUnit));
    }

    @Override
    @Transactional
    public void deleteUnit(UUID unitId) {
        studyUnitRepository.delete(findStudyUnit(unitId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVocabularyDto> getVocabularies(UUID unitId, String keyword, Pageable pageable) {
        findStudyUnit(unitId);
        String normalizedKeyword = StringNormalizer.trimToNull(keyword);
        return vocabularyRepository.search(unitId, normalizedKeyword, pageable)
                .map(adminCatalogMapper::toVocabularyDto);
    }

    @Override
    @Transactional
    public AdminVocabularyDto createVocabulary(UUID unitId, UpsertVocabularyRequest request) {
        StudyUnit studyUnit = findStudyUnit(unitId);
        Vocabulary vocabulary = adminCatalogMapper.toVocabulary(request, studyUnit, request.word().trim(),
                request.meaning().trim());
        return adminCatalogMapper.toVocabularyDto(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public AdminVocabularyDto updateVocabulary(UUID vocabularyId, UpsertVocabularyRequest request) {
        Vocabulary vocabulary = findVocabulary(vocabularyId);
        adminCatalogMapper.updateVocabularyFromRequest(request, request.word().trim(), request.meaning().trim(),
                vocabulary);
        return adminCatalogMapper.toVocabularyDto(vocabularyRepository.save(vocabulary));
    }

    @Override
    @Transactional
    public void deleteVocabulary(UUID vocabularyId) {
        vocabularyRepository.delete(findVocabulary(vocabularyId));
    }

    private StudySet findStudySet(UUID studySetId) {
        return studySetRepository.findById(studySetId)
                .orElseThrow(() -> new StudySetNotFoundException(String.valueOf(studySetId)));
    }

    private StudyUnit findStudyUnit(UUID unitId) {
        return studyUnitRepository.findById(unitId)
                .orElseThrow(() -> new StudyUnitNotFoundException(unitId));
    }

    private Vocabulary findVocabulary(UUID vocabularyId) {
        return vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new VocabularyNotFoundException(vocabularyId));
    }
}
