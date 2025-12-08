package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.Study;
import teamprojects.demo.entity.StudyHasCategory;
import java.util.List;

public interface StudyHasCategoryRepository extends JpaRepository<StudyHasCategory, Integer> {

    //API 1-5
    List<StudyHasCategory> findByStudy(Study study);

    // API 2-1 스터디 개설 시 .save() 메서드가 주로 사용됩니다.

    void deleteByStudy(Study study);
}