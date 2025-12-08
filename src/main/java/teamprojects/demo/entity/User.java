package teamprojects.demo.entity;

import teamprojects.demo.entity.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "username", nullable = false, length = 10, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    @Builder
    public User(String email, String password, String salt, String username, UserRole role) {
        this.email = email;
        this.password = password;
        this.salt = salt;
        this.username = username;
        this.role = role;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
        if (userProfile.getUser() != this) {
            userProfile.setUser(this);
        }
    }

    // 닉네임 수정
    public void updateUsername(String username) {
        this.username = username;
    }

    // 비밀번호 수정
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}