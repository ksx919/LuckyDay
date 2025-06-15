package com.luckyDay.model.json;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 02:28
 */
@Data
@ToString
public class ResultChildJson implements Serializable {
    String suggestion;
    ScenesJson scenes;
}
