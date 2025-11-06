package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private Integer id;

    @Column(name = "study_name", length = 50)
    private String studyName;

    @Column(name = "study_type", nullable = false, length = 50)
    private String studyType;

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


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leader_id")
    private User leader;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyHasCategory> categoryMappings = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyMember> studyMembers = new ArrayList<>();


    @Builder
    public Study(User leader, String studyName, String studyType, String title,
                 String content, Integer maxMembers, String status,
                 LocalDateTime closedAt, LocalDateTime startDate,
                 LocalDateTime endDate, String todoCycle) {

        this.leader = leader;
        this.studyName = studyName;
        this.studyType = studyType;
        this.title = title;
        this.content = content;
        this.maxMembers = maxMembers;
        this.status = status;
        this.closedAt = closedAt;
        this.startDate = startDate;
        this.endDate = endDate;
        this.todoCycle = todoCycle;
    }
}