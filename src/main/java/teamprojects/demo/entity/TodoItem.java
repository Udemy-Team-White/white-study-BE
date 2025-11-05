package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TODO_ITEM")
@DynamicInsert
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_item_id", nullable = false)
    private Long id;

    @Column(name = "content", length = 200)
    private String content;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_list_id")
    private TodoList todoList;


    @Builder
    public TodoItem(TodoList todoList, String content, Integer orderIndex) {
        this.todoList = todoList;
        this.content = content;
        this.orderIndex = orderIndex;
    }
}