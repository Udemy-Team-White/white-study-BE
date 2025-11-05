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
@Table(name = "USER_INVENTORY")
@DynamicInsert
public class UserInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id", nullable = false)
    private Long id;

    @CreationTimestamp
    @Column(name = "acquired_at", nullable = false, updatable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "is_expired", nullable = false)
    private Boolean isExpired;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id")
    private StoreItem storeItem;

    @Builder
    public UserInventory(User user, StoreItem storeItem, LocalDateTime expiresAt, Integer durationDays) {
        this.user = user;
        this.storeItem = storeItem;
        this.expiresAt = expiresAt;
        this.durationDays = (durationDays != null) ? durationDays : 0;
    }
}