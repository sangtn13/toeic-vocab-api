package com.toeic.vocab.repository.vocabulary;

import com.toeic.vocab.model.vocabulary.Vocabulary;
import com.toeic.vocab.service.study.AnswerSubmissionContext;
import com.toeic.vocab.service.study.ProgressSummaryAggregate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {

    @Query("""
            select new com.toeic.vocab.service.study.AnswerSubmissionContext(
                v.id,
                v.word,
                v.meaning,
                u.id,
                u.unitOrder,
                s.id
            )
            from Vocabulary v
            join v.unit u
            join u.studySet s
            where v.id = :vocabularyId
            """)
    java.util.Optional<AnswerSubmissionContext> findAnswerSubmissionContextById(
            @Param("vocabularyId") UUID vocabularyId);

    @Query("""
            select v
            from Vocabulary v
            where (:unitId is null or v.unit.id = :unitId)
              and (
                  coalesce(:keyword, '') = ''
                  or lower(v.word) like concat('%', lower(coalesce(:keyword, '')), '%')
                  or lower(v.meaning) like concat('%', lower(coalesce(:keyword, '')), '%')
                  or lower(v.definition) like concat('%', lower(coalesce(:keyword, '')), '%')
              )
            """)
    Page<Vocabulary> search(
            @Param("unitId") UUID unitId,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("""
            select v
            from Vocabulary v
            join v.unit u
            where u.id in :unitIds
              and v.active = true
            order by u.unitOrder asc, u.id asc, v.displayOrder asc, v.id asc
            """)
    List<Vocabulary> findByUnitIdInAndActiveTrueOrderByUnitOrderAscDisplayOrderAscIdAsc(
            @Param("unitIds") Collection<UUID> unitIds);

    @Query("""
            select v
            from Vocabulary v
            join v.unit u
            where u.studySet.id = :studySetId
              and v.active = true
            order by v.displayOrder asc, v.id asc
            """)
    List<Vocabulary> findByUnit_StudySet_IdAndActiveTrueOrderByDisplayOrderAscIdAsc(@Param("studySetId") UUID studySetId);

    @Query("""
            select v.id
            from Vocabulary v
            where v.unit.id = :unitId
              and v.active = true
            order by v.displayOrder, v.id
            """)
    List<UUID> findActiveIdsByUnitIdOrderByDisplayOrderAscIdAsc(@Param("unitId") UUID unitId);

    @Query("""
            select v.id
            from Vocabulary v
            where v.unit.studySet.id = :studySetId
              and v.active = true
            order by v.displayOrder, v.id
            """)
    List<UUID> findActiveIdsByStudySetIdOrderByDisplayOrderAscIdAsc(@Param("studySetId") UUID studySetId);

    @Query("""
            select new com.toeic.vocab.service.study.ProgressSummaryAggregate(
                count(v),
                coalesce(sum(case when progress.attemptCount > 0 then 1 else 0 end), 0),
                coalesce(sum(case when progress.mastered = true then 1 else 0 end), 0)
            )
            from Vocabulary v
            left join StudyProgress progress
                on progress.vocabulary = v
               and progress.user.id = :userId
            where v.unit.id = :unitId
              and v.active = true
            """)
    ProgressSummaryAggregate summarizeUnitProgress(@Param("userId") UUID userId, @Param("unitId") UUID unitId);

    @Query("""
            select new com.toeic.vocab.service.study.ProgressSummaryAggregate(
                count(v),
                coalesce(sum(case when progress.attemptCount > 0 then 1 else 0 end), 0),
                coalesce(sum(case when progress.mastered = true then 1 else 0 end), 0)
            )
            from Vocabulary v
            left join StudyProgress progress
                on progress.vocabulary = v
               and progress.user.id = :userId
            where v.unit.studySet.id = :studySetId
              and v.active = true
            """)
    ProgressSummaryAggregate summarizeStudySetProgress(
            @Param("userId") UUID userId,
            @Param("studySetId") UUID studySetId);
}
