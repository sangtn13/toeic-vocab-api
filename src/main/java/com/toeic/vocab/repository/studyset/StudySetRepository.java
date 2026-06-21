package com.toeic.vocab.repository.studyset;

import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.model.studyset.StudySet;
import com.toeic.vocab.service.study.StudySetCardAggregate;
import com.toeic.vocab.service.study.StudySetVocabularyRef;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudySetRepository extends JpaRepository<StudySet, UUID> {

    @Query("""
            select s from StudySet s
            where (coalesce(:keyword, '') = ''
                or lower(s.title) like concat('%', lower(coalesce(:keyword, '')), '%')
                or lower(s.slug) like concat('%', lower(coalesce(:keyword, '')), '%'))
            """)
    Page<StudySet> search(@Param("keyword") String keyword, Pageable pageable);

    Optional<StudySet> findBySlugAndStatus(String slug, StudySetStatus status);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, UUID id);

    @Query("""
            select new com.toeic.vocab.service.study.StudySetCardAggregate(
                s.id,
                s.title,
                s.slug,
                s.description,
                s.thumbnailUrl,
                s.status,
                count(distinct u.id),
                count(v.id),
                0L,
                0L
            )
            from StudySet s
            left join s.units u on u.active = true
            left join Vocabulary v on v.unit = u and v.active = true
            where s.status = :status
            group by s.id, s.title, s.slug, s.description, s.thumbnailUrl, s.status, s.displayOrder
            order by s.displayOrder asc, s.id asc
            """)
    List<StudySetCardAggregate> findStudySetCardStatsByStatus(@Param("status") StudySetStatus status);

    @Query(
            value = """
                    select new com.toeic.vocab.service.study.StudySetCardAggregate(
                        s.id,
                        s.title,
                        s.slug,
                        s.description,
                        s.thumbnailUrl,
                        s.status,
                        count(distinct u.id),
                        count(v.id),
                        0L,
                        0L
                    )
                    from StudySet s
                    left join s.units u on u.active = true
                    left join Vocabulary v on v.unit = u and v.active = true
                    where s.status = :status
                    group by s.id, s.title, s.slug, s.description, s.thumbnailUrl, s.status, s.displayOrder
                    order by s.displayOrder asc, s.id asc
                    """,
            countQuery = """
                    select count(s)
                    from StudySet s
                    where s.status = :status
                    """)
    Page<StudySetCardAggregate> findStudySetCardStatsByStatus(
            @Param("status") StudySetStatus status,
            Pageable pageable);

    @Query("""
            select new com.toeic.vocab.service.study.StudySetCardAggregate(
                s.id,
                s.title,
                s.slug,
                s.description,
                s.thumbnailUrl,
                s.status,
                count(distinct u.id),
                count(v.id),
                coalesce(sum(case when progress.attemptCount > 0 then 1 else 0 end), 0),
                coalesce(sum(case when progress.mastered = true then 1 else 0 end), 0)
            )
            from StudySet s
            left join s.units u on u.active = true
            left join Vocabulary v on v.unit = u and v.active = true
            left join StudyProgress progress on progress.vocabulary = v and progress.user.id = :userId
            where s.status = :status
            group by s.id, s.title, s.slug, s.description, s.thumbnailUrl, s.status, s.displayOrder
            order by s.displayOrder asc, s.id asc
            """)
    List<StudySetCardAggregate> findStudySetCardProgressByStatusAndUserId(
            @Param("status") StudySetStatus status,
            @Param("userId") UUID userId);

    @Query(
            value = """
                    select new com.toeic.vocab.service.study.StudySetCardAggregate(
                        s.id,
                        s.title,
                        s.slug,
                        s.description,
                        s.thumbnailUrl,
                        s.status,
                        count(distinct u.id),
                        count(v.id),
                        coalesce(sum(case when progress.attemptCount > 0 then 1 else 0 end), 0),
                        coalesce(sum(case when progress.mastered = true then 1 else 0 end), 0)
                    )
                    from StudySet s
                    left join s.units u on u.active = true
                    left join Vocabulary v on v.unit = u and v.active = true
                    left join StudyProgress progress on progress.vocabulary = v and progress.user.id = :userId
                    where s.status = :status
                    group by s.id, s.title, s.slug, s.description, s.thumbnailUrl, s.status, s.displayOrder
                    order by s.displayOrder asc, s.id asc
                    """,
            countQuery = """
                    select count(s)
                    from StudySet s
                    where s.status = :status
                    """)
    Page<StudySetCardAggregate> findStudySetCardProgressByStatusAndUserId(
            @Param("status") StudySetStatus status,
            @Param("userId") UUID userId,
            Pageable pageable);

    @Query("""
            select new com.toeic.vocab.service.study.StudySetVocabularyRef(
                s.id,
                v.id
            )
            from StudySet s
            join s.units u
            join u.vocabularies v
            where s.id in :studySetIds
              and u.active = true
              and v.active = true
            order by s.displayOrder asc, s.id asc, v.displayOrder asc, v.id asc
            """)
    List<StudySetVocabularyRef> findStudySetVocabularyRefsByStudySetIds(@Param("studySetIds") List<UUID> studySetIds);
}
