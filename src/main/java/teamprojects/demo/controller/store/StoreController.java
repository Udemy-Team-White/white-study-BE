package teamprojects.demo.controller.store;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import teamprojects.demo.dto.store.StoreItemListRequest;
import teamprojects.demo.dto.store.StoreItemListResponse;
import teamprojects.demo.global.common.ApiResponse;
import teamprojects.demo.service.store.StoreService;
import teamprojects.demo.dto.store.ItemPurchaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreController {

    private final StoreService storeService;

    /**
     * API 6-1: 상점 아이템 목록 조회
     * URL: GET /api/store/items?page=0&size=12&filter=...
     */
    @GetMapping("/items")
    public ApiResponse<StoreItemListResponse> getStoreItems(
            @ModelAttribute StoreItemListRequest request) { // 쿼리 파라미터 수신

        StoreItemListResponse responseDto = storeService.getStoreItems(request);

        return ApiResponse.onSuccess(responseDto, "상점 아이템 목록 조회 성공");
    }

    /**
     * API 6-2: 아이템 구매 요청
     * URL: POST /api/store/items/{itemId}/purchase
     */
    @PostMapping("/items/{itemId}/purchase")
    public ApiResponse<ItemPurchaseResponse> purchaseItem(@PathVariable Integer itemId) {

        ItemPurchaseResponse responseDto = storeService.purchaseItem(itemId);

        return ApiResponse.onSuccess(responseDto, "아이템 구매가 완료되었습니다.");
    }
}