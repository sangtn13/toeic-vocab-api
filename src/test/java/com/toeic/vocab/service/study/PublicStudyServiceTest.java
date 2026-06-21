package com.toeic.vocab.service.study;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toeic.vocab.enums.PartOfSpeech;
import com.toeic.vocab.enums.PracticeMode;
import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.enums.VocabularyLevel;
import com.toeic.vocab.mapper.StudyMapper;
import com.toeic.vocab.model.progress.StudyProgress;
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
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PublicStudyServiceTest {

    private static final String TEST_JWT_SECRET =
            "g0qlJwfjNUHoDn4YOos9jItP5/srQ3QXbPwJjzQFfyTTKpVH+NRLFSGgErlYp3KnThZ+tXBmHms5ysdmk8WL6g==";

    @Mock
    private StudySetRepository studySetRepository;

    @Mock
    private StudyUnitRepository studyUnitRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Mock
    private StudyProgressRepository studyProgressRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private EntityManager entityManager;

    @Test
    void shouldMarkCorrectAnswerAsMastered() {
        UUID studySetId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID studyUnitId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID nextUnitId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID vocabularyId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        UUID currentUserId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        StudySet studySet = StudySet.builder()
                .title("600 TOEIC")
                .slug("600-toeic")
                .status(StudySetStatus.PUBLISHED)
                .displayOrder(1)
                .build();
        studySet.setId(studySetId);

        StudyUnit studyUnit = StudyUnit.builder()
                .studySet(studySet)
                .title("Unit 1")
                .unitOrder(1)
                .active(true)
                .build();
        studyUnit.setId(studyUnitId);

        StudyUnit nextUnit = StudyUnit.builder()
                .studySet(studySet)
                .title("Unit 2")
                .unitOrder(2)
                .active(true)
                .build();
        nextUnit.setId(nextUnitId);

        Vocabulary vocabulary = Vocabulary.builder()
                .unit(studyUnit)
                .word("abide by")
                .meaning("tuan thu")
                .partOfSpeech(PartOfSpeech.PHRASAL_VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary.setId(vocabularyId);

        AppUser currentUser = AppUser.builder()
                .email("learner@test.local")
                .passwordHash("secret")
                .fullName("Learner")
                .build();
        currentUser.setId(currentUserId);

        when(vocabularyRepository.findAnswerSubmissionContextById(vocabularyId))
                .thenReturn(Optional.of(new AnswerSubmissionContext(
                        vocabularyId,
                        "abide by",
                        "tuan thu",
                        studyUnitId,
                        1,
                        studySetId)));
        when(studyProgressRepository.findByUserIdAndVocabularyId(currentUserId, vocabularyId)).thenReturn(Optional.empty());
        when(entityManager.getReference(AppUser.class, currentUserId)).thenReturn(currentUser);
        when(entityManager.getReference(Vocabulary.class, vocabularyId)).thenReturn(vocabulary);
        when(vocabularyRepository.summarizeStudySetProgress(currentUserId, studySetId))
                .thenReturn(new ProgressSummaryAggregate(1, 1, 1));
        when(vocabularyRepository.summarizeUnitProgress(currentUserId, studyUnitId))
                .thenReturn(new ProgressSummaryAggregate(1, 1, 1));
        when(studyUnitRepository.findNextActiveUnits(studySetId, 1, studyUnitId, Pageable.ofSize(1)))
                .thenReturn(List.of(nextUnit));
        when(studyUnitRepository.findById(studyUnitId)).thenReturn(Optional.of(studyUnit));
        when(vocabularyRepository.findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(studySetId))
                .thenReturn(List.of(vocabulary));
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(currentUser));

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                (GuestProgressTokenCodec) guestProgressCodec(),
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.submitAnswer("legacy-progress-token", new SubmitAnswerRequest(
                vocabularyId,
                PracticeMode.GUESS_WORD,
                "abide by"));

        assertThat(result.correct()).isTrue();
        assertThat(result.unitCompleted()).isTrue();
        assertThat(result.correctAnswer()).isEqualTo("abide by");
        assertThat(result.unitCompletion()).isNotNull();
        assertThat(result.unitCompletion().nextUnit()).isNotNull();
        assertThat(result.unitCompletion().nextUnit().unitId()).isEqualTo(nextUnitId);
        assertThat(result.unitCompletion().vocabularies()).hasSize(1);
        assertThat(result.unitProgress().masteredWords()).isEqualTo(1);
        assertThat(result.progress().persistent()).isTrue();
        assertThat(result.progress().progressToken()).isEqualTo("usr." + currentUserId);
    }

    @Test
    void shouldReuseGuestProgressByToken() {
        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        GuestProgressTokenCodec codec = (GuestProgressTokenCodec) guestProgressCodec();
        String existingToken = codec.create("Guest", "browser-1").progressToken();

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                codec,
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.resolveStudyProgress(new ResolveStudyProgressRequest("Guest", existingToken, "browser-1"));

        assertThat(result.created()).isFalse();
        assertThat(result.progress().progressToken()).isNotBlank();
        assertThat(result.progress().displayName()).isEqualTo("Guest");
        assertThat(result.progress().persistent()).isFalse();
        verify(studyProgressRepository, never()).save(any());
    }

    @Test
    void shouldKeepGuestProgressStatelessWhenSubmittingAnswer() {
        UUID studySetId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID studyUnitId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        UUID vocabularyId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        StudySet studySet = StudySet.builder()
                .title("600 TOEIC")
                .slug("600-toeic")
                .status(StudySetStatus.PUBLISHED)
                .displayOrder(1)
                .build();
        studySet.setId(studySetId);

        StudyUnit studyUnit = StudyUnit.builder()
                .studySet(studySet)
                .title("Unit 1")
                .unitOrder(1)
                .active(true)
                .build();
        studyUnit.setId(studyUnitId);

        Vocabulary vocabulary = Vocabulary.builder()
                .unit(studyUnit)
                .word("abide by")
                .meaning("tuan thu")
                .partOfSpeech(PartOfSpeech.PHRASAL_VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary.setId(vocabularyId);

        GuestProgressTokenCodec codec = (GuestProgressTokenCodec) guestProgressCodec();
        String guestToken = codec.create("Guest", "browser-1").progressToken();

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(vocabularyRepository.findAnswerSubmissionContextById(vocabularyId))
                .thenReturn(Optional.of(new AnswerSubmissionContext(
                        vocabularyId,
                        "abide by",
                        "tuan thu",
                        studyUnitId,
                        1,
                        studySetId)));
        when(vocabularyRepository.findActiveIdsByStudySetIdOrderByDisplayOrderAscIdAsc(studySetId))
                .thenReturn(List.of(vocabularyId));
        when(vocabularyRepository.findActiveIdsByUnitIdOrderByDisplayOrderAscIdAsc(studyUnitId))
                .thenReturn(List.of(vocabularyId));
        when(studyUnitRepository.findNextActiveUnits(studySetId, 1, studyUnitId, Pageable.ofSize(1)))
                .thenReturn(List.of());
        when(studyUnitRepository.findById(studyUnitId)).thenReturn(Optional.of(studyUnit));
        when(vocabularyRepository.findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(studySetId))
                .thenReturn(List.of(vocabulary));

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                codec,
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.submitAnswer(guestToken, new SubmitAnswerRequest(
                vocabularyId,
                PracticeMode.GUESS_WORD,
                "abide by"));

        assertThat(result.correct()).isTrue();
        assertThat(result.unitCompletion()).isNotNull();
        assertThat(result.progress().persistent()).isFalse();
        assertThat(result.progress().progressToken()).isNotEqualTo(guestToken);
        verify(studyProgressRepository, never()).save(any());
    }

    @Test
    void shouldEmbedStudyActivityWhenCorrectAnswerDoesNotCompleteGuestUnit() {
        UUID studySetId = UUID.fromString("10101010-1010-1010-1010-101010101010");
        UUID studyUnitId = UUID.fromString("20202020-2020-2020-2020-202020202020");
        UUID vocabularyId = UUID.fromString("30303030-3030-3030-3030-303030303030");
        UUID vocabulary2Id = UUID.fromString("40404040-4040-4040-4040-404040404040");
        StudySet studySet = StudySet.builder()
                .title("600 TOEIC")
                .slug("600-toeic")
                .status(StudySetStatus.PUBLISHED)
                .displayOrder(1)
                .build();
        studySet.setId(studySetId);

        StudyUnit studyUnit = StudyUnit.builder()
                .studySet(studySet)
                .title("Unit 1")
                .unitOrder(1)
                .active(true)
                .build();
        studyUnit.setId(studyUnitId);

        Vocabulary vocabulary = Vocabulary.builder()
                .unit(studyUnit)
                .word("abide by")
                .meaning("tuan thu")
                .partOfSpeech(PartOfSpeech.PHRASAL_VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary.setId(vocabularyId);

        Vocabulary vocabulary2 = Vocabulary.builder()
                .unit(studyUnit)
                .word("allocate")
                .meaning("phan bo")
                .partOfSpeech(PartOfSpeech.VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(2)
                .active(true)
                .build();
        vocabulary2.setId(vocabulary2Id);

        GuestProgressTokenCodec codec = (GuestProgressTokenCodec) guestProgressCodec();
        String guestToken = codec.create("Guest", "browser-1").progressToken();

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.empty());
        when(vocabularyRepository.findAnswerSubmissionContextById(vocabularyId))
                .thenReturn(Optional.of(new AnswerSubmissionContext(
                        vocabularyId,
                        "abide by",
                        "tuan thu",
                        studyUnitId,
                        1,
                        studySetId)));
        when(vocabularyRepository.findActiveIdsByStudySetIdOrderByDisplayOrderAscIdAsc(studySetId))
                .thenReturn(List.of(vocabularyId, vocabulary2Id));
        when(vocabularyRepository.findActiveIdsByUnitIdOrderByDisplayOrderAscIdAsc(studyUnitId))
                .thenReturn(List.of(vocabularyId, vocabulary2Id));
        when(vocabularyRepository.findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(studySetId))
                .thenReturn(List.of(vocabulary, vocabulary2));
        when(studyUnitRepository.findNextActiveUnits(studySetId, 1, studyUnitId, Pageable.ofSize(1)))
                .thenReturn(List.of());
        when(studyUnitRepository.findById(studyUnitId)).thenReturn(Optional.of(studyUnit));

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                codec,
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.submitAnswer(guestToken, new SubmitAnswerRequest(
                vocabularyId,
                PracticeMode.GUESS_WORD,
                "abide by"));

        assertThat(result.correct()).isTrue();
        assertThat(result.unitCompleted()).isFalse();
        assertThat(result.unitCompletion()).isNull();
        assertThat(result.studyActivity()).isNotNull();
        assertThat(result.studyActivity().mode()).isEqualTo(PracticeMode.GUESS_WORD);
        assertThat(result.studyActivity().items()).hasSize(2);
        assertThat(result.studyActivity().unitProgress().learnedWords()).isEqualTo(1);
        assertThat(result.progress().persistent()).isFalse();
        assertThat(result.progress().progressToken()).isNotEqualTo(guestToken);
    }
    @Test
    void shouldLoadStudySetCardsFromAggregateForPersistedUser() {
        UUID currentUserId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        AppUser currentUser = AppUser.builder()
                .email("learner@test.local")
                .passwordHash("secret")
                .fullName("Learner")
                .build();
        currentUser.setId(currentUserId);
        PageRequest pageable = PageRequest.of(0, 10);

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(studySetRepository.findStudySetCardProgressByStatusAndUserId(
                StudySetStatus.PUBLISHED,
                currentUserId,
                pageable))
                .thenReturn(new PageImpl<>(List.of(new StudySetCardAggregate(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "600 TOEIC",
                        "600-toeic",
                        "Description",
                        "thumb.png",
                        StudySetStatus.PUBLISHED,
                        2,
                        10,
                        3,
                        1)), pageable, 1));

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                (GuestProgressTokenCodec) guestProgressCodec(),
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.getPublishedStudySets("usr." + currentUserId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).totalUnits()).isEqualTo(2);
        assertThat(result.getContent().get(0).totalWords()).isEqualTo(10);
        assertThat(result.getContent().get(0).learningStatus())
                .isEqualTo(com.toeic.vocab.enums.StudySetLearningStatus.IN_PROGRESS);
        verify(studySetRepository, never()).findStudySetCardStatsByStatus(StudySetStatus.PUBLISHED, pageable);
        verify(studyProgressRepository, never()).findByUserId(any());
    }

    @Test
    void shouldKeepLaterPageUnitsAvailableWhenPreviousPageIsIncomplete() {
        UUID studySetId = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
        UUID currentUserId = UUID.fromString("bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb");
        UUID unit1Id = UUID.fromString("cccccccc-3333-3333-3333-cccccccccccc");
        UUID unit2Id = UUID.fromString("dddddddd-4444-4444-4444-dddddddddddd");
        UUID unit3Id = UUID.fromString("eeeeeeee-5555-5555-5555-eeeeeeeeeeee");
        UUID vocabulary1Id = UUID.fromString("11111111-6666-6666-6666-111111111111");
        UUID vocabulary2Id = UUID.fromString("22222222-7777-7777-7777-222222222222");
        UUID vocabulary3Id = UUID.fromString("33333333-8888-8888-8888-333333333333");

        StudySet studySet = StudySet.builder()
                .title("600 TOEIC")
                .slug("600-toeic")
                .status(StudySetStatus.PUBLISHED)
                .displayOrder(1)
                .build();
        studySet.setId(studySetId);

        StudyUnit unit1 = StudyUnit.builder().studySet(studySet).title("Unit 1").unitOrder(1).active(true).build();
        unit1.setId(unit1Id);
        StudyUnit unit2 = StudyUnit.builder().studySet(studySet).title("Unit 2").unitOrder(2).active(true).build();
        unit2.setId(unit2Id);
        StudyUnit unit3 = StudyUnit.builder().studySet(studySet).title("Unit 3").unitOrder(3).active(true).build();
        unit3.setId(unit3Id);

        Vocabulary vocabulary1 = Vocabulary.builder()
                .unit(unit1)
                .word("abide by")
                .meaning("tuan thu")
                .partOfSpeech(PartOfSpeech.PHRASAL_VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary1.setId(vocabulary1Id);

        Vocabulary vocabulary2 = Vocabulary.builder()
                .unit(unit2)
                .word("allocate")
                .meaning("phan bo")
                .partOfSpeech(PartOfSpeech.VERB)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary2.setId(vocabulary2Id);

        Vocabulary vocabulary3 = Vocabulary.builder()
                .unit(unit3)
                .word("agenda")
                .meaning("chuong trinh nghi su")
                .partOfSpeech(PartOfSpeech.NOUN)
                .difficultyLevel(VocabularyLevel.CORE)
                .displayOrder(1)
                .active(true)
                .build();
        vocabulary3.setId(vocabulary3Id);

        AppUser currentUser = AppUser.builder()
                .email("learner@test.local")
                .passwordHash("secret")
                .fullName("Learner")
                .build();
        currentUser.setId(currentUserId);

        StudyProgress progress = StudyProgress.builder()
                .user(currentUser)
                .vocabulary(vocabulary1)
                .attemptCount(1)
                .correctCount(1)
                .mastered(true)
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(studySetRepository.findBySlugAndStatus("600-toeic", StudySetStatus.PUBLISHED))
                .thenReturn(Optional.of(studySet));
        when(studyProgressRepository.findRecentByUserIdOrderByUpdatedAtDesc(currentUserId, PageRequest.of(0, 1)))
                .thenReturn(List.of());
        when(studyUnitRepository.findPublicUnitsByStudySetId(studySetId, PageRequest.of(1, 2)))
                .thenReturn(new PageImpl<>(List.of(unit3), PageRequest.of(1, 2), 3));
        when(vocabularyRepository.findByUnitIdInAndActiveTrueOrderByUnitOrderAscDisplayOrderAscIdAsc(
                List.of(unit3Id)))
                .thenReturn(List.of(vocabulary3));
        when(studyProgressRepository.findByUserIdAndVocabularyIdIn(
                currentUserId,
                List.of(vocabulary3Id)))
                .thenReturn(List.of());

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                (GuestProgressTokenCodec) guestProgressCodec(),
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.getStudySetUnits("600-toeic", "usr." + currentUserId, PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(unit3Id);
        assertThat(result.getContent().get(0).status()).isEqualTo("AVAILABLE");
    }

    @Test
    void shouldResolveUserProgressWithoutLoadingFullProgressMap() {
        UUID currentUserId = UUID.fromString("12121212-1212-1212-1212-121212121212");
        AppUser currentUser = AppUser.builder()
                .email("learner@test.local")
                .passwordHash("secret")
                .fullName("Learner")
                .build();
        currentUser.setId(currentUserId);

        when(currentUserProvider.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(studyProgressRepository.countAllByUserId(currentUserId)).thenReturn(0L);
        when(studyProgressRepository.findRecentByUserIdOrderByUpdatedAtDesc(currentUserId, Pageable.ofSize(1)))
                .thenReturn(List.of());

        PublicStudyService service = new PublicStudyServiceImpl(
                studySetRepository,
                studyUnitRepository,
                vocabularyRepository,
                studyProgressRepository,
                currentUserProvider,
                (GuestProgressTokenCodec) guestProgressCodec(),
                Mappers.getMapper(StudyMapper.class),
                new StudyProgressSupport(),
                new StudyItemFactory(),
                entityManager);

        var result = service.resolveStudyProgress(new ResolveStudyProgressRequest(null, null, null));

        assertThat(result.progress().persistent()).isTrue();
        assertThat(result.created()).isTrue();
        verify(studyProgressRepository, never()).findByUserId(currentUserId);
    }

    private Object guestProgressCodec() {
        GuestProgressTokenCodec codec = new GuestProgressTokenCodec(new ObjectMapper());
        ReflectionTestUtils.setField(codec, "jwtSecret", TEST_JWT_SECRET);
        ReflectionTestUtils.setField(codec, "guestProgressExpirationMs", 2_592_000_000L);
        return codec;
    }
}
