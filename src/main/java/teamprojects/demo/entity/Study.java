package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(name = "study_type", nullable = false, length = 50)
    private StudyType studyType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "max_members", nullable = false)
    private Integer maxMembers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private StudyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "closed_at")
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
    @Builder.Default // 빌더 패턴 사용 시 초기화 유지
    private List<StudyHasCategory> categoryMappings = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 패턴 사용 시 초기화 유지
    private List<StudyMember> studyMembers = new ArrayList<>();

    public void updateTitle(String title) { this.title = title; }

    public void updateStudyName(String studyName) { this.studyName = studyName; }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public void updateStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void updateStatus(StudyStatus status) {
        this.status = status;
    }

    // 스터디 진행 방식 Enum (studyType 필드에서 사용)
    public enum StudyType {
        ONLINE,      // 온라인
        OFFLINE,     // 오프라인
        MIXED        // 온/오프라인 혼합
    }

    // 스터디 모집 상태 Enum (status 필드에서 사용)
    public enum StudyStatus {
        RECRUITING,          // 모집 중
        RECRUITMENT_CLOSED,  // 모집 마감
        IN_PROGRESS,         // 진행 중
        FINISHED,          // 종료
        RECRUITING_IN_PROGRESS // 진행 중 & 추가 모집
    }

    // 멤버 역할 Enum
    public enum StudyRole {
        LEADER,     // 스터디장
        MEMBER      // 일반 멤버
    }
    public Integer getCurrentMembers() {
        return this.studyMembers.size();
    }
    public void updateEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public void updateTodoCycle(String todoCycle) { this.todoCycle = todoCycle; }
    public void updateStudyType(StudyType studyType) { this.studyType = studyType; }
}