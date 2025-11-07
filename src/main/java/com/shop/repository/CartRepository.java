package com.shop.repository;
import com.shop.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select; // XML을 사용한다면 Select는 생략 가능

// JPA Repository 대신 MyBatis의 Mapper 인터페이스로 변경
@Mapper
public interface CartRepository {

    // 1. 카트 저장 (JPA의 save() 대체)
    // 이 메서드는 일반적으로 CartMapper.xml 파일에 <insert id="save">로 구현됩니다.
    void save(Cart cart);

    // 2. 회원 ID로 카트 조회 (JPA의 findByMemberId(Long memberId) 대체)
    // 이 메서드는 CartMapper.xml 파일에 <select id="findByMemberId" resultType="Cart">로 구현됩니다.
    Cart findByMemberId(@Param("memberId") Long memberId);

    // 3. ID로 카트 조회 (JPA의 findById() 대체)
    Cart findById(Long cartId);

    // 4. 카트 업데이트 (필요 시)
    void update(Cart cart);

    // 5. 카트 삭제 (필요 시)
    void delete(Long cartId);
}
