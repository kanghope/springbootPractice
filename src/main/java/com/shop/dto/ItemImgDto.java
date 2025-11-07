package com.shop.dto;
import com.shop.entity.ItemImg;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;
public class ItemImgDto {
    private Long id;

    private String imgName;

    private String oriImgName;

    private String imgUrl;

    private String repImgYn;

    private static ModelMapper modelMapper = new ModelMapper();


    // ==========================================================
    // 💡 Getter Methods (수동 작성)
    // ==========================================================

    public Long getId() {
        return id;
    }

    public String getImgName() {
        return imgName;
    }

    public String getOriImgName() {
        return oriImgName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getRepImgYn() {
        return repImgYn;
    }
    // ==========================================================
    // 💡 Setter Methods (수동 작성)
    // ==========================================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public void setOriImgName(String oriImgName) {
        this.oriImgName = oriImgName;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setRepImgYn(String repImgYn) {
        this.repImgYn = repImgYn;
    }

    // ==========================================================
    // 💡 DTO 변환 메소드 (유지)
    // ==========================================================

    /**
     * MyBatis 모델 객체를 DTO로 변환하는 정적 메소드
     * @param itemImg MyBatis에서 조회된 ItemImg 모델 객체
     * @return ItemImgDto
     */
    public static ItemImgDto of(ItemImg itemImg) {
//        if(itemImg == null)
//        {
//            return null;
//        }
        return modelMapper.map(itemImg, ItemImgDto.class);
    }
}
