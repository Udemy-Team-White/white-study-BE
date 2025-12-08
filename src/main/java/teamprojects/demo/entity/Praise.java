package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.*;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "PRAISE")
@DynamicInsert
public class Praise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "praise_id", nullable = false)
    private Integer id;

    @Column(name = "message", nullable = false, length = 100)
    private String message;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    @Builder
    public Praise(Study study, User sender, User receiver, String message) {
        this.study = study;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }
}