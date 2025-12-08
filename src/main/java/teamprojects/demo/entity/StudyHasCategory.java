package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "STUDY_HAS_CATEGORY")
public class StudyHasCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_has_category_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_category_id") // DB의 FK 컬럼명
    private StudyCategory studyCategory; // 참조할 부모 엔티티


    public StudyHasCategory(Study study, StudyCategory studyCategory) {
        this.study = study;
        this.studyCategory = studyCategory;
    }
}