package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "USER_PROFILE")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Integer id;


    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "username", nullable = false, unique = true, length = 10)
    private String username;

    @Column(name = "profile_image_url", length = 1024)
    private String profileImageUrl;

    @Column(name = "introduction", length = 500)
    private String introduction;

    @Builder.Default
    @Column(name = "points", nullable = false)
    private Integer points = 0;

    @Builder.Default
    @Column(name = "reliability_score", nullable = false)
    private Integer reliabilityScore = 50;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_login", nullable = false)
    private LocalDateTime lastLogin;


    public void setUser(User user) {
        this.user = user;
        if (user.getUserProfile() != this) {
            user.setUserProfile(this);
        }
    }
    public void updateReliabilityScore(Integer changeAmount) {
        int newScore = this.reliabilityScore + changeAmount;

        if (newScore < 0) {
            this.reliabilityScore = 0;   // 0점 미만으로 떨어지면 0점으로 고정
        } else if (newScore > 100) {
            this.reliabilityScore = 100; // 100점 초과하면 100점으로 고정 (Max Cap)
        } else {
            this.reliabilityScore = newScore; // 그 외에는 정상 반영
        }
    }
    public void updatePoints(Integer points) {
        this.points = points;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }
}