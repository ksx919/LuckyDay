package com.luckyDay.model.user.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class FindPWVO {

    @Email(message = "邮箱格式不正确")
    String email;

    @NotNull(message = "code不能为空")
    Integer code;

    @NotBlank(message = "新密码不能为空")
    String newPassword;
}