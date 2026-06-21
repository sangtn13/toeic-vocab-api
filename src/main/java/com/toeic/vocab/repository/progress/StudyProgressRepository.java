package com.toeic.vocab.repository.progress;

import com.toeic.vocab.model.progress.StudyProgress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StudyProgressRepository extends JpaRepository<StudyProgress, UUID> {

    @Query("""
            select count(progress)
            from StudyProgress progress
            where progress.user.id = :userId
            """)
    long countAllByUserId(@Param("userId") UUID userId);

    @Query("""
            select progress
            from StudyProgress progress
            where progress.user.id = :userId
            """)
    List<StudyProgress> findByUserId(@Param("userId") UUID userId);

    @Query("""
            select progress
            from StudyProgress progress
            where progress.user.id = :userId
              and progress.vocabulary.id in :vocabularyIds
            """)
    List<StudyProgress> findByUserIdAndVocabularyIdIn(
            @Param("userId") UUID userId,
            @Param("vocabularyIds") List<UUID> vocabularyIds);

    @Query("""
            select progress
            from StudyProgress progress
            where progress.user.id = :userId
              and progress.vocabulary.id = :vocabularyId
            """)
    Optional<StudyProgress> findByUserIdAndVocabularyId(
            @Param("userId") UUID userId,
            @Param("vocabularyId") UUID vocabularyId);

    @Query("""
            select progress
            from StudyProgress progress
            where progress.user.id = :userId
            order by progress.updatedAt desc
            """)
    List<StudyProgress> findRecentByUserIdOrderByUpdatedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("""
            delete from StudyProgress progress
            where progress.user.id = :userId
              and progress.vocabulary.unit.id = :unitId
            """)
    int deleteByUserIdAndUnitId(@Param("userId") UUID userId, @Param("unitId") UUID unitId);
}
