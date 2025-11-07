package com.shop.entity;

import lombok.*;
import java.util.Date; // Oracle의 DATE 타입과 매핑

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private Long memberId;      // MEMBER_ID 컬럼과 매핑
    private String refreshToken; // REFRESH_TOKEN 컬럼과 매핑
    private Date expiryDate;    // EXPIRY_DATE 컬럼과 매핑
    // private Date createdDate; // CREATED_DATE 컬럼과 매핑 (선택적)
}