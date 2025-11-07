package com.shop.dto;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Getter @Setter
public class MemberFormDto {

    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식으로 입력해주세요.")
    private String email;

    @NotEmpty(message = "비밀번호는 필수 입력값입니다.")
    @Length(min=8,max=16, message = "비밀번호는 8자 이상, 16자 이하로 입력해주세요")
    // 복잡한 비밀번호 규칙을 적용하는 @Pattern 어노테이션 추가
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`])(?=\\S+$).{8,16}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotEmpty(message = "주소는 필수 입력값입니다.")
    private String address;
    private String createdBy; // 필드가 정의되어 있어야 합니다.
    private String modifiedBy; // 수정자
    private java.time.LocalDateTime regTime; // 등록 시간
    private java.time.LocalDateTime updateTime; // 수정 시간
}
