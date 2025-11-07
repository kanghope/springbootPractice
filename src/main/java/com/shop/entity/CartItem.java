package com.shop.entity;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter

public class CartItem {


    private Long cartItemId; // 카트 상품 ID (DB 컬럼 이름과 일치하도록 변경)

    // 외래 키 관계 필드를 객체 대신 ID 값으로 변경합니다.
    private Long cartId; // 카트 ID (Long)
    private Long itemId; // 상품 ID (Long)

    private int count; // 상품 수량

    // BaseEntity를 상속받지 않으므로, 필드를 직접 추가합니다.
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // =======================================================
    // Getter / Setter (Lombok @Getter, @Setter 대체)
    // =======================================================

    public Long getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    // =======================================================
    // 팩토리 및 비즈니스 메서드 (ID 기반으로 수정)
    // =======================================================

    // MyBatis 형식에 맞게 ID 값을 받도록 수정합니다.
    public static CartItem createCartItem(Long cartId, Long itemId, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCartId(cartId); // Cart 객체 대신 ID 설정
        cartItem.setItemId(itemId); // Item 객체 대신 ID 설정
        cartItem.setCount(count);
        // regTime, updateTime은 DB에서 SYSDATE로 자동 처리된다고 가정합니다.
        return cartItem;
    }

    public void addCount(int count){
        this.count += count;
    }

    public void updateCount(int count){
        this.count = count;
    }

}