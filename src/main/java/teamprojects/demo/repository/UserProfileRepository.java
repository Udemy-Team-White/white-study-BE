package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByUsername(String username);
}