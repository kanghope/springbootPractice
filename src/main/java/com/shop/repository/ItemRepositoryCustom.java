package com.shop.repository;
import com.shop.dto.ItemSearchDto;
//import com.shop.dto.MainItemDto;
import com.shop.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper // MyBatis Mapper 임을 명시
public interface ItemRepositoryCustom {
    // 1. 어드민 페이지 상품 목록 조회
    List<Item> getAdminItemPage(
            @Param("searchDto") ItemSearchDto itemSearchDto,
            @Param("offset") long offset,
            @Param("limit") int limit
    );

    // 2. 어드민 페이지 상품 전체 카운트 조회
    long getAdminItemCount(@Param("searchDto") ItemSearchDto itemSearchDto);
/*
    // 3. 메인 페이지 상품 목록 조회
    List<MainItemDto> getMainItemPage(
            @Param("searchDto") ItemSearchDto itemSearchDto,
            @Param("offset") long offset,
            @Param("limit") int limit
    );
*/
    // 4. 메인 페이지 상품 전체 카운트 조회
    long getMainItemCount(@Param("searchDto") ItemSearchDto itemSearchDto);
}
