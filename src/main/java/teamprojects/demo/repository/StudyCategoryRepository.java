package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StudyCategory;

public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Integer> {
}