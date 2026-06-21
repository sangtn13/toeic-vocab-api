package com.toeic.vocab.model.studyset;

import com.toeic.vocab.enums.StudySetStatus;
import com.toeic.vocab.model.base.BaseEntity;
import com.toeic.vocab.model.studyunit.StudyUnit;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "study_sets")
public class StudySet extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudySetStatus status;

    @Builder.Default
    @OneToMany(mappedBy = "studySet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyUnit> units = new ArrayList<>();
}
