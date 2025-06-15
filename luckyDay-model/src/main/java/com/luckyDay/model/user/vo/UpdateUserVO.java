package com.luckyDay.model.user.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserVO {

    @NotBlank(message = "昵称不可为空")
    private String nickName;

    private Long avatar;

    private Boolean sex;

    private String description;

    private Long defaultFavoritesId;



}
