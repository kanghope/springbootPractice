package com.shop.controller;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController; // ⭐️ @RestController로 변경
import java.util.Optional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.shop.dto.ItemFormDto; // 또는 MainItemDetailDto 같은 경량화된 DTO
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

// 이 컨트롤러는 API 응답을 처리하도록 변경합니다.
@RestController // JSON 데이터를 반환합니다.
@RequiredArgsConstructor
@RequestMapping("/api") // 👈 클래스 레벨 매핑을 '/api'로 변경
public class MainController {
    private final ItemService itemService;

    /**
     * 메인 페이지 상품 목록 조회 API
     * GET /items?searchQuery=...&page=...&size=...
     * React 프론트엔드에서 이 엔드포인트를 호출합니다.
     * * @param itemSearchDto 검색 조건 (searchQuery를 담고 있음)
     * @param page Optional 페이지 번호 (0-based)
     * @param size Optional 페이지 크기 (프론트엔드에서는 기본 6으로 가정)
     * @return Spring Data Page<MainItemDto> 객체 (JSON으로 변환되어 응답)
     */
    @GetMapping(value = "/items") // ⭐️ 엔드포인트를 명확히 /items로 설정
    public Page<MainItemDto> mainApi(
            ItemSearchDto itemSearchDto, // Query Parameter로 바인딩
            @RequestParam(required = false) Optional<Integer> page,
            @RequestParam(required = false) Optional<Integer> size) {

        // 프론트엔드에서 size를 보내지 않으면 기본 6을 사용합니다.
        int pageSize = size.orElse(6);

        // 페이지 번호는 Optional.orElse(0) 또는 0을 사용합니다.
        Pageable pageable = PageRequest.of(page.orElse(0), pageSize);

        // itemService.getMainItemPage는 이미 Page<MainItemDto>를 반환하도록 잘 구현되어 있습니다.
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        // @RestController 덕분에 Page 객체가 자동으로 JSON으로 변환되어 응답됩니다.
        return items;
    }

    /**
     * ⭐️ 일반 사용자용 상품 상세 조회 API
     * GET /api/item/{itemId}
     */
    @GetMapping(value = "/item/{itemId}")
    public ResponseEntity<?> publicItemDtl(@PathVariable("itemId") Long itemId){
        try{
            // 상품 ID로 DTO 조회 (일반 사용자에게 필요한 정보만 포함된 DTO 사용을 권장)
            ItemFormDto itemFormDto = itemService.getItemDtl(itemId); // 현재는 기존 DTO 재사용
            return ResponseEntity.ok(itemFormDto);
        }
        catch(Exception e)
        {
            // 404/500 등의 오류 처리
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다.");
        }
    }

}
