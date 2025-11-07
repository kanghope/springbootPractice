package com.shop.entity;

import com.shop.constant.ItemSellStatus; // 필요하다면
import java.time.LocalDateTime;

public class Cart {

    private Long cartId; // 카트 ID (JPA의 id -> MyBatis의 cartId)
    private Long memberId; // 회원 ID (외래 키, JPA에서는 Member 객체였으나 DB 컬럼 값으로 변경)

    // BaseEntity에서 상속받던 필드를 직접 추가 (필요한 경우)
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // =======================================================
    // Getter / Setter / toString (Lombok 대체)
    // =======================================================

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getRegTime() {
        return regTime;
    }

    public void setRegTime(LocalDateTime regTime) {
        this.regTime = regTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "cartId=" + cartId +
                ", memberId=" + memberId +
                ", regTime=" + regTime +
                ", updateTime=" + updateTime +
                '}';
    }
    // =======================================================
    // 팩토리 메서드 (MyBatis에서도 재사용 가능)
    // =======================================================

    // JPA와 달리 Member 객체를 받지 않고, DB 컬럼 값인 memberId를 직접 받는 것이 일반적입니다.
    public static Cart createCart(Long memberId){
        Cart cart = new Cart();
        cart.setMemberId(memberId);
        // 등록 시간은 DB에서 자동 생성되므로 여기서는 설정하지 않습니다.
        return cart;
    }
}