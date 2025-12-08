package teamprojects.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import teamprojects.demo.entity.StoreItem;

public interface StoreItemRepository extends JpaRepository<StoreItem, Integer> {

    // API 6-1: 상점 목록 '전체' 페이징 조회 시
    // Service에서 isActive=true인 것만 필터링
    Page<StoreItem> findByIsActiveTrue(Pageable pageable);

    // API 6-1: 상점 목록 '필터링' 페이징 조회 시
    // (예: ?filter=BADGE)
    Page<StoreItem> findByIsActiveTrueAndItemType(String itemType, Pageable pageable);

    Page<StoreItem> findByItemType(String itemType, Pageable pageable);
}