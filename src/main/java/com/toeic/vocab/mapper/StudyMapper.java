package com.toeic.vocab.mapper;

import com.toeic.vocab.dto.study.StudyProgressContextDto;
import com.toeic.vocab.dto.study.StudyWordReviewDto;
import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.service.study.StudyProgressState;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudyMapper {

    @Mapping(target = "persistent", source = "persisted")
    StudyProgressContextDto toStudyProgressContextDto(StudyProgressState progressState);

    @Mapping(target = "vocabularyId", source = "id")
    StudyWordReviewDto toWordReviewDto(Vocabulary vocabulary);
}
