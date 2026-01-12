package com.shop.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // 💡 기본 생성자 추가용

@Getter @Setter
@NoArgsConstructor // 💡 기본 생성자 추가
public class MainItemDto {

    private Long id;

    private String itemNm;

    private String itemDetail;

    private String imgUrl;

    private Integer price;


    public MainItemDto(Long id, String itemNm, String itemDetail, String imgUrl,Integer price){
        this.id = id;
        this.itemNm = itemNm;
        this.itemDetail = itemDetail;
        this.imgUrl = imgUrl;
        this.price = price;
    }
}
