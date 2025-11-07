package com.shop.entity;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime; // BaseEntity 필드 가정
//@Getter @Setter
public class ItemImg {
    private Long id;          // item_img_id
    private String imgName;   // 이미지 파일명
    private String oriImgName;// 원본 이미지 파일명
    private String imgUrl;    // 이미지 조회 경로
    private String repImgYn;  // 대표 이미지 여부

    // 외래 키 (item_id)
    private Long itemId;

    // BaseEntity의 필드 (감사 필드 가정)
    private LocalDateTime regTime;
    private LocalDateTime updateTime;

    // ==========================================================
    // 💡 Getter와 Setter (수동 작성)
    // ==========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public String getOriImgName() {
        return oriImgName;
    }

    public void setOriImgName(String oriImgName) {
        this.oriImgName = oriImgName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getRepImgYn() {
        return repImgYn;
    }

    public void setRepImgYn(String repImgYn) {
        this.repImgYn = repImgYn;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
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

    //마이바티스 환경을 위하 setItem 메서드 추가
    //Item 엔티티를 받아 해당 ID를 itemId필드에 설정합니다.
    public void setItem(Item item)
    {
        //Item 객체 자체를 저장하는 것이 아니라, 욀래 키인 ID를 저장합니다.
        if(item != null){
            this.itemId = item.getId();
        }
        else {
            this.itemId = null;
        }

    }
    // ==========================================================
    // 💡 기존 비즈니스 로직 메소드 (유지)
    // ==========================================================

    public void updateItemImg(String oriImgName, String imgName, String imgUrl){
        this.oriImgName = oriImgName;
        this.imgName = imgName;
        this.imgUrl = imgUrl;
    }
}
