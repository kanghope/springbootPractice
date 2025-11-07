package com.shop.repository;

import com.shop.entity.Order; // Order 엔티티/모델 사용 가정
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
@Mapper // 스프링 부트에서 이 인터페이스를 매퍼로 인식하게 함
public interface OrderRepository {
    /**
     * 특정 이메일로 주문 목록을 조회하고, 페이징 처리를 위해 offset과 limit을 사용합니다.
     * @param email 회원 이메일
     * @param offset 페이지 시작 위치 (건너뛸 개수)
     * @param limit 페이지당 표시할 최대 개수
     * @return 주문 목록
     */
    List<Order> findOrders(@Param("email") String email, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 특정 이메일 회원의 전체 주문 개수를 조회합니다.
     * @param email 회원 이메일
     * @return 전체 주문 개수
     */
    Long countOrder(@Param("email") String email);
}

