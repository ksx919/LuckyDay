package com.luckyDay.model.user;

import com.baomidou.mybatisplus.annotation.TableField;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class Captcha implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * uuid
     */
    @NotBlank(message = "uuid为空")
    private String uuid;

    /**
     * 验证码
     */
    @NotBlank(message = "code为空")
    private String code;

    /**
     * 过期时间
     */
    private Date expireTime;

    @TableField(exist = false)
    @Email
    private String email;

}