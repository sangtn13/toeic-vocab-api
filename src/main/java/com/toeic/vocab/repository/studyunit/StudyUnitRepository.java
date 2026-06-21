package com.toeic.vocab.repository.studyunit;

import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.model.studyunit.StudyUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyUnitRepository extends JpaRepository<StudyUnit, UUID> {

    @Query("""
            select u from StudyUnit u
            where u.studySet.id = :studySetId
            order by u.unitOrder asc, u.id asc
            """)
    List<StudyUnit> findByStudySetIdOrderByUnitOrderAscIdAsc(@Param("studySetId") UUID studySetId);

    @Query("""
            select u from StudyUnit u
            where u.studySet.id = :studySetId
              and u.active = true
            order by u.unitOrder asc, u.id asc
            """)
    List<StudyUnit> findByStudySetIdAndActiveTrueOrderByUnitOrderAscIdAsc(@Param("studySetId") UUID studySetId);

    @Query(
            value = """
                    select u
                    from StudyUnit u
                    where u.studySet.id = :studySetId
                      and u.active = true
                    order by u.unitOrder asc, u.id asc
                    """,
            countQuery = """
                    select count(u)
                    from StudyUnit u
                    where u.studySet.id = :studySetId
                      and u.active = true
                    """)
    Page<StudyUnit> findPublicUnitsByStudySetId(@Param("studySetId") UUID studySetId, Pageable pageable);

    @Query("""
            select u from StudyUnit u
            join fetch u.studySet s
            where s.slug = :slug
                and s.status = :status
                and u.id = :unitId
                and u.active = true
            """)
    Optional<StudyUnit> findPublicUnit(@Param("slug") String slug, @Param("unitId") UUID unitId,
            @Param("status") StudySetStatus status);

    @Query("""
            select u from StudyUnit u
            where u.studySet.id = :studySetId
              and u.active = true
              and (u.unitOrder > :unitOrder or (u.unitOrder = :unitOrder and u.id > :currentUnitId))
            order by u.unitOrder asc, u.id asc
            """)
    List<StudyUnit> findNextActiveUnits(
            @Param("studySetId") UUID studySetId,
            @Param("unitOrder") Integer unitOrder,
            @Param("currentUnitId") UUID currentUnitId,
            Pageable pageable);
}
