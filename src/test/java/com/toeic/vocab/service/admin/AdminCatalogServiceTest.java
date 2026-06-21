package com.toeic.vocab.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.mapper.AdminCatalogMapper;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.repository.studyset.StudySetRepository;
import com.toeic.vocab.repository.studyunit.StudyUnitRepository;
import com.toeic.vocab.repository.vocabulary.VocabularyRepository;
import com.toeic.vocab.request.admin.UpsertStudySetRequest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminCatalogServiceTest {

    @Mock
    private StudySetRepository studySetRepository;

    @Mock
    private StudyUnitRepository studyUnitRepository;

    @Mock
    private VocabularyRepository vocabularyRepository;

    @Test
    void shouldCreateStudySetWithGeneratedSlug() {
        UUID studySetId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(studySetRepository.existsBySlugIgnoreCase("600-tu-vung-toeic")).thenReturn(false);

        StudySet savedStudySet = StudySet.builder()
            .title("600 Từ Vựng TOEIC")
            .slug("600-tu-vung-toeic")
            .description("Bo the hoc tu vung")
            .thumbnailUrl("https://example.com/thumb.png")
            .displayOrder(1)
            .status(StudySetStatus.PUBLISHED)
            .build();
        savedStudySet.setId(studySetId);

        when(studySetRepository.save(any(StudySet.class))).thenReturn(savedStudySet);

        AdminCatalogService service = new AdminCatalogServiceImpl(
            studySetRepository,
            studyUnitRepository,
            vocabularyRepository,
            Mappers.getMapper(AdminCatalogMapper.class),
            new StudySetSlugGenerator(studySetRepository)
        );
        var result = service.createStudySet(new UpsertStudySetRequest(
            "600 Từ Vựng TOEIC",
            "Bo the hoc tu vung",
            "https://example.com/thumb.png",
            1,
            StudySetStatus.PUBLISHED
        ));

        assertThat(result.id()).isEqualTo(studySetId);
        assertThat(result.slug()).isEqualTo("600-tu-vung-toeic");
        assertThat(result.title()).isEqualTo("600 Từ Vựng TOEIC");
        assertThat(result.status()).isEqualTo(StudySetStatus.PUBLISHED);
    }

    @Test
    void shouldCreateStudySetWithIncrementedSlugWhenTitleAlreadyExists() {
        UUID studySetId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        when(studySetRepository.existsBySlugIgnoreCase("toeic-basic")).thenReturn(true);
        when(studySetRepository.existsBySlugIgnoreCase("toeic-basic-2")).thenReturn(false);

        StudySet savedStudySet = StudySet.builder()
            .title("TOEIC Basic")
            .slug("toeic-basic-2")
            .description("Duplicated title")
            .thumbnailUrl(null)
            .displayOrder(2)
            .status(StudySetStatus.DRAFT)
            .build();
        savedStudySet.setId(studySetId);

        when(studySetRepository.save(any(StudySet.class))).thenReturn(savedStudySet);

        AdminCatalogService service = new AdminCatalogServiceImpl(
            studySetRepository,
            studyUnitRepository,
            vocabularyRepository,
            Mappers.getMapper(AdminCatalogMapper.class),
            new StudySetSlugGenerator(studySetRepository)
        );
        var result = service.createStudySet(new UpsertStudySetRequest(
            "TOEIC Basic",
            "Duplicated title",
            null,
            2,
            StudySetStatus.DRAFT
        ));

        assertThat(result.id()).isEqualTo(studySetId);
        assertThat(result.slug()).isEqualTo("toeic-basic-2");

        verify(studySetRepository).existsBySlugIgnoreCase("toeic-basic");
        verify(studySetRepository).existsBySlugIgnoreCase("toeic-basic-2");
    }

    @Test
    void shouldUpdateStudySetAndRegenerateSlugFromTitle() {
        UUID studySetId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        StudySet existingStudySet = StudySet.builder()
            .title("Old Title")
            .slug("old-title")
            .description("Old description")
            .thumbnailUrl("https://example.com/old.png")
            .displayOrder(1)
            .status(StudySetStatus.DRAFT)
            .build();
        existingStudySet.setId(studySetId);

        when(studySetRepository.findById(studySetId)).thenReturn(Optional.of(existingStudySet));
        when(studySetRepository.existsBySlugIgnoreCaseAndIdNot("new-toeic-title", studySetId)).thenReturn(false);
        when(studySetRepository.save(any(StudySet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminCatalogService service = new AdminCatalogServiceImpl(
            studySetRepository,
            studyUnitRepository,
            vocabularyRepository,
            Mappers.getMapper(AdminCatalogMapper.class),
            new StudySetSlugGenerator(studySetRepository)
        );
        var result = service.updateStudySet(studySetId, new UpsertStudySetRequest(
            "New TOEIC Title",
            "New description",
            "https://example.com/new.png",
            3,
            StudySetStatus.PUBLISHED
        ));

        assertThat(result.id()).isEqualTo(studySetId);
        assertThat(result.title()).isEqualTo("New TOEIC Title");
        assertThat(result.slug()).isEqualTo("new-toeic-title");
        assertThat(result.description()).isEqualTo("New description");
        assertThat(result.thumbnailUrl()).isEqualTo("https://example.com/new.png");
        assertThat(result.displayOrder()).isEqualTo(3);
        assertThat(result.status()).isEqualTo(StudySetStatus.PUBLISHED);

        verify(studySetRepository).existsBySlugIgnoreCaseAndIdNot("new-toeic-title", studySetId);
        verify(studySetRepository).save(eq(existingStudySet));
    }

    @Test
    void shouldUpdateStudySetWithIncrementedSlugWhenGeneratedSlugBelongsToAnotherSet() {
        UUID studySetId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        StudySet existingStudySet = StudySet.builder()
            .title("Toeic Intermediate")
            .slug("toeic-intermediate")
            .description("Old")
            .thumbnailUrl(null)
            .displayOrder(4)
            .status(StudySetStatus.DRAFT)
            .build();
        existingStudySet.setId(studySetId);

        when(studySetRepository.findById(studySetId)).thenReturn(Optional.of(existingStudySet));
        when(studySetRepository.existsBySlugIgnoreCaseAndIdNot("toeic-basic", studySetId)).thenReturn(true);
        when(studySetRepository.existsBySlugIgnoreCaseAndIdNot("toeic-basic-2", studySetId)).thenReturn(false);
        when(studySetRepository.save(any(StudySet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminCatalogService service = new AdminCatalogServiceImpl(
            studySetRepository,
            studyUnitRepository,
            vocabularyRepository,
            Mappers.getMapper(AdminCatalogMapper.class),
            new StudySetSlugGenerator(studySetRepository)
        );
        var result = service.updateStudySet(studySetId, new UpsertStudySetRequest(
            "TOEIC Basic",
            "Updated",
            null,
            5,
            StudySetStatus.PUBLISHED
        ));

        assertThat(result.slug()).isEqualTo("toeic-basic-2");
        assertThat(result.title()).isEqualTo("TOEIC Basic");

        verify(studySetRepository).existsBySlugIgnoreCaseAndIdNot("toeic-basic", studySetId);
        verify(studySetRepository).existsBySlugIgnoreCaseAndIdNot("toeic-basic-2", studySetId);
    }
}
