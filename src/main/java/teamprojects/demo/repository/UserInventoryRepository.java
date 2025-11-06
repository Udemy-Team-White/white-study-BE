package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StoreItem;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserInventory;

public interface UserInventoryRepository extends JpaRepository<UserInventory, Integer> {

    // (API 6-3: '내 인벤토리' 페이징 조회 시 사용)
    Page<UserInventory> findByUser(User user, Pageable pageable);

    // (API 6-1: 상점 목록에서 '보유 여부' 표시 시 사용 가능)
    // (API 6-2: 아이템 '중복 구매 방지' 로직에서 사용)
    boolean existsByUserAndStoreItem(User user, StoreItem storeItem);
}