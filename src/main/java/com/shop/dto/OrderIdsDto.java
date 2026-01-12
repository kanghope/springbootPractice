package com.shop.dto;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter @Setter
public class OrderIdsDto {
    @NotEmpty(message = "취소할 주문 ID를 하나 이상 선택해주세요.")
    private List<Long> orderIds;
}
