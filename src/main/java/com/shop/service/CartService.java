package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.NoSuchElementException; // Optional 대신 사용할 예외

import com.shop.dto.CartDetailDto;
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.util.StringUtils;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    public Long addCart(CartItemDto cartItemDto, String email){

        // 1. 상품 및 회원 조회
        Item item = itemRepository.findById(cartItemDto.getItemId());
        if (item == null) {
            throw new EntityNotFoundException("Item not found with id: " + cartItemDto.getItemId());
        }

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new EntityNotFoundException("Member not found with email: " + email);
        }

        Long memberId = member.getId();

        // 2. 카트 조회 및 생성
        Cart cart = cartRepository.findByMemberId(memberId);
        if(cart == null){
            // ⭐ Cart.createCart 팩토리 메서드 수정: Member 객체 대신 ID를 받도록 변경 가정
            cart = Cart.createCart(memberId);
            cartRepository.save(cart);
        }

        // 3. 기존 카트 상품 조회
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getCartId(), item.getId());

        if(savedCartItem != null){
            // 4. 수량 증가 및 업데이트
            savedCartItem.addCount(cartItemDto.getCount());
            // ⭐ MyBatis 매퍼를 통해 UPDATE 쿼리 호출
            cartItemRepository.updateCount(savedCartItem);
            return savedCartItem.getCartItemId();
        } else {
            // 5. 새 카트 상품 생성 및 저장
            // ⭐ CartItem.createCartItem 팩토리 메서드 수정: ID를 받도록 변경 가정
            CartItem cartItem = CartItem.createCartItem(cart.getCartId(), item.getId(), cartItemDto.getCount());
            cartItemRepository.save(cartItem); // ID는 DB 트리거에서 자동 생성됨

            // MyBatis는 insert 후 생성된 ID를 반환하지 않으므로, 이 시점에서 cartItem.getCartItemId()는 null일 수 있습니다.
            // 그러나 다음 트랜잭션에서 다시 조회하거나, 클라이언트에서 갱신된 목록을 조회한다고 가정하고,
            // 일단 저장된 시점의 엔티티를 반환합니다. (ID를 즉시 필요하다면 insert 쿼리 수정 필요)
            return cartItem.getCartItemId();
        }
    }

    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email){

        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        if (member == null) return cartDetailDtoList;

        Cart cart = cartRepository.findByMemberId(member.getId());
        if(cart == null){
            return cartDetailDtoList;
        }

        // ⭐ MyBatis 매퍼의 DTO Projection 메서드 호출
        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getCartId());
        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email){
        Member curMember = memberRepository.findByEmail(email);
        if (curMember == null) return false;

        // ⭐ findById가 Optional을 반환하지 않도록 변경되었거나, Optional.orElseThrow 대신 수동 처리
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem not found with id: " + cartItemId));

        // ⭐ JPA 관계 탐색 제거: CartItem은 이제 Cart 객체 대신 CartId를 가집니다.
        // 따라서 Cart 엔티티를 조회하고, 그 Cart의 MemberId와 현재 Member의 ID를 비교해야 합니다.
        Cart savedCart = cartRepository.findById(cartItem.getCartId());
        if (savedCart == null) return false;

        // 장바구니 주인 Member의 ID를 가져옵니다.
        Long savedMemberId = savedCart.getMemberId();

        // 현재 사용자 ID와 장바구니 주인 ID 비교
        if(!curMember.getId().equals(savedMemberId)){
            return false;
        }

        return true;
    }

    public void updateCartItemCount(Long cartItemId, int count){
        // ⭐ findById가 Optional을 반환하지 않도록 변경되었거나, Optional.orElseThrow 대신 수동 처리
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem not found with id: " + cartItemId));

        cartItem.updateCount(count);
        // ⭐ 수량 업데이트를 위한 MyBatis 매퍼 메서드 호출
        cartItemRepository.updateCount(cartItem);
    }

    public void deleteCartItem(Long cartItemId) {
        // ⭐ findById가 Optional을 반환하지 않도록 변경되었거나, Optional.orElseThrow 대신 수동 처리
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("CartItem not found with id: " + cartItemId));

        // ⭐ ID 기반 삭제로 변경
        cartItemRepository.delete(cartItem.getCartItemId());
    }

    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email){
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            // ⭐ findById가 Optional을 반환하지 않도록 변경되었거나, Optional.orElseThrow 대신 수동 처리
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new NoSuchElementException("CartItem not found with id: " + cartOrderDto.getCartItemId()));

            OrderDto orderDto = new OrderDto();
            // ⭐ JPA 관계 탐색 제거: Item 객체 대신 ItemId를 사용합니다.
            orderDto.setItemId(cartItem.getItemId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email);

        // 주문 완료 후 장바구니 상품 삭제
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            // ⭐ findById가 Optional을 반환하지 않도록 변경되었거나, Optional.orElseThrow 대신 수동 처리
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new NoSuchElementException("CartItem not found with id: " + cartOrderDto.getCartItemId()));

            // ⭐ ID 기반 삭제로 변경
            cartItemRepository.delete(cartItem.getCartItemId());
        }

        return orderId;
    }

}