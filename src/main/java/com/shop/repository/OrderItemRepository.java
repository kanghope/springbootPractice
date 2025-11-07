package com.shop.repository;

import com.shop.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;
@Mapper // MyBatis 매퍼임을 나타냅니다.
public interface OrderItemRepository {
    // 1. ID로 조회 (findById)
    Optional<OrderItem> findById(Long id);

    // 2. 전체 조회 (findAll)
    List<OrderItem> findAll();

    // 3. 등록 (save)
    int insert(OrderItem orderItem);

    // 4. 수정 (save)
    int update(OrderItem orderItem);

    // 5. ID로 삭제 (deleteById)
    int deleteById(Long id);

    // 6. ID 존재 여부 확인 (existsById)
    boolean existsById(Long id);
}
