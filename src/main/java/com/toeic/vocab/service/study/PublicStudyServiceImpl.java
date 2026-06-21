package com.toeic.vocab.service.study;

import com.toeic.vocab.dto.shared.ProgressSummaryDto;
import com.toeic.vocab.dto.study.AnswerResultDto;
import com.toeic.vocab.dto.study.RestartUnitResultDto;
import com.toeic.vocab.dto.study.StudyActivityDto;
import com.toeic.vocab.dto.study.StudyItemDto;
import com.toeic.vocab.dto.study.StudyProgressContextDto;
import com.toeic.vocab.dto.study.StudyProgressResolutionDto;
import com.toeic.vocab.dto.study.StudySetCardDto;
import com.toeic.vocab.dto.study.StudyUnitActionDto;
import com.toeic.vocab.dto.study.StudySetDetailDto;
import com.toeic.vocab.dto.study.StudyUnitProgressDto;
import com.toeic.vocab.dto.study.UnitCompletionDto;
import com.toeic.vocab.enums.PracticeMode;
import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.exception.progress.StudyProgressNotFoundException;
import com.toeic.vocab.exception.studyset.StudySetNotFoundException;
import com.toeic.vocab.exception.studyunit.StudyUnitNotFoundException;
import com.toeic.vocab.exception.vocabulary.VocabularyNotFoundException;
import com.toeic.vocab.model.progress.StudyProgress;
import com.toeic.vocab.mapper.StudyMapper;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.model.studyunit.StudyUnit;
import com.toeic.vocab.model.user.AppUser;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.repository.progress.StudyProgressRepository;
import com.toeic.vocab.repository.studyset.StudySetRepository;
import com.toeic.vocab.repository.studyunit.StudyUnitRepository;
import com.toeic.vocab.repository.vocabulary.VocabularyRepository;
import com.toeic.vocab.request.study.ResolveStudyProgressRequest;
import com.toeic.vocab.request.study.SubmitAnswerRequest;
import com.toeic.vocab.security.auth.CurrentUserProvider;
import com.toeic.vocab.util.AppTime;
import com.toeic.vocab.util.StringNormalizer;
import com.toeic.vocab.util.StudyTextUtils;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PublicStudyServiceImpl implements PublicStudyService {

    private static final String USER_PROGRESS_TOKEN_PREFIX = "usr";

    private final StudySetRepository studySetRepository;
    private final StudyUnitRepository studyUnitRepository;
    private final VocabularyRepository vocabularyRepository;
    private final StudyProgressRepository studyProgressRepository;
    private final CurrentUserProvider currentUserProvider;
    private final GuestProgressTokenCodec guestProgressTokenCodec;
    private final StudyMapper studyMapper;
    private final StudyProgressSupport studyProgressSupport;
    private final StudyItemFactory studyItemFactory;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public StudyProgressResolutionDto resolveStudyProgress(ResolveStudyProgressRequest request) {
        AppUser currentUser = currentUserProvider.getCurrentUser().orElse(null);
        return currentUser == null
                ? createOrReuseGuestProgress(request)
                : resolveUserProgress(request, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudySetCardDto> getPublishedStudySets(String progressToken, Pageable pageable) {
        Pageable normalizedPageable = normalizePageable(pageable);
        StudyProgressState progressState = findProgressIfPresent(progressToken);
        if (progressState == null) {
            return studySetRepository.findStudySetCardStatsByStatus(StudySetStatus.PUBLISHED, normalizedPageable)
                    .map(this::toStudySetCardDto);
        }
        if (progressState.persisted()) {
            return studySetRepository.findStudySetCardProgressByStatusAndUserId(
                    StudySetStatus.PUBLISHED,
                    progressState.userId(),
                    normalizedPageable)
                    .map(this::toStudySetCardDto);
        }

        Page<StudySetCardAggregate> page = studySetRepository.findStudySetCardStatsByStatus(
                StudySetStatus.PUBLISHED,
                normalizedPageable);
        if (page.isEmpty()) {
            return page.map(this::toStudySetCardDto);
        }

        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = loadProgressMap(progressState);
        Map<UUID, ProgressSummaryAggregate> progressByStudySetId = new HashMap<>();
        List<UUID> studySetIds = page.stream().map(StudySetCardAggregate::id).toList();
        for (StudySetVocabularyRef ref : studySetRepository.findStudySetVocabularyRefsByStudySetIds(studySetIds)) {
            StudyProgressSnapshot progress = progressByVocabularyId.get(ref.vocabularyId());
            ProgressSummaryAggregate current = progressByStudySetId.getOrDefault(
                    ref.studySetId(),
                    new ProgressSummaryAggregate(0, 0, 0));
            progressByStudySetId.put(
                    ref.studySetId(),
                    new ProgressSummaryAggregate(
                            current.totalWords() + 1,
                            current.learnedWords() + (progress != null && progress.attemptCount() > 0 ? 1 : 0),
                            current.masteredWords() + (progress != null && progress.mastered() ? 1 : 0)));
        }
        return page.map(aggregate -> toStudySetCardDto(mergeStudySetCardProgress(
                        aggregate,
                        progressByStudySetId.getOrDefault(
                                aggregate.id(),
                                new ProgressSummaryAggregate(aggregate.totalWords(), 0, 0)))));
    }

    @Override
    @Transactional(readOnly = true)
    public StudySetDetailDto getStudySetDetail(String slug, String progressToken) {
        StudySet studySet = findPublishedStudySet(slug);
        StudyProgressState progressState = findProgressIfPresent(progressToken);
        List<Vocabulary> studySetVocabularies = vocabularyRepository
                .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(studySet.getId());
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = loadProgressMap(progressState);

        ProgressSummaryDto summary = studyProgressSupport.buildProgressSummary(
                studySetVocabularies,
                progressByVocabularyId);
        return new StudySetDetailDto(
                studySet.getTitle(),
                studySet.getDescription(),
                summary);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudyUnitProgressDto> getStudySetUnits(String slug, String progressToken, Pageable pageable) {
        StudySet studySet = findPublishedStudySet(slug);
        Pageable normalizedPageable = normalizePageable(pageable);
        Page<StudyUnit> unitPage = studyUnitRepository.findPublicUnitsByStudySetId(studySet.getId(), normalizedPageable);
        if (unitPage.isEmpty()) {
            return new PageImpl<>(List.of(), normalizedPageable, unitPage.getTotalElements());
        }

        List<StudyUnit> pageUnits = unitPage.getContent();
        List<UUID> pageUnitIds = pageUnits.stream().map(StudyUnit::getId).toList();
        List<Vocabulary> pageVocabularies = vocabularyRepository
                .findByUnitIdInAndActiveTrueOrderByUnitOrderAscDisplayOrderAscIdAsc(pageUnitIds);
        List<UUID> pageVocabularyIds = pageVocabularies.stream().map(Vocabulary::getId).toList();
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = loadProgressMapForVocabularyIds(
                findProgressIfPresent(progressToken),
                pageVocabularyIds);
        Map<UUID, List<Vocabulary>> vocabulariesByUnitId = groupVocabulariesByUnitId(pageVocabularies);

        List<StudyUnitProgressDto> pageItems = pageUnits.stream()
                .map(unit -> toStudyUnitProgressDto(
                        unit,
                        vocabulariesByUnitId.getOrDefault(unit.getId(), List.of()),
                        progressByVocabularyId))
                .toList();

        return new PageImpl<>(pageItems, normalizedPageable, unitPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public StudyActivityDto getStudyActivity(String slug, UUID unitId, PracticeMode mode, String progressToken) {
        StudyUnit unit = studyUnitRepository.findPublicUnit(slug, unitId, StudySetStatus.PUBLISHED)
                .orElseThrow(() -> new StudyUnitNotFoundException(unitId));
        StudyProgressState progressState = findProgressIfPresent(progressToken);
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = loadProgressMap(progressState);

        return buildStudyActivity(mode, unit, progressByVocabularyId);
    }


    @Override
    @Transactional
    public RestartUnitResultDto restartUnit(String progressToken, String slug, UUID unitId) {
        StudyProgressState progressState = findProgress(progressToken);
        StudyUnit unit = studyUnitRepository.findPublicUnit(slug, unitId, StudySetStatus.PUBLISHED)
                .orElseThrow(() -> new StudyUnitNotFoundException(unitId));
        List<Vocabulary> studySetVocabularies = vocabularyRepository
                .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(unit.getStudySet().getId());
        List<Vocabulary> unitVocabularies = filterVocabulariesByUnitId(studySetVocabularies, unit.getId());
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = new HashMap<>(loadProgressMap(progressState));
        Set<UUID> unitVocabularyIds = new java.util.HashSet<>();
        for (Vocabulary vocabulary : unitVocabularies) {
            unitVocabularyIds.add(vocabulary.getId());
        }

        long clearedProgressCount;
        StudyProgressState resolvedProgress;
        if (progressState.persisted()) {
            clearedProgressCount = studyProgressRepository.deleteByUserIdAndUnitId(progressState.userId(), unitId);
            unitVocabularyIds.forEach(progressByVocabularyId::remove);
            resolvedProgress = refreshPersistedProgress(progressState, progressByVocabularyId);
        } else {
            long before = progressByVocabularyId.size();
            unitVocabularyIds.forEach(progressByVocabularyId::remove);
            clearedProgressCount = before - progressByVocabularyId.size();
            resolvedProgress = reissueGuestProgress(progressState, progressState.displayName(),
                    progressState.clientKey(), progressByVocabularyId);
        }

        return new RestartUnitResultDto(
                studyProgressSupport.buildProgressSummary(unitVocabularies, progressByVocabularyId),
                studyProgressSupport.buildProgressSummary(studySetVocabularies, progressByVocabularyId),
                studyMapper.toStudyProgressContextDto(resolvedProgress));
    }

    @Override
    @Transactional
    public AnswerResultDto submitAnswer(String progressToken, SubmitAnswerRequest request) {
        if (request.practiceMode() == PracticeMode.FLASHCARD) {
            throw new IllegalArgumentException("Flashcard mode does not require answer submission.");
        }

        AnswerSubmissionContext submissionContext = vocabularyRepository.findAnswerSubmissionContextById(
                request.vocabularyId())
                .orElseThrow(() -> new VocabularyNotFoundException(request.vocabularyId()));

        String correctAnswer = switch (request.practiceMode()) {
            case GUESS_WORD, MULTIPLE_CHOICE -> submissionContext.word();
            case REVERSE_MULTIPLE_CHOICE -> submissionContext.meaning();
            case FLASHCARD -> throw new IllegalArgumentException("Flashcard mode does not require answer submission.");
        };

        boolean correct = StudyTextUtils.normalizeAnswer(request.answer())
                .equals(StudyTextUtils.normalizeAnswer(correctAnswer));

        AppUser currentUser = currentUserProvider.getCurrentUser().orElse(null);
        if (currentUser != null && !guestProgressTokenCodec.isGuestToken(progressToken)) {
            return submitPersistentAnswer(currentUser, submissionContext, request, correct, correctAnswer);
        }

        StudyProgressState progressState = findProgress(progressToken);
        Map<UUID, StudyProgressSnapshot> progressByVocabularyId = new HashMap<>(loadProgressMap(progressState));
        StudyProgressSnapshot currentProgress = progressByVocabularyId.getOrDefault(
                submissionContext.vocabularyId(),
                new StudyProgressSnapshot(0, 0, false));
        StudyProgressSnapshot updatedProgress = new StudyProgressSnapshot(
                currentProgress.attemptCount() + 1,
                currentProgress.correctCount() + (correct ? 1 : 0),
                currentProgress.mastered() || correct);
        progressByVocabularyId.put(submissionContext.vocabularyId(), updatedProgress);

        StudyProgressState resolvedProgress = progressState.persisted()
                ? savePersistentProgress(progressState, submissionContext.vocabularyId(), request, correct, updatedProgress,
                        progressByVocabularyId)
                : reissueGuestProgress(progressState, progressState.displayName(), progressState.clientKey(),
                        progressByVocabularyId);

        ProgressSummaryDto studySetProgress = studyProgressSupport.buildProgressSummaryFromVocabularyIds(
                vocabularyRepository.findActiveIdsByStudySetIdOrderByDisplayOrderAscIdAsc(
                        submissionContext.studySetId()),
                progressByVocabularyId);
        ProgressSummaryDto unitProgress = studyProgressSupport.buildProgressSummaryFromVocabularyIds(
                vocabularyRepository.findActiveIdsByUnitIdOrderByDisplayOrderAscIdAsc(submissionContext.unitId()),
                progressByVocabularyId);
        StudyUnitActionDto nextUnit = findNextUnitAction(
                submissionContext.studySetId(),
                submissionContext.unitOrder(),
                submissionContext.unitId());
        boolean unitCompleted = studyProgressSupport.isUnitCompleted(unitProgress);
        StudyActivityDto studyActivity = correct && !unitCompleted
                ? buildStudyActivity(request.practiceMode(), loadUnitForCompletion(submissionContext.unitId()), progressByVocabularyId)
                : null;
        UnitCompletionDto unitCompletion = unitCompleted
                ? buildUnitCompletion(loadUnitForCompletion(submissionContext.unitId()), progressByVocabularyId)
                : null;

        return new AnswerResultDto(
                submissionContext.vocabularyId(),
                request.practiceMode(),
                correct,
                correctAnswer,
                unitCompleted,
                studySetProgress,
                unitProgress,
                studyMapper.toStudyProgressContextDto(resolvedProgress),
                studyActivity,
                unitCompletion);
    }

    private AnswerResultDto submitPersistentAnswer(
            AppUser currentUser,
            AnswerSubmissionContext submissionContext,
            SubmitAnswerRequest request,
            boolean correct,
            String correctAnswer) {
        AppUser userReference = entityManager.getReference(AppUser.class, currentUser.getId());
        StudyProgress progress = studyProgressRepository
                .findByUserIdAndVocabularyId(currentUser.getId(), submissionContext.vocabularyId())
                .orElseGet(() -> StudyProgress.builder()
                        .user(userReference)
                        .vocabulary(entityManager.getReference(Vocabulary.class, submissionContext.vocabularyId()))
                        .attemptCount(0)
                        .correctCount(0)
                        .mastered(false)
                        .build());

        int updatedAttemptCount = progress.getAttemptCount() + 1;
        int updatedCorrectCount = progress.getCorrectCount() + (correct ? 1 : 0);
        boolean updatedMastered = Boolean.TRUE.equals(progress.getMastered()) || correct;
        LocalDateTime now = LocalDateTime.now(AppTime.ZONE_ID);

        progress.setAttemptCount(updatedAttemptCount);
        progress.setCorrectCount(updatedCorrectCount);
        progress.setMastered(updatedMastered);
        progress.setLastMode(request.practiceMode());
        progress.setLastAnswer(request.answer());
        if (correct) {
            progress.setLastCorrectAt(now);
        }
        studyProgressRepository.save(progress);

        ProgressSummaryDto studySetProgress = loadPersistedStudySetProgressSummary(
                currentUser.getId(),
                submissionContext.studySetId());
        ProgressSummaryDto unitProgress = loadPersistedUnitProgressSummary(
                currentUser.getId(),
                submissionContext.unitId());
        StudyUnitActionDto nextUnit = findNextUnitAction(
                submissionContext.studySetId(),
                submissionContext.unitOrder(),
                submissionContext.unitId());
        boolean unitCompleted = studyProgressSupport.isUnitCompleted(unitProgress);
        StudyActivityDto studyActivity = null;
        UnitCompletionDto unitCompletion = null;
        if (unitCompleted) {
            StudyUnit unit = loadUnitForCompletion(submissionContext.unitId());
            List<Vocabulary> studySetVocabularies = vocabularyRepository
                    .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(submissionContext.studySetId());
            List<Vocabulary> unitVocabularies = filterVocabulariesByUnitId(studySetVocabularies, submissionContext.unitId());
            unitCompletion = buildUnitCompletion(unit, unitVocabularies, unitProgress, studySetProgress, nextUnit);
        } else if (correct) {
            StudyUnit unit = loadUnitForCompletion(submissionContext.unitId());
            List<Vocabulary> studySetVocabularies = vocabularyRepository
                    .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(submissionContext.studySetId());
            List<Vocabulary> unitVocabularies = filterVocabulariesByUnitId(studySetVocabularies, submissionContext.unitId());
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId = loadPersistedProgressMap(
                    currentUser.getId(),
                    studySetVocabularies.stream().map(Vocabulary::getId).toList());
            studyActivity = buildStudyActivity(
                    request.practiceMode(),
                    unit,
                    studySetVocabularies,
                    unitVocabularies,
                    progressByVocabularyId);
        }

        return new AnswerResultDto(
                submissionContext.vocabularyId(),
                request.practiceMode(),
                correct,
                correctAnswer,
                unitCompleted,
                studySetProgress,
                unitProgress,
                persistedProgressContext(currentUser, now),
                studyActivity,
                unitCompletion);
    }

    private StudyActivityDto buildStudyActivity(
            PracticeMode mode,
            StudyUnit unit,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        List<Vocabulary> studySetVocabularies = vocabularyRepository
                .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(unit.getStudySet().getId());
        List<Vocabulary> unitVocabularies = filterVocabulariesByUnitId(studySetVocabularies, unit.getId());

        return buildStudyActivity(mode, unit, studySetVocabularies, unitVocabularies, progressByVocabularyId);
    }

    private StudyActivityDto buildStudyActivity(
            PracticeMode mode,
            StudyUnit unit,
            List<Vocabulary> studySetVocabularies,
            List<Vocabulary> unitVocabularies,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        StudySet studySet = unit.getStudySet();
        List<StudyItemDto> items = unitVocabularies.stream()
                .map(vocabulary -> studyItemFactory.toStudyItemDto(
                        mode,
                        vocabulary,
                        studySetVocabularies,
                        progressByVocabularyId.containsKey(vocabulary.getId())
                                && progressByVocabularyId.get(vocabulary.getId()).mastered()))
                .toList();

        return new StudyActivityDto(
                mode,
                studySet.getTitle(),
                unit.getTitle(),
                studyProgressSupport.buildProgressSummary(studySetVocabularies, progressByVocabularyId),
                studyProgressSupport.buildProgressSummary(unitVocabularies, progressByVocabularyId),
                items);
    }
    private StudyUnit loadUnitForCompletion(UUID unitId) {
        return studyUnitRepository.findById(unitId)
                .orElseThrow(() -> new StudyUnitNotFoundException(unitId));
    }

    private UnitCompletionDto buildUnitCompletion(
            StudyUnit unit,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        List<Vocabulary> studySetVocabularies = vocabularyRepository
                .findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(unit.getStudySet().getId());
        List<Vocabulary> unitVocabularies = filterVocabulariesByUnitId(studySetVocabularies, unit.getId());
        ProgressSummaryDto unitProgress = studyProgressSupport.buildProgressSummary(unitVocabularies, progressByVocabularyId);
        ProgressSummaryDto studySetProgress = studyProgressSupport.buildProgressSummary(studySetVocabularies,
                progressByVocabularyId);

        return buildUnitCompletion(
                unit,
                unitVocabularies,
                unitProgress,
                studySetProgress,
                findNextUnitAction(unit.getStudySet().getId(), unit.getUnitOrder(), unit.getId()));
    }

    private UnitCompletionDto buildUnitCompletion(
            StudyUnit unit,
            List<Vocabulary> unitVocabularies,
            ProgressSummaryDto unitProgress,
            ProgressSummaryDto studySetProgress,
            StudyUnitActionDto nextUnit) {
        StudySet studySet = unit.getStudySet();
        return new UnitCompletionDto(
                unitProgress,
                studySetProgress,
                nextUnit,
                unitVocabularies.stream().map(studyMapper::toWordReviewDto).toList());
    }

    private StudyProgressResolutionDto createOrReuseGuestProgress(ResolveStudyProgressRequest request) {
        StudyProgressState existingProgress = decodeGuestProgressOrNull(
                StringNormalizer.trimToNull(request.progressToken()));
        if (existingProgress != null) {
            StudyProgressState reusedProgress = guestProgressTokenCodec.reissue(
                    existingProgress,
                    resolveDisplayName(request.displayName(), existingProgress.displayName()),
                    resolveClientKey(request.clientKey(), existingProgress.clientKey()),
                    existingProgress.lastStudiedAt(),
                    existingProgress.progressByVocabularyId());
            return new StudyProgressResolutionDto(studyMapper.toStudyProgressContextDto(reusedProgress), false);
        }

        StudyProgressState createdProgress = guestProgressTokenCodec.create(
                StringNormalizer.trimToNull(request.displayName()),
                StringNormalizer.trimToNull(request.clientKey()));
        return new StudyProgressResolutionDto(studyMapper.toStudyProgressContextDto(createdProgress), true);
    }

    private StudyProgressResolutionDto resolveUserProgress(ResolveStudyProgressRequest request, AppUser currentUser) {
        boolean hadPersistedProgress = studyProgressRepository.countAllByUserId(currentUser.getId()) > 0;
        StudyProgressState guestProgress = decodeGuestProgressOrNull(
                StringNormalizer.trimToNull(request.progressToken()));

        if (guestProgress != null) {
            mergeGuestProgressIntoUser(currentUser, guestProgress);
        }

        StudyProgressState persistedProgress = buildPersistedProgressState(
                currentUser,
                resolveDisplayName(request.displayName(),
                        guestProgress == null ? currentUser.getFullName() : guestProgress.displayName()),
                resolveClientKey(request.clientKey(), guestProgress == null ? null : guestProgress.clientKey()),
                guestProgress != null && guestProgress.lastStudiedAt() != null
                        ? guestProgress.lastStudiedAt()
                        : resolvePersistedLastStudiedAt(currentUser.getId()));

        return new StudyProgressResolutionDto(
                studyMapper.toStudyProgressContextDto(persistedProgress),
                !hadPersistedProgress);
    }

    private StudyProgressState savePersistentProgress(
            StudyProgressState progressState,
            UUID vocabularyId,
            SubmitAnswerRequest request,
            boolean correct,
            StudyProgressSnapshot updatedProgress,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        AppUser currentUser = currentUserProvider.getRequiredUser();
        AppUser userReference = entityManager.getReference(AppUser.class, currentUser.getId());

        StudyProgress progress = studyProgressRepository
                .findByUserIdAndVocabularyId(progressState.userId(), vocabularyId)
                .orElseGet(() -> StudyProgress.builder()
                        .user(userReference)
                        .vocabulary(entityManager.getReference(Vocabulary.class, vocabularyId))
                        .attemptCount(0)
                        .correctCount(0)
                        .mastered(false)
                        .build());

        progress.setAttemptCount(updatedProgress.attemptCount());
        progress.setCorrectCount(updatedProgress.correctCount());
        progress.setMastered(updatedProgress.mastered());
        progress.setLastMode(request.practiceMode());
        progress.setLastAnswer(request.answer());
        if (correct) {
            progress.setLastCorrectAt(LocalDateTime.now(AppTime.ZONE_ID));
        }
        studyProgressRepository.save(progress);

        return buildPersistedProgressState(
                currentUser,
                progressState.displayName(),
                progressState.clientKey(),
                LocalDateTime.now(AppTime.ZONE_ID),
                progressByVocabularyId);
    }

    private void mergeGuestProgressIntoUser(AppUser user, StudyProgressState guestProgress) {
        if (guestProgress.progressByVocabularyId().isEmpty()) {
            return;
        }
        AppUser userReference = entityManager.getReference(AppUser.class, user.getId());

        Map<UUID, Vocabulary> vocabularyById = new HashMap<>();
        for (Vocabulary vocabulary : vocabularyRepository.findAllById(guestProgress.progressByVocabularyId().keySet())) {
            vocabularyById.put(vocabulary.getId(), vocabulary);
        }

        Map<UUID, StudyProgress> existingProgressByVocabularyId = new HashMap<>();
        for (StudyProgress progress : studyProgressRepository.findByUserId(user.getId())) {
            existingProgressByVocabularyId.put(progress.getVocabulary().getId(), progress);
        }

        List<StudyProgress> progressToSave = new ArrayList<>();
        for (Map.Entry<UUID, StudyProgressSnapshot> entry : guestProgress.progressByVocabularyId().entrySet()) {
            Vocabulary vocabulary = vocabularyById.get(entry.getKey());
            if (vocabulary == null) {
                continue;
            }

            StudyProgressSnapshot snapshot = entry.getValue();
            StudyProgress progress = existingProgressByVocabularyId.get(entry.getKey());
            if (progress == null) {
                progress = StudyProgress.builder()
                        .user(userReference)
                        .vocabulary(vocabulary)
                        .attemptCount(snapshot.attemptCount())
                        .correctCount(snapshot.correctCount())
                        .mastered(snapshot.mastered())
                        .build();
            } else {
                progress.setAttemptCount(Math.max(progress.getAttemptCount(), snapshot.attemptCount()));
                progress.setCorrectCount(Math.max(progress.getCorrectCount(), snapshot.correctCount()));
                progress.setMastered(Boolean.TRUE.equals(progress.getMastered()) || snapshot.mastered());
            }

            if (snapshot.mastered() && progress.getLastCorrectAt() == null) {
                progress.setLastCorrectAt(
                        guestProgress.lastStudiedAt() != null ? guestProgress.lastStudiedAt()
                                : LocalDateTime.now(AppTime.ZONE_ID));
            }
            progressToSave.add(progress);
        }

        if (!progressToSave.isEmpty()) {
            studyProgressRepository.saveAll(progressToSave);
        }
    }

    private StudyProgressState reissueGuestProgress(
            StudyProgressState progressState,
            String displayName,
            String clientKey,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        return guestProgressTokenCodec.reissue(
                progressState,
                displayName,
                clientKey,
                LocalDateTime.now(AppTime.ZONE_ID),
                progressByVocabularyId);
    }

    private StudyProgressState refreshPersistedProgress(
            StudyProgressState progressState,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        AppUser currentUser = currentUserProvider.getRequiredUser();
        return buildPersistedProgressState(
                currentUser,
                progressState.displayName(),
                progressState.clientKey(),
                LocalDateTime.now(AppTime.ZONE_ID),
                progressByVocabularyId);
    }

    private StudySet findPublishedStudySet(String slug) {
        return studySetRepository.findBySlugAndStatus(slug, StudySetStatus.PUBLISHED)
                .orElseThrow(() -> new StudySetNotFoundException(slug));
    }

    private StudyProgressState findProgressIfPresent(String progressToken) {
        if (!StringUtils.hasText(progressToken) && currentUserProvider.getCurrentUser().isEmpty()) {
            return null;
        }
        return findProgress(progressToken);
    }

    private StudyProgressState findProgress(String progressToken) {
        AppUser currentUser = currentUserProvider.getCurrentUser().orElse(null);
        if (currentUser != null && !guestProgressTokenCodec.isGuestToken(progressToken)) {
            return buildPersistedProgressState(
                    currentUser,
                    currentUser.getFullName(),
                    null,
                    resolvePersistedLastStudiedAt(currentUser.getId()));
        }

        if (!StringUtils.hasText(progressToken)) {
            throw new StudyProgressNotFoundException(progressToken);
        }

        return guestProgressTokenCodec.decode(progressToken);
    }

    private StudyProgressState decodeGuestProgressOrNull(String progressToken) {
        if (!guestProgressTokenCodec.isGuestToken(progressToken)) {
            return null;
        }
        try {
            return guestProgressTokenCodec.decode(progressToken);
        } catch (StudyProgressNotFoundException exception) {
            return null;
        }
    }

    private String resolveDisplayName(String requestedDisplayName, String fallbackDisplayName) {
        String displayName = StringNormalizer.trimToNull(requestedDisplayName);
        return StringUtils.hasText(displayName) ? displayName : fallbackDisplayName;
    }

    private String resolveClientKey(String requestedClientKey, String fallbackClientKey) {
        String clientKey = StringNormalizer.trimToNull(requestedClientKey);
        return StringUtils.hasText(clientKey) ? clientKey : fallbackClientKey;
    }

    private Map<UUID, StudyProgressSnapshot> loadProgressMap(StudyProgressState progressState) {
        if (progressState == null) {
            return Map.of();
        }
        if (progressState.progressByVocabularyId() != null) {
            return progressState.progressByVocabularyId();
        }
        if (progressState.persisted() && progressState.userId() != null) {
            return loadPersistedProgressMap(progressState.userId());
        }
        return Map.of();
    }

    private Map<UUID, StudyProgressSnapshot> loadProgressMapForVocabularyIds(
            StudyProgressState progressState,
            List<UUID> vocabularyIds) {
        if (progressState == null || vocabularyIds.isEmpty()) {
            return Map.of();
        }
        if (progressState.progressByVocabularyId() != null) {
            Map<UUID, StudyProgressSnapshot> filtered = new HashMap<>();
            for (UUID vocabularyId : vocabularyIds) {
                StudyProgressSnapshot snapshot = progressState.progressByVocabularyId().get(vocabularyId);
                if (snapshot != null) {
                    filtered.put(vocabularyId, snapshot);
                }
            }
            return filtered;
        }
        if (!progressState.persisted() || progressState.userId() == null) {
            return Map.of();
        }
        return loadPersistedProgressMap(progressState.userId(), vocabularyIds);
    }

    private Map<UUID, List<Vocabulary>> groupVocabulariesByUnitId(List<Vocabulary> vocabularies) {
        Map<UUID, List<Vocabulary>> grouped = new HashMap<>();
        for (Vocabulary vocabulary : vocabularies) {
            grouped.computeIfAbsent(vocabulary.getUnit().getId(), ignored -> new ArrayList<>())
                    .add(vocabulary);
        }
        return grouped;
    }

    private List<Vocabulary> filterVocabulariesByUnitId(List<Vocabulary> vocabularies, UUID unitId) {
        return vocabularies.stream()
                .filter(vocabulary -> vocabulary.getUnit().getId().equals(unitId))
                .toList();
    }

    private ProgressSummaryDto loadPersistedStudySetProgressSummary(UUID userId, UUID studySetId) {
        return studyProgressSupport.toSummary(
                vocabularyRepository.summarizeStudySetProgress(userId, studySetId));
    }

    private ProgressSummaryDto loadPersistedUnitProgressSummary(UUID userId, UUID unitId) {
        return studyProgressSupport.toSummary(
                vocabularyRepository.summarizeUnitProgress(userId, unitId));
    }

    private StudyUnitProgressDto toStudyUnitProgressDto(
            StudyUnit unit,
            List<Vocabulary> vocabularies,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        ProgressSummaryDto unitProgress = studyProgressSupport.buildProgressSummary(vocabularies, progressByVocabularyId);
        String status;
        if (studyProgressSupport.isUnitCompleted(unitProgress)) {
            status = "COMPLETED";
        } else if (unitProgress.learnedWords() > 0) {
            status = "IN_PROGRESS";
        } else {
            status = "AVAILABLE";
        }

        return new StudyUnitProgressDto(
                unit.getId(),
                unit.getTitle(),
                unit.getUnitOrder(),
                unitProgress.totalWords(),
                unitProgress.learnedWords(),
                unitProgress.masteredWords(),
                unitProgress.percentage(),
                status);
    }

    private StudyUnitActionDto findNextUnitAction(UUID studySetId, Integer unitOrder, UUID unitId) {
        return studyUnitRepository.findNextActiveUnits(
                studySetId,
                unitOrder,
                unitId,
                PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(nextUnit -> new StudyUnitActionDto(nextUnit.getId(), nextUnit.getTitle(), nextUnit.getUnitOrder()))
                .orElse(null);
    }

    private StudyProgressContextDto persistedProgressContext(AppUser user, LocalDateTime lastStudiedAt) {
        return new StudyProgressContextDto(
                userProgressToken(user.getId()),
                user.getFullName(),
                true);
    }

    private StudySetCardAggregate mergeStudySetCardProgress(
            StudySetCardAggregate aggregate,
            ProgressSummaryAggregate progress) {
        return new StudySetCardAggregate(
                aggregate.id(),
                aggregate.title(),
                aggregate.slug(),
                aggregate.description(),
                aggregate.thumbnailUrl(),
                aggregate.status(),
                aggregate.totalUnits(),
                aggregate.totalWords(),
                progress.learnedWords(),
                progress.masteredWords());
    }

    private StudySetCardDto toStudySetCardDto(StudySetCardAggregate aggregate) {
        ProgressSummaryDto progress = studyProgressSupport.toSummary(new ProgressSummaryAggregate(
                aggregate.totalWords(),
                aggregate.learnedWords(),
                aggregate.masteredWords()));
        return new StudySetCardDto(
                aggregate.id(),
                aggregate.title(),
                aggregate.slug(),
                aggregate.description(),
                studyProgressSupport.resolveStudySetLearningStatus(progress),
                Math.toIntExact(aggregate.totalUnits()),
                Math.toIntExact(aggregate.totalWords()));
    }

    private Pageable normalizePageable(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    private StudyProgressState buildPersistedProgressState(
            AppUser user,
            String displayName,
            String clientKey,
            LocalDateTime lastStudiedAt) {
        return buildPersistedProgressState(
                user,
                displayName,
                clientKey,
                lastStudiedAt,
                null);
    }

    private StudyProgressState buildPersistedProgressState(
            AppUser user,
            String displayName,
            String clientKey,
            LocalDateTime lastStudiedAt,
            Map<UUID, StudyProgressSnapshot> progressByVocabularyId) {
        return new StudyProgressState(
                userProgressToken(user.getId()),
                true,
                user.getId(),
                displayName,
                clientKey,
                user.getCreatedAt(),
                lastStudiedAt,
                progressByVocabularyId == null ? null : Map.copyOf(progressByVocabularyId));
    }

    private Map<UUID, StudyProgressSnapshot> loadPersistedProgressMap(UUID userId) {
        Map<UUID, StudyProgressSnapshot> map = new HashMap<>();
        for (StudyProgress progress : studyProgressRepository.findByUserId(userId)) {
            map.put(
                    progress.getVocabulary().getId(),
                    new StudyProgressSnapshot(
                            progress.getAttemptCount(),
                            progress.getCorrectCount(),
                            Boolean.TRUE.equals(progress.getMastered())));
        }
        return map;
    }

    private Map<UUID, StudyProgressSnapshot> loadPersistedProgressMap(UUID userId, List<UUID> vocabularyIds) {
        Map<UUID, StudyProgressSnapshot> map = new HashMap<>();
        for (StudyProgress progress : studyProgressRepository.findByUserIdAndVocabularyIdIn(userId, vocabularyIds)) {
            map.put(
                    progress.getVocabulary().getId(),
                    new StudyProgressSnapshot(
                            progress.getAttemptCount(),
                            progress.getCorrectCount(),
                            Boolean.TRUE.equals(progress.getMastered())));
        }
        return map;
    }

    private LocalDateTime resolvePersistedLastStudiedAt(UUID userId) {
        return studyProgressRepository.findRecentByUserIdOrderByUpdatedAtDesc(userId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(StudyProgress::getUpdatedAt)
                .orElse(null);
    }

    private String userProgressToken(UUID userId) {
        return USER_PROGRESS_TOKEN_PREFIX + "." + userId;
    }
}
