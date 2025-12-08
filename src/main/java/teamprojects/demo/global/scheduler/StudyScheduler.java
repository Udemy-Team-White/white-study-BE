package teamprojects.demo.global.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import teamprojects.demo.entity.Study;
import teamprojects.demo.repository.StudyRepository;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StudyScheduler {

    private final StudyRepository studyRepository;

    // 매일 자정 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredStudies() {
        System.out.println("=== [스케줄러] 만료된 스터디 정리 시작 ===");

        // 오늘 날짜만 구합니다.
        LocalDate today = LocalDate.now();

        List<Study> expiredStudies = studyRepository.findByEndDateBeforeAndStatusNot(today, Study.StudyStatus.FINISHED);

        for (Study study : expiredStudies) {
            study.updateStatus(Study.StudyStatus.FINISHED);
            System.out.println(" -> 스터디 [" + study.getTitle() + "] 종료 처리됨.");
        }

        System.out.println("=== [스케줄러] 총 " + expiredStudies.size() + "건 처리 완료 ===");
    }
}