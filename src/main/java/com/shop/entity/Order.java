package com.shop.entity;
import com.shop.constant.OrderStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private Long orderId; // 주문 ID (DB 컬럼 이름과 일치하도록 변경)

    // JPA의 Member member 객체 대신 DB 컬럼 값인 Long 타입 ID를 사용합니다.
    private Long memberId; // 회원 ID (외래 키)

    private LocalDateTime orderDate; // 주문일

    // EnumType.STRING 대신 OrderStatus Enum 자체를 필드로 유지합니다.
    private OrderStatus orderStatus; // 주문상태

    // MyBatis는 1:N 관계를 자동으로 매핑하지 않으므로, 이 리스트는 DB 연동보다는 비즈니스 로직(예: getTotalPrice)을 위해 유지됩니다.
    // 실제 DB 데이터 로딩은 OrderItemMapper에서 별도로 수행됩니다.
    private List<OrderItem> orderItems = new ArrayList<>();

    // BaseEntity 상속 제거에 따라 필요한 필드를 직접 추가 (추가된 것으로 가정)
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // =======================================================
    // Getter / Setter (Lombok 대체)
    // =======================================================

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    // regTime, updateTime Getter/Setter (필요시 추가)
    // ...

    // =======================================================
    // 비즈니스 및 팩토리 메서드 (MyBatis 환경에 맞게 수정)
    // =======================================================

    /**
     * JPA에서는 orderItem.setOrder(this)를 통해 양방향 관계를 설정했지만,
     * MyBatis에서는 단순히 리스트에 추가하는 역할만 합니다.
     * 실제 DB 저장 로직은 OrderMapper와 OrderItemMapper에서 분리됩니다.
     */
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        //orderItem.setOrder(this);// 로직은 OrderItem에 Order 객체 대신 OrderId 필드를 설정하는 것으로 대체되어야 합니다.
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    /**
     * 주문 생성 팩토리 메서드. Long memberId를 받도록 수정합니다.
     * OrderItem 리스트의 OrderId 설정은 서비스 계층에서 수행되어야 합니다.
     */
    public static Order createOrder(Long memberId, List<OrderItem> orderItemList) {
        Order order = new Order();
        order.setMemberId(memberId); // Member 객체 대신 ID 설정

        for(OrderItem orderItem : orderItemList) {
            order.addOrderItem(orderItem);
            // ⭐ 참고: 실제 DB 저장을 위해 OrderItem 객체에 OrderId를 설정하는 로직이 서비스 계층에 필요합니다.
        }

        order.setOrderStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    /**
     * 총 가격 계산 로직 (유지)
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for(OrderItem orderItem : orderItems){
            // OrderItem 클래스에 getTotalPrice() 메서드가 존재해야 합니다.
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

    /**
     * 주문 취소 로직 (유지)
     */
    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
        for (OrderItem orderItem : orderItems) {
            // OrderItem 클래스에 cancel() 메서드가 존재해야 합니다.
            orderItem.cancel();
        }
    }
}
