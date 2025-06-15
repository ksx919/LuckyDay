package com.luckyDay.model.json;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 02:14
 */
@Data
@ToString
public class ResultJson implements Serializable {
    Integer code;
    String message;
    ResultChildJson result;
}


