package com.shop.repository;

import com.shop.entity.CartItem;
import com.shop.dto.CartDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Mapper // MyBatis 매퍼 인터페이스로 지정
public interface CartItemRepository {

    // 1. 등록 (JPA의 save() 대체, XML에 <insert>로 구현)
    void save(CartItem cartItem);

    // 2. ID로 조회 (Optional 반환)
    Optional<CartItem> findById(Long cartItemId);

    // 3. 카트 ID와 상품 ID로 조회 (기존 JPA 메서드와 동일)
    CartItem findByCartIdAndItemId(@Param("cartId") Long cartId, @Param("itemId") Long itemId);

    // 4. 삭제 (JPA의 delete(entity) 대체, XML에 <delete>로 구현)
    void delete(Long cartItemId); // 엔티티 대신 ID를 받는 것이 MyBatis에서 일반적입니다.

    // 5. 수량 업데이트 (비즈니스 로직)
    void updateCount(CartItem cartItem);

    // 6. 장바구니 상세 목록 조회 (DTO Projection Query)
    // 이 메서드는 XML에서 복잡한 조인 쿼리로 구현되어야 합니다.
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
    //List<CartDetailDto> findCartDetailDtoList(Map<String, Object> paramMap);
}