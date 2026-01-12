package com.shop.repository;
import com.shop.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository // 스프링 빈으로 등록
public interface ItemRepository {

    // 💡 참고: JpaRepository의 기본 CRUD 메서드도 필요하면 여기에 추가해야 합니다.
    void save(Item item);
    void update(Item item);
    Item findById(Long id);
    void delete(Long id);

    // ⭐ 추가: 재고만 업데이트하는 메서드 정의 (MyBatis 쿼리 작성이 필요함)
    int updateStock(Item item);

    // 1. findByItemNm
    List<Item> findByItemNm(String itemNm);

    // 2. findByItemNmOrItemDetail
    List<Item> findByItemNmOrItemDetail(@Param("itemNm") String itemNm, @Param("itemDetail") String itemDetail);

    // 3. findByPriceLessThan
    List<Item> findByPriceLessThan(Integer price);

    // 4. findByPriceLessThanOrderByPriceDesc
    List<Item> findByPriceLessThanOrderByPriceDesc(Integer price);

    // 5. findByItemDetail (@QueryJPQL)
    List<Item> findByItemDetail(@Param("itemDetail") String itemDetail);

    // 6. findByItemDetailByNative (@QueryNative)
    List<Item> findByItemDetailByNative(@Param("itemDetail") String itemDetail);

    // 7. ItemRepositoryCustom의 메서드들도 여기에 추가되어야 합니다.
    // (예: getAdminItemPage, getMainItemPage 등)
    // List<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable)
}
