package com.shop.entity;
import java.time.LocalDateTime;
public class OrderItem {
    private Long orderItemId; // 주문 상품 ID (DB 컬럼 이름과 일치하도록 변경)

    // JPA의 객체 연관 관계 대신 ID 값을 사용합니다.
    private Long itemId;  // 상품 ID (외래 키)
    private Long orderId; // 주문 ID (외래 키)

    private int orderPrice; // 주문가격

    private int count; // 수량

    // BaseEntity 상속 제거에 따라 필요한 필드를 직접 추가 (추가된 것으로 가정)
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // =======================================================
    // Getter / Setter (Lombok @Getter, @Setter 대체)
    // =======================================================

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(int orderPrice) {
        this.orderPrice = orderPrice;
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

    /**
     * 주문 상품 생성 팩토리 메서드. Item 객체를 받아 가격과 재고 로직을 처리합니다.
     * 주의: MyBatis 환경에서는 Item 객체를 직접 사용하는 대신, ItemService를 통해
     * 가격을 조회하고 재고를 감소시키는 비즈니스 로직을 서비스 계층에서 분리해야 더 명확합니다.
     * 여기서는 기존 JPA 로직을 최대한 유지하되, Order 필드는 비워둡니다.
     */
    public static OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = new OrderItem();

        // Item 객체 대신 ID를 설정합니다.
        orderItem.setItemId(item.getId());
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());

        // ⭐ 재고 감소 로직: MyBatis 환경에서는 이 비즈니스 로직이
        // OrderService에서 ItemMapper를 호출하여 처리되어야 합니다.
        //item.removeStock(count);

        // orderId는 Order 저장 후 서비스 계층에서 명시적으로 설정되어야 합니다.
        return orderItem;
    }

    /**
     * 총 가격 계산 로직 (유지)
     */
    public int getTotalPrice(){
        return orderPrice * count;
    }

    /**
     * 주문 취소 로직.
     * ⭐ 주의: MyBatis 환경에서는 이 메서드 호출이 ItemMapper의 재고 증가 쿼리를 유발하도록
     * 서비스 계층에서 명시적으로 처리해야 합니다.
     */
    public void cancel() {
        // this.getItem().addStock(count);
        // 직접적인 객체 호출 대신, OrderService에서 ItemMapper를 통해 재고를 복구해야 합니다.
    }
}
