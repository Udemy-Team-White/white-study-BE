package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {

    boolean existsByUsername(String username);
}