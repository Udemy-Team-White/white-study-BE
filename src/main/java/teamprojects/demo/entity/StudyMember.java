package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@Entity
@Table(name = "STUDY_MEMBER")
@DynamicInsert
public class StudyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_member_id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING) // DB에는 "LEADER", "MEMBER" 문자열로 저장됨
    @Column(name = "role", nullable = false, length = 50)
    private StudyRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;


    public enum StudyRole {
        LEADER,     // 스터디장
        MEMBER      // 일반 멤버
    }

    // StudyMember.java
    public void updateRole(StudyRole role) {
        this.role = role;
    }
}