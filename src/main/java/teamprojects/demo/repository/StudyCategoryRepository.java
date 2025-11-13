package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StudyCategory;

public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Integer> {
    // ID로 조회하는 findById(Integer id)는 JpaRepository에 기본으로 존재합니다.
}