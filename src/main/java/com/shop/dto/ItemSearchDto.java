package com.shop.dto;

import com.shop.constant.ItemSellStatus;

public class ItemSearchDto {

    private String searchDateType;

    private ItemSellStatus searchSellStatus;

    private String searchBy;

    private String searchQuery = "";

    // --- Getter Methods ---

    public String getSearchDateType() {
        return searchDateType;
    }

    public ItemSellStatus getSearchSellStatus() {
        return searchSellStatus;
    }

    public String getSearchBy() {
        return searchBy;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    // --- Setter Methods ---

    public void setSearchDateType(String searchDateType) {
        this.searchDateType = searchDateType;
    }

    public void setSearchSellStatus(ItemSellStatus searchSellStatus) {
        this.searchSellStatus = searchSellStatus;
    }

    public void setSearchBy(String searchBy) {
        this.searchBy = searchBy;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }
}