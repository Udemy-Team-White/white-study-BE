package teamprojects.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StudyHasCategory;

public interface StudyHasCategoryRepository extends JpaRepository<StudyHasCategory, Long> {

    // (API 2-1 스터디 개설 시 .save() 메서드가 주로 사용됩니다.)
    // (특별한 이름 규칙의 쿼리 메서드가 당장은 필요하지 않습니다.)

}