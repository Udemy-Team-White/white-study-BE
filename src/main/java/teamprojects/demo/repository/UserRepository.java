package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // 로그인 시 사용
    Optional<User> findByEmail(String email);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 닉네임 중복 확인을 위한 메서드
    boolean existsByUsername(String username);
}