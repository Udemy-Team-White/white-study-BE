package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "STUDY")
@DynamicInsert
public class Study {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_id", nullable = false)
    private Long id;

    @Column(name = "study_name", length = 50)
    private String studyName;

    @Column(name = "study_type", nullable = false, length = 50)
    private String studyType; // (Default: 'ONLINE' -> @DynamicInsert로 처리)

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "todo_cycle", length = 50)
    private String todoCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id") // (DB의 'leader_id' FK 컬럼)
    private User leader;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyHasCategory> categoryMappings = new ArrayList<>();

    // (builder 등은 후에 추가하겠습니다.)
}