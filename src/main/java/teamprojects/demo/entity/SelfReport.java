package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "SELF_REPORT")
public class SelfReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", nullable = false)
    private Integer id;

    @Column(name = "subject", nullable = false, length = 100)
    private String subject;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @Builder
    public SelfReport(User user, Study study, String subject, String summary, String content) {
        this.user = user;
        this.study = study;
        this.subject = subject;
        this.summary = summary;
        this.content = content;
    }
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateSubject(String subject) {
        this.subject = subject;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}