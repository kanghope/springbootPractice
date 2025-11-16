package com.shop.controller;

import com.shop.dto.OrderDto;
import com.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.shop.dto.OrderHistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.core.context.SecurityContextHolder;
import com.shop.config.CustomUserDetails; // CustomUserDetails 임포트 필요
import com.shop.dto.OrderIdsDto; // ⭐️ 새로 추가된 DTO 임포트

@RestController // 💡 @Controller + @ResponseBody 역할
@RequiredArgsConstructor
@RequestMapping("/api/order") // 💡 클래스 레벨 매핑 추가
public class OrderController {

    private final OrderService orderService;

    // -------------------------------------------------------------------------
    // 1. POST /api/order/order (주문 요청)
    // -------------------------------------------------------------------------
    @PostMapping(value = "")
    public ResponseEntity<?> order(@RequestBody @Valid OrderDto orderDto,
                                   BindingResult bindingResult,
                                   Principal principal) {

        // 1. 유효성 검사 실패 (400 Bad Request)
        if (bindingResult.hasErrors()) {
            // FieldError를 Map<필드명, 에러메시지> 형태로 반환
            Map<String, String> errorMap = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            DefaultMessageSourceResolvable::getDefaultMessage
                    ));
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }

        // ⭐️ [수정] 이메일 정보를 CustomUserDetails에서 직접 가져오기
        //String email = principal.getName();
        String email ;
        try {
            // SecurityContext에서 인증 객체를 가져옵니다.
            Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principalObject instanceof CustomUserDetails) {
                // CustomUserDetails로 형 변환 후 getEmail() 메서드를 사용
                email = ((CustomUserDetails) principalObject).getEmail();
            } else {
                // CustomUserDetails가 아닌 경우, 기본적으로 getName()을 사용 (백업 로직)
                email = principal.getName();
            }
        } catch (Exception e) {
            // 인증 정보가 아예 없는 경우 (토큰이 없거나 만료된 경우), SecurityConfig에서 401이 먼저 처리되지만
            // 안전을 위해 예외 처리
            return new ResponseEntity<>("인증된 사용자 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        Long orderId;

        try {
            // 2. 주문 서비스 호출 및 주문 ID 획득
            orderId = orderService.order(orderDto, email);
        } catch (Exception e) {
            // 3. 주문 처리 중 오류 (e.getMessage()를 본문에 담아 400 Bad Request 반환)
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // 4. 주문 성공 (200 OK와 함께 생성된 주문 ID 반환)
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // 2. GET /api/order/orders (주문 이력 목록 조회)
    // -------------------------------------------------------------------------
    // 경로 변수 대신 쿼리 파라미터로 페이지와 사이즈를 받습니다.
    @GetMapping(value = "/orders")
    public ResponseEntity<?> orderHist(
            @RequestParam(value = "page", defaultValue = "0") int page, // 0부터 시작
            @RequestParam(value = "size", defaultValue = "4") int size, // 페이지당 4개
            Principal principal) {

        // Pageable 객체 생성 (page, size는 0-based)
        Pageable pageable = PageRequest.of(page, size);

        // ⭐️ [수정] 이메일 정보를 CustomUserDetails에서 직접 가져오기
        //String email = principal.getName();
        String email ;
        try {
            // SecurityContext에서 인증 객체를 가져옵니다.
            Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principalObject instanceof CustomUserDetails) {
                // CustomUserDetails로 형 변환 후 getEmail() 메서드를 사용
                email = ((CustomUserDetails) principalObject).getEmail();
            } else {
                // CustomUserDetails가 아닌 경우, 기본적으로 getName()을 사용 (백업 로직)
                email = principal.getName();
            }
        } catch (Exception e) {
            // 인증 정보가 아예 없는 경우 (토큰이 없거나 만료된 경우), SecurityConfig에서 401이 먼저 처리되지만
            // 안전을 위해 예외 처리
            return new ResponseEntity<>("인증된 사용자 정보가 없습니다.", HttpStatus.UNAUTHORIZED); // 401
        }

        // 주문 이력 목록 및 페이징 정보 조회
        Page<OrderHistDto> ordersHistDtoPage = orderService.getOrderList(email, pageable);

        // Page<OrderHistDto> 객체를 JSON 응답 본문에 담아 200 OK로 반환
        return ResponseEntity.ok(ordersHistDtoPage);
    }

    // -------------------------------------------------------------------------
    // 3. POST /api/order/{orderId}/cancel (주문 취소)
    // -------------------------------------------------------------------------
    @PostMapping("/orders/{orderId}/cancel") // 💡 경로를 orders로 변경
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId") Long orderId, Principal principal) {

        String email ;
        try {
            // SecurityContext에서 인증 객체를 가져옵니다.
            Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principalObject instanceof CustomUserDetails) {
                // CustomUserDetails로 형 변환 후 getEmail() 메서드를 사용
                email = ((CustomUserDetails) principalObject).getEmail();
            } else {
                // CustomUserDetails가 아닌 경우, 기본적으로 getName()을 사용 (백업 로직)
                email = principal.getName();
            }
        } catch (Exception e) {
            // 인증 정보가 아예 없는 경우 (토큰이 없거나 만료된 경우), SecurityConfig에서 401이 먼저 처리되지만
            // 안전을 위해 예외 처리
            return new ResponseEntity<>("인증된 사용자 정보가 없습니다.", HttpStatus.UNAUTHORIZED); // 401
        }
        // 1. 주문 권한 검증
        if (!orderService.validateOrder(orderId, email)) {
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN); // 403 Forbidden
        }

        try {
            // 2. 주문 취소 서비스 호출
            orderService.cancelOrder(orderId);
            // 3. 취소 성공 (200 OK)
            return new ResponseEntity<String>("주문 취소 성공", HttpStatus.OK);
        } catch (ResponseStatusException e) {
            // 4. 주문 정보가 없는 경우 (404)
            return new ResponseEntity<String>("취소할 주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            // 5. 기타 오류 (500 Internal Server Error)
            return new ResponseEntity<String>("주문 취소 중 에러가 발생하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // -------------------------------------------------------------------------
    // ⭐️ 4. POST /api/order/orders/bulk-cancel (선택 주문 일괄 취소) - [NEW]
    // -------------------------------------------------------------------------
    @PostMapping("/orders/bulk-cancel")
    public ResponseEntity<?> bulkCancelOrders(
            @RequestBody @Valid OrderIdsDto orderIdsDto,
            BindingResult bindingResult,
            Principal principal) {

        // 1. 유효성 검사
        if (bindingResult.hasErrors()) {
            // DTO 유효성 검사 실패 시 에러 메시지 반환 (400 Bad Request)
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }

        List<Long> orderIds = orderIdsDto.getOrderIds();
       // String email = principal.getName();
        String email ;
        try {
            // SecurityContext에서 인증 객체를 가져옵니다.
            Object principalObject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principalObject instanceof CustomUserDetails) {
                // CustomUserDetails로 형 변환 후 getEmail() 메서드를 사용
                email = ((CustomUserDetails) principalObject).getEmail();
            } else {
                // CustomUserDetails가 아닌 경우, 기본적으로 getName()을 사용 (백업 로직)
                email = principal.getName();
            }
        } catch (Exception e) {
            // 인증 정보가 아예 없는 경우 (토큰이 없거나 만료된 경우), SecurityConfig에서 401이 먼저 처리되지만
            // 안전을 위해 예외 처리
            return new ResponseEntity<>("인증된 사용자 정보가 없습니다.", HttpStatus.UNAUTHORIZED); // 401
        }

        int successCount = 0;
        int failCount = 0;

        for (Long orderId : orderIds) {
            try {
                // 2. 각 주문에 대해 권한 검증 및 취소 시도
                if (orderService.validateOrder(orderId, email)) {
                    orderService.cancelOrder(orderId);
                    successCount++;
                } else {
                    // 권한이 없거나 유효하지 않은 주문 ID
                    failCount++;
                }
            } catch (Exception e) {
                // 이미 취소되었거나 기타 서비스 로직 오류
                failCount++;
            }
        }

        String responseMessage = String.format("총 %d건 중 %d건 주문 취소 성공, %d건 실패.",
                orderIds.size(), successCount, failCount);

        // 3. 일괄 취소 결과 반환 (200 OK)
        return new ResponseEntity<String>(responseMessage, HttpStatus.OK);
    }
}