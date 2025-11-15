package com.shop.entity;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.exception.OutOfStockException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

//@Getter
//@Setter
//@ToString
public class Item {
    private Long id;            // 상품 코드
    private String itemNm;      // 상품명
    private int price;          // 가격
    private int stockNumber;    // 재고수량
    private String itemDetail;  // 상품 상세 설명
    private ItemSellStatus itemSellStatus; // 상품 판매 상태
    private LocalDateTime regTime;  // 등록시간
    private LocalDateTime updateTime; // 수정시간
    private String createdBy;//등록자
    private String modifiedBy;//수정자

    // =======================================================
    // Getter 메서드 (@Getter 대체)
    // =======================================================
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemNm() {
        return itemNm;
    }

    public void setItemNm(String itemNm) {
        this.itemNm = itemNm;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getStockNumber() {
        return stockNumber;
    }

    public void setStockNumber(int stockNumber) {
        this.stockNumber = stockNumber;
    }

    public String getItemDetail() {
        return itemDetail;
    }

    public void setItemDetail(String itemDetail) {
        this.itemDetail = itemDetail;
    }

    public ItemSellStatus getItemSellStatus() {
        return itemSellStatus;
    }

    public void setItemSellStatus(ItemSellStatus itemSellStatus) {
        this.itemSellStatus = itemSellStatus;
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
    // toString() 메서드 (@ToString 대체)
    // =======================================================

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", itemNm='" + itemNm + '\'' +
                ", price=" + price +
                ", stockNumber=" + stockNumber +
                ", itemDetail='" + itemDetail + '\'' +
                ", itemSellStatus=" + itemSellStatus +
                ", regTime=" + regTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public void updateItem(ItemFormDto itemFormDto)
    {
        this.itemNm = itemFormDto.getItemNm();
        this.price = itemFormDto.getPrice();
        this.stockNumber = itemFormDto.getStockNumber();
        this.itemDetail = itemFormDto.getItemDetail();
        this.itemSellStatus = itemFormDto.getItemSellStatus();
        this.modifiedBy = itemFormDto.getModifiedBy();
    }

    public void removeStock(int stockNumber){
        int restStock = this.stockNumber - stockNumber;
        if(restStock<0){
            throw new OutOfStockException("상품의 재고가 부족 합니다. (현재 재고 수량: " + this.stockNumber + ")");
        }
        this.stockNumber = restStock;
    }

    public void addStock(int stockNumber){
        this.stockNumber += stockNumber;
    }

}
