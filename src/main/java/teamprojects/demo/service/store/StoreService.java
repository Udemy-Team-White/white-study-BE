package teamprojects.demo.service.store;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import teamprojects.demo.dto.store.StoreItemListRequest;
import teamprojects.demo.dto.store.StoreItemListResponse;
import teamprojects.demo.entity.StoreItem;
import teamprojects.demo.entity.User;
import teamprojects.demo.entity.UserProfile;
import teamprojects.demo.global.common.code.status.ErrorStatus;
import teamprojects.demo.global.common.exception.CustomException;
import teamprojects.demo.global.utils.SecurityUtils;
import teamprojects.demo.repository.StoreItemRepository;
import teamprojects.demo.repository.UserProfileRepository;
import teamprojects.demo.repository.UserRepository;
import teamprojects.demo.dto.store.ItemPurchaseResponse;
import teamprojects.demo.entity.PointHistory;
import teamprojects.demo.entity.UserInventory;
import teamprojects.demo.repository.PointHistoryRepository;
import teamprojects.demo.repository.UserInventoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreItemRepository storeItemRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserInventoryRepository userInventoryRepository;
    private final PointHistoryRepository pointHistoryRepository;

    /**
     * API 6-1: 상점 아이템 목록 조회
     */
    public StoreItemListResponse getStoreItems(StoreItemListRequest request) {

        // 현재 사용자 확인 (포인트 조회용)
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        UserProfile profile = userProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 아이템 목록 조회 (필터링)
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<StoreItem> itemPage;

        if (request.getFilter() != null && !request.getFilter().isBlank()) {
            // 필터가 있으면 해당 타입만 조회
            itemPage = storeItemRepository.findByItemType(request.getFilter(), pageable);
        } else {
            // 필터가 없으면 전체 조회
            itemPage = storeItemRepository.findAll(pageable);
        }

        // DTO 변환
        List<StoreItemListResponse.StoreItemDto> itemDtos = itemPage.getContent().stream()
                .map(item -> StoreItemListResponse.StoreItemDto.builder()
                        .itemId(item.getId())
                        .itemName(item.getItemName())
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .itemType(item.getItemType())
                        .imageUrl(item.getImageUrl()) // 이미지도 반환
                        .build())
                .collect(Collectors.toList());

        // 페이지 정보
        StoreItemListResponse.PageInfoDto pageInfo = StoreItemListResponse.PageInfoDto.builder()
                .page(itemPage.getNumber())
                .size(itemPage.getSize())
                .totalPages(itemPage.getTotalPages())
                .totalElements(itemPage.getTotalElements())
                .build();

        // 최종 응답 (내 포인트 포함)
        return StoreItemListResponse.builder()
                .myPoints(profile.getPoints()) // 내 포인트
                .items(itemDtos)
                .pageInfo(pageInfo)
                .build();
    }
    /**
     * API 6-2: 아이템 구매
     */
    @Transactional
    public ItemPurchaseResponse purchaseItem(Integer itemId) {

        // 유저 확인
        Integer currentUserId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new CustomException(ErrorStatus.UNAUTHORIZED));
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        UserProfile profile = userProfileRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(ErrorStatus._INTERNAL_SERVER_ERROR));

        // 아이템 확인
        StoreItem item = storeItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorStatus.ITEM_NOT_FOUND)); // 404

        // 중복 구매 확인 (이미 보유 중이면 409)
        if (userInventoryRepository.existsByUserAndStoreItem(user, item)) {
            throw new CustomException(ErrorStatus.ALREADY_OWNED_ITEM);
        }

        // 포인트 잔액 확인 (부족하면 400)
        if (profile.getPoints() < item.getPrice()) {
            throw new CustomException(ErrorStatus.NOT_ENOUGH_POINTS);
        }

        // 구매 처리 (트랜잭션)

        // 포인트 차감
        profile.updatePoints(profile.getPoints() - item.getPrice());
        userProfileRepository.save(profile);

        // 포인트 내역 기록
        PointHistory history = PointHistory.builder()
                .user(user)
                .amount(-item.getPrice()) // 차감이니까 음수
                .reason("아이템 구매: " + item.getItemName())
                .build();
        pointHistoryRepository.save(history);

        // 인벤토리 추가
        UserInventory newInventory = UserInventory.builder()
                .user(user)
                .storeItem(item)
                .equipped(false)
                .build();
        newInventory = userInventoryRepository.save(newInventory);

        // 응답 반환
        return ItemPurchaseResponse.builder()
                .inventoryId(newInventory.getId())
                .purchasedItem(ItemPurchaseResponse.PurchasedItemDto.builder()
                        .itemId(item.getId())
                        .itemName(item.getItemName())
                        .build())
                .remainingPoints(profile.getPoints())
                .build();
    }
}
