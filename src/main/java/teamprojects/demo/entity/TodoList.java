package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TODO_LIST")
public class TodoList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_list_id", nullable = false)
    private Integer id;

    @Column(name = "title", length = 100)
    private String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "target_date", nullable = false)
    private LocalDateTime targetDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "todoList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoItem> todoItems = new ArrayList<>();


    @Builder
    public TodoList(Study study, User user, String title, LocalDateTime targetDate) {
        this.study = study;
        this.user = user;
        this.title = title;
        this.targetDate = targetDate;
    }
    public void updateTargetDate(LocalDateTime targetDate) { this.targetDate = targetDate; }
}