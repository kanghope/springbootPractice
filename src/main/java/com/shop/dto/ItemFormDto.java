package com.shop.dto;
import com.shop.constant.ItemSellStatus;
import com.shop.entity.Item;
import org.modelmapper.ModelMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ItemFormDto {
    private Long id;

    @NotBlank(message = "상품명은 필수 입력 값입니다.")
    private String itemNm;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    private Integer price;

    @NotBlank(message = "상품 상세는 필수 입력 값입니다.")
    private String itemDetail;

    @NotNull(message = "재고는 필수 입력 값입니다.")
    private Integer stockNumber;

    private ItemSellStatus itemSellStatus;

    private List<ItemImgDto> itemImgDtoList = new ArrayList<>();

    private List<Long> itemImgIds = new ArrayList<>();

    private static ModelMapper modelMapper = new ModelMapper();

    private String createdBy;//등록자
    private String modifiedBy;//수정자

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
    // ==========================================================
    // 💡 Getter Methods (수동 작성)
    // ==========================================================

    public Long getId() {
        return id;
    }

    public String getItemNm() {
        return itemNm;
    }

    public Integer getPrice() {
        return price;
    }

    public String getItemDetail() {
        return itemDetail;
    }

    public Integer getStockNumber() {
        return stockNumber;
    }

    public ItemSellStatus getItemSellStatus() {
        return itemSellStatus;
    }

    public List<ItemImgDto> getItemImgDtoList() {
        return itemImgDtoList;
    }

    public List<Long> getItemImgIds() {
        return itemImgIds;
    }

    // ==========================================================
    // 💡 Setter Methods (수동 작성)
    // ==========================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setItemNm(String itemNm) {
        this.itemNm = itemNm;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setItemDetail(String itemDetail) {
        this.itemDetail = itemDetail;
    }

    public void setStockNumber(Integer stockNumber) {
        this.stockNumber = stockNumber;
    }

    public void setItemSellStatus(ItemSellStatus itemSellStatus) {
        this.itemSellStatus = itemSellStatus;
    }

    public void setItemImgDtoList(List<ItemImgDto> itemImgDtoList) {
        this.itemImgDtoList = itemImgDtoList;
    }

    public void setItemImgIds(List<Long> itemImgIds) {
        this.itemImgIds = itemImgIds;
    }

    public Item createItem(){
        return modelMapper.map(this, Item.class);
    }

    public static ItemFormDto of(Item item){
        return modelMapper.map(item,ItemFormDto.class);
    }
}
