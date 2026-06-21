package com.toeic.vocab.mapper;

import com.toeic.vocab.dto.admin.AdminStudySetDto;
import com.toeic.vocab.dto.admin.AdminStudyUnitDto;
import com.toeic.vocab.dto.admin.AdminVocabularyDto;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.model.studyunit.StudyUnit;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.request.admin.UpsertStudySetRequest;
import com.toeic.vocab.request.admin.UpsertStudyUnitRequest;
import com.toeic.vocab.request.admin.UpsertVocabularyRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AdminCatalogMapper {

    @Mapping(target = "unitCount", expression = "java(studySet.getUnits() == null ? 0 : studySet.getUnits().size())")
    @Mapping(target = "vocabularyCount", expression = "java(studySet.getUnits() == null ? 0 : studySet.getUnits().stream().mapToInt(unit -> unit.getVocabularies() == null ? 0 : unit.getVocabularies().size()).sum())")
    AdminStudySetDto toStudySetDto(StudySet studySet);

    @Mapping(target = "studySetId", source = "studySet.id")
    @Mapping(target = "studySetTitle", source = "studySet.title")
    @Mapping(target = "vocabularyCount", expression = "java(studyUnit.getVocabularies() == null ? 0 : studyUnit.getVocabularies().size())")
    AdminStudyUnitDto toStudyUnitDto(StudyUnit studyUnit);

    @Mapping(target = "studySetId", source = "unit.studySet.id")
    @Mapping(target = "studySetTitle", source = "unit.studySet.title")
    @Mapping(target = "unitId", source = "unit.id")
    @Mapping(target = "unitTitle", source = "unit.title")
    AdminVocabularyDto toVocabularyDto(Vocabulary vocabulary);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "units", ignore = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "thumbnailUrl", source = "request.thumbnailUrl")
    @Mapping(target = "displayOrder", source = "request.displayOrder")
    @Mapping(target = "status", source = "request.status")
    StudySet toStudySet(UpsertStudySetRequest request, String title, String slug);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "thumbnailUrl", source = "request.thumbnailUrl")
    @Mapping(target = "displayOrder", source = "request.displayOrder")
    @Mapping(target = "status", source = "request.status")
    void updateStudySetFromRequest(
            UpsertStudySetRequest request,
            String title,
            String slug,
            @MappingTarget StudySet studySet);

    @Mapping(target = "vocabularies", ignore = true)
    @Mapping(target = "studySet", source = "studySet")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "unitOrder", source = "request.unitOrder")
    @Mapping(target = "active", source = "request.active")
    StudyUnit toStudyUnit(UpsertStudyUnitRequest request, StudySet studySet, String title);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "unitOrder", source = "request.unitOrder")
    @Mapping(target = "active", source = "request.active")
    @BeanMapping(ignoreByDefault = true)
    void updateStudyUnitFromRequest(
            UpsertStudyUnitRequest request,
            String title,
            @MappingTarget StudyUnit studyUnit);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "unit", source = "studyUnit")
    @Mapping(target = "word", source = "word")
    @Mapping(target = "meaning", source = "meaning")
    @Mapping(target = "definition", source = "request.definition")
    @Mapping(target = "exampleSentence", source = "request.exampleSentence")
    @Mapping(target = "exampleTranslation", source = "request.exampleTranslation")
    @Mapping(target = "phoneticUs", source = "request.phoneticUs")
    @Mapping(target = "phoneticUk", source = "request.phoneticUk")
    @Mapping(target = "pronunciationUsUrl", source = "request.pronunciationUsUrl")
    @Mapping(target = "pronunciationUkUrl", source = "request.pronunciationUkUrl")
    @Mapping(target = "hint", source = "request.hint")
    @Mapping(target = "partOfSpeech", source = "request.partOfSpeech")
    @Mapping(target = "difficultyLevel", source = "request.difficultyLevel")
    @Mapping(target = "displayOrder", source = "request.displayOrder")
    @Mapping(target = "active", source = "request.active")
    Vocabulary toVocabulary(
            UpsertVocabularyRequest request,
            StudyUnit studyUnit,
            String word,
            String meaning);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "word", source = "word")
    @Mapping(target = "meaning", source = "meaning")
    @Mapping(target = "definition", source = "request.definition")
    @Mapping(target = "exampleSentence", source = "request.exampleSentence")
    @Mapping(target = "exampleTranslation", source = "request.exampleTranslation")
    @Mapping(target = "phoneticUs", source = "request.phoneticUs")
    @Mapping(target = "phoneticUk", source = "request.phoneticUk")
    @Mapping(target = "pronunciationUsUrl", source = "request.pronunciationUsUrl")
    @Mapping(target = "pronunciationUkUrl", source = "request.pronunciationUkUrl")
    @Mapping(target = "hint", source = "request.hint")
    @Mapping(target = "partOfSpeech", source = "request.partOfSpeech")
    @Mapping(target = "difficultyLevel", source = "request.difficultyLevel")
    @Mapping(target = "displayOrder", source = "request.displayOrder")
    @Mapping(target = "active", source = "request.active")
    void updateVocabularyFromRequest(
            UpsertVocabularyRequest request,
            String word,
            String meaning,
            @MappingTarget Vocabulary vocabulary);
}
