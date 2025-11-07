package com.shop.repository;
import com.shop.entity.ItemImg;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper // MyBatis 매퍼임을 지정합니다. (Spring Boot 설정에 따라 @Repository 대신 사용 가능)
@Repository // 스프링 빈으로 등록합니다.

public interface ItemImgRepository {

    /**
     * 상품 ID(itemId)를 기준으로 상품 이미지들을 ID 오름차순으로 조회합니다.
     */
    List<ItemImg> findByItemIdOrderByIdAsc(Long itemId);

    /**
     * 상품 ID와 대표 이미지 여부(repimgYn)를 기준으로 상품 이미지를 조회합니다.
     */
    ItemImg findByItemIdAndRepimgYn(Long itemId, String repimgYn);

    // 💡 참고: JpaRepository에 있던 기본적인 CRUD 기능(save, findById 등)은
    // Mapper XML에 insert, select, update, delete 태그로 별도 정의해야 합니다.

    // 예시: 상품 이미지 저장 (insert)
    void save(ItemImg itemImg);

    //상품 이미지 수정(update) 파라미터로 ItemImg 객체를 받습니다.
    void update(ItemImg itemimg);

    // 예시: 상품 이미지 ID로 조회 (select)
    ItemImg findById(Long id);
}
