package teamprojects.demo.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "STORE_ITEM")
@DynamicInsert
public class StoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Integer id;

    @Column(name = "item_name", nullable = false, length = 50)
    private String itemName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "item_type", nullable = false, length = 50)
    private String itemType;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public StoreItem(String itemName, String description, Integer price, String itemType, Boolean isActive) {
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.itemType = itemType;
        this.isActive = isActive;
        this.imageUrl = imageUrl;
    }
}