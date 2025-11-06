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

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "reliability_score", nullable = false)
    private Integer reliabilityScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_login", nullable = false)
    private LocalDateTime lastLogin;

    @Builder
    public UserProfile(User user, String username, String profileImageUrl, String introduction, Integer points, Integer reliabilityScore) {
        this.user = user;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
        this.points = (points != null) ? points : 0;
        this.reliabilityScore = (reliabilityScore != null) ? reliabilityScore : 50;
    }

    public void setUser(User user) {
        this.user = user;
        if (user.getUserProfile() != this) {
            user.setUserProfile(this);
        }
    }
}