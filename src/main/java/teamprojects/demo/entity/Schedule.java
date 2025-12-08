package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime; // (DB의 TIME 타입 매핑)

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "SCHEDULE")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING) // (DB에 0,1이 아닌 'Mon', 'Tue' 문자열로 저장)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; // (방금 위에서 만든 DayOfWeek Enum)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // (DB의 TIME 타입은 LocalTime으로 매핑)

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // Schedule (N) : Study (1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    // Schedule (N) : User (1)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    //생성자 (Service에서 일정을 생성할 때 사용)
    @Builder
    public Schedule(Study study, User user, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.study = study;
        this.user = user;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    // UserService의 컴파일 오류 해결을 위한 임시 Getter
    public LocalTime getScheduleTime() {
        return this.startTime;
    }
}