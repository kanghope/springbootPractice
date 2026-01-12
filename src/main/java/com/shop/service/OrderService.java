package com.shop.service;

import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import com.shop.repository.OrderItemRepository; // OrderItemRepository 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional; // Optional 임포트

import com.shop.dto.OrderHistDto;
import com.shop.dto.OrderItemDto;
import com.shop.repository.ItemImgRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort; // Pageable 처리를 위해 Sort import

import org.thymeleaf.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor

public class OrderService {
    private final ItemRepository itemRepository;

    private final MemberRepository memberRepository;

    private final OrderRepository orderRepository;

    private final OrderItemRepository orderItemRepository; // OrderItemRepository 주입

    private final ItemImgRepository itemImgRepository;

    /**
     * 단일 상품 주문 로직
     */
    public Long order(OrderDto orderDto, String email){

        // 1. 상품 및 회원 조회
        Item item = itemRepository.findById(orderDto.getItemId());
        if (item == null) {
            throw new NoSuchElementException("주문 상품을 찾을 수 없습니다. ID: " + orderDto.getItemId());
        }

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("Member not found with email: " + email);
        }

        // 2. OrderItem 생성 및 재고 감소 (OrderItem.createOrderItem에서 처리)
        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item.getId(), item.getPrice(), orderDto.getCount());
        orderItemList.add(orderItem);

        // ⭐ 재고 감소: Item 엔티티를 사용하여 재고를 변경하고, DB에 반영
        item.removeStock(orderDto.getCount());
        itemRepository.updateStock(item); // 👈 ItemRepository를 통해 재고 업데이트 쿼리 호출

        // 3. Order 생성 및 저장 (기존 로직 유지)
        Long memberId = member.getId();
        Order order = Order.createOrder(memberId, orderItemList);
        orderRepository.save(order);

        // 4. OrderItem에 생성된 orderId 설정 및 저장
        for (OrderItem oi : orderItemList) {
            oi.setOrderId(order.getOrderId()); // 생성된 주문 ID 설정
            orderItemRepository.insert(oi); // OrderItem 저장
        }

        return order.getOrderId();
    }

    /**
     * 다중 상품 주문 로직 (MyBatis 버전)
     */
    public Long orders(List<OrderDto> orderDtoList, String email){

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("Member not found with email: " + email);
        }
        Long memberId = member.getId();

        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId());
            if (item == null) {
                throw new NoSuchElementException("주문 상품을 찾을 수 없습니다. ID: " + orderDto.getItemId());
            }

            // ⭐ OrderItem 생성 시 Item 객체 대신 ID와 가격을 전달
            OrderItem orderItem = OrderItem.createOrderItem(item.getId(), item.getPrice(), orderDto.getCount());
            orderItemList.add(orderItem);

            // ⭐ 재고 감소: Item 엔티티를 사용하여 재고를 변경하고, DB에 반영
            item.removeStock(orderDto.getCount());
            itemRepository.updateStock(item); // 👈 ItemRepository를 통해 재고 업데이트 쿼리 호출
        }

        // Order 생성 및 저장
        Order order = Order.createOrder(memberId, orderItemList);
        orderRepository.save(order);

        // OrderItem에 생성된 orderId 설정 및 저장
        for (OrderItem oi : orderItemList) {
            oi.setOrderId(order.getOrderId()); // 생성된 주문 ID 설정
            orderItemRepository.insert(oi); // OrderItem 저장
        }

        return order.getOrderId();
    }

    /**
     * 주문 이력 조회 로직 (MyBatis 버전)
     */
    @Transactional(readOnly = true)
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {

        // Pageable에서 offset과 limit 계산 (Oracle rownum 기반 페이징)
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        // 1. 주문 목록 조회
        List<Order> orders = orderRepository.findOrders(email, offset, limit);

        // 2. 전체 개수 조회
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        // 3. 주문 상품(OrderItem) 정보를 별도로 조회하여 OrderHistDto에 추가
        for (Order order : orders) {
            // 주문에 포함된 OrderItem 목록 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

            // Order 엔티티에 OrderItem 리스트 설정 (getTotalPrice 등 비즈니스 로직용)
            order.setOrderItems(orderItems);

            OrderHistDto orderHistDto = new OrderHistDto(order);

            for (OrderItem orderItem : orderItems) {
                // 대표 이미지 조회
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn
                        (orderItem.getItemId(), "Y"); // Item 객체 대신 ID 사용

                // OrderItemDto 생성
                // ⭐ OrderItemDto의 생성자 대신, 여기서 Item Name 등을 설정해야 합니다.
                // Item Name 조회를 위해 ItemRepository에 findItemNmById(Long itemId) 같은 메서드가 필요합니다.
                // 여기서는 Item 엔티티를 통해 Item Name을 가져온다고 가정합니다.

                Item item = itemRepository.findById(orderItem.getItemId());
                if (item == null) {
                    throw new NoSuchElementException("주문 상품을 찾을 수 없습니다. ID: " + orderItem.getItemId());
                }

                OrderItemDto orderItemDto = new OrderItemDto();
                orderItemDto.setItemNm(item.getItemNm()); // Item 엔티티에서 이름 가져오기
                orderItemDto.setCount(orderItem.getCount());
                orderItemDto.setOrderPrice(orderItem.getOrderPrice());
                orderItemDto.setImgUrl(itemImg != null ? itemImg.getImgUrl() : null);

                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<OrderHistDto>(orderHistDtos, pageable, totalCount);
    }

    /**
     * 주문 소유자 검증 로직 (MyBatis 버전)
     */
    @Transactional(readOnly = true)
    public boolean validateOrder(Long orderId, String email){
        Member curMember = memberRepository.findByEmail(email);
        if (curMember == null) return false;

        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new EntityNotFoundException("Order not found with id: " + orderId);
        }
        Order order = orderOptional.get();

        // Order 엔티티는 Member 객체 대신 memberId를 가지고 있습니다.
        // 따라서 MemberRepository를 통해 주문의 주인인 Member를 조회해야 합니다.
        Member savedMember = memberRepository.findById(order.getMemberId()); // MemberRepository에 findById(Long id)가 있다고 가정


        if (savedMember == null) return false;

        // 이메일 비교
        if(!StringUtils.equals(curMember.getEmail(), savedMember.getEmail())){
            return false;
        }

        return true;
    }

    /**
     * 주문 취소 로직 (MyBatis 버전)
     */
    public void cancelOrder(Long orderId){
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        Order order = orderOptional.orElseThrow(EntityNotFoundException::new);

        // Order 엔티티의 상태만 변경 (DB update 쿼리 호출)
        order.cancelOrder(); // orderStatus = CANCEL로 변경
        orderRepository.updateStatus(order); // Order 테이블의 ORDER_STATUS 컬럼 업데이트

        // ⭐ OrderItem의 재고 복구 로직 (수동 처리)
        // OrderItem 목록을 조회하여 각 상품의 재고를 복구해야 합니다.
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
        for (OrderItem orderItem : orderItems) {
            // Item 엔티티를 조회하여 재고 복구 로직 실행
            Item item = itemRepository.findById(orderItem.getItemId());
            if (item == null) {
                throw new NoSuchElementException("주문 상품을 찾을 수 없습니다. ID: " + orderItem.getItemId());
            }

            item.addStock(orderItem.getCount()); // Item 엔티티에 addStock()이 있다고 가정
            itemRepository.updateStock(item); // ItemRepository를 통해 DB에 재고 반영 (MyBatis 쿼리 필요)
        }
    }


}
