package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StoreItem;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserInventory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;


public interface UserInventoryRepository extends JpaRepository<UserInventory, Integer> {

    @Query("SELECT ui FROM UserInventory ui JOIN FETCH ui.storeItem WHERE ui.user = :user")
    List<UserInventory> findAllByUserWithItem(@Param("user") User user);

    Page<UserInventory> findByUser(User user, Pageable pageable);

    boolean existsByUserAndStoreItem_ItemTypeAndEquippedTrue(User user, String itemType);

    boolean existsByUserAndStoreItem(User user, StoreItem storeItem);

    @Query("SELECT ui FROM UserInventory ui " +
            "JOIN FETCH ui.storeItem si " +
            "WHERE ui.user.id = :userId " +
            "AND (:itemType IS NULL OR si.itemType = :itemType)")
    Page<UserInventory> findByUserAndItemType(@Param("userId") Integer userId,
                                              @Param("itemType") String itemType,
                                              Pageable pageable);
}