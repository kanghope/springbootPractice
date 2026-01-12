package com.shop.controller;

import com.shop.config.CustomUserDetails;
import com.shop.dto.CartItemDto;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*; // RestController, RequestMapping 등을 사용하기 위해 * 임포트

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // 유효성 검사 에러 처리를 위해 추가

import com.shop.dto.CartDetailDto;
import org.springframework.ui.Model; // GET /api/cart (주문 이력)에서만 사용됩니다.
import org.springframework.context.support.DefaultMessageSourceResolvable; // 유효성 검사 에러 처리를 위해 추가

import com.shop.dto.CartOrderDto;

@RestController // 1. @Controller + @ResponseBody 역할
@RequiredArgsConstructor
@RequestMapping("/api/cart") // 2. 클래스 레벨의 요청 매핑 추가
public class CartController {

    private final CartService cartService;

    // -------------------------------------------------------------------------
    // 1. POST /api/cart (장바구니에 상품 추가)
    // -------------------------------------------------------------------------
    @PostMapping(value = "") // 기존: /cart
    public ResponseEntity<?> addCart(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal){

        // OrderController와 유사하게 Map 형태로 에러 반환 (개선된 에러 처리)
        if(bindingResult.hasErrors()){
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
       // String email = principal.getName();
        Long cartItemId;

        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // 2. GET /api/cart (장바구니 목록 조회 - 웹 페이지 렌더링용)
    // -------------------------------------------------------------------------
    // NOTE: @RestController를 사용했기 때문에 이 메서드만 @Controller의 역할을 수행해야 합니다.
    // HTML 뷰를 반환하려면 이 메서드만 @ResponseBody를 제외하고 클래스에 @Controller와 @ResponseBody를 분리해야 하지만,
    // 클라이언트 API 요청 처리 일관성을 위해 이 메서드는 API 응답 (JSON)을 반환하도록 변경하는 것이 RESTful합니다.

    // 만약 웹 페이지 렌더링이 필요하다면, 아래 주석을 참고하여 별도의 Controller를 사용하거나 @Controller를 유지해야 합니다.

    @GetMapping(value = "") // 기존: /cart
    public ResponseEntity<?> getCartList(Principal principal){

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
        List<CartDetailDto> cartDetailList = cartService.getCartList(email);
        return ResponseEntity.ok(cartDetailList);
    }

    /* // 만약 여전히 뷰 템플릿을 반환해야 한다면, 이 컨트롤러는 @RestController 대신 @Controller를 유지하고,
    // 이 메서드만 @ResponseBody가 없어야 하며, 나머지 메서드에 @ResponseBody를 붙여야 합니다.
    @GetMapping(value = "")
    public String orderHist(Principal principal, Model model){
        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";
    }
    */


    // -------------------------------------------------------------------------
    // 3. PATCH /api/cart/items/{cartItemId} (장바구니 상품 수량 변경)
    // -------------------------------------------------------------------------
    @PatchMapping(value = "/items/{cartItemId}") // 기존: /cartItem/{cartItemId}
    public ResponseEntity<String> updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                 @RequestParam int count, // 쿼리 파라미터로 count 받기
                                                 Principal principal){

        if(count <= 0){
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
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
        //String email = principal.getName();

        if(!cartService.validateCartItem(cartItemId, email)){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        try {
            cartService.updateCartItemCount(cartItemId, count);
            return new ResponseEntity<String>("수량 변경 성공", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("장바구니 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
    }

    // -------------------------------------------------------------------------
    // 4. DELETE /api/cart/items/{cartItemId} (장바구니 상품 삭제)
    // -------------------------------------------------------------------------
    @DeleteMapping(value = "/items/{cartItemId}") // 기존: /cartItem/{cartItemId}
    public ResponseEntity<String> deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal){

        //String email = principal.getName();
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
        if(!cartService.validateCartItem(cartItemId, email)){
            return new ResponseEntity<String>("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        try {
            cartService.deleteCartItem(cartItemId);
            return new ResponseEntity<String>("장바구니 상품 삭제 성공", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("장바구니 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
        }
    }

    // -------------------------------------------------------------------------
    // 5. POST /api/cart/orders (장바구니 상품 주문)
    // -------------------------------------------------------------------------
    @PostMapping(value = "/orders") // 기존: /cart/orders
    public ResponseEntity<?> orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal){

        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
        //String email = principal.getName();
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

        if(cartOrderDtoList == null || cartOrderDtoList.isEmpty()){
            return new ResponseEntity<String>("주문할 상품을 선택해주세요", HttpStatus.BAD_REQUEST); // 400 Bad Request가 더 적절합니다.
        }

        // 권한 검증
        for (CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), email)){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId;
        try {
            orderId = cartService.orderCartItem(cartOrderDtoList, email);
            return new ResponseEntity<Long>(orderId, HttpStatus.OK);
        } catch (Exception e) {
            // 재고 부족 등 서비스 오류 처리
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}