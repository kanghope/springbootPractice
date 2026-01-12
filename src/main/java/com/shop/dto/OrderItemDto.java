package com.shop.dto;

import com.shop.entity.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderItemDto {

    /*
    public OrderItemDto(OrderItem orderItem, String imgUrl){
        this.itemNm = orderItem.getOrderItemId().getItemNm();
        this.count = orderItem.getCount();
        this.orderPrice = orderItem.getOrderPrice();
        this.imgUrl = imgUrl;
    }
*/
    private String itemNm; //상품명
    private int count; //주문 수량

    private int orderPrice; //주문 금액
    private String imgUrl; //상품 이미지 경로

    // 생성자 제거 또는 비워둠 (Mapper나 Service에서 직접 설정할 것이므로)
    public OrderItemDto() {}

}