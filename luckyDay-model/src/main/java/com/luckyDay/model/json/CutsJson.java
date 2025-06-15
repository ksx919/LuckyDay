package com.luckyDay.model.json;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:04
 */
@Data
@ToString
public class CutsJson implements Serializable {
    List<DetailsJson> details;
    String suggestion;
    Long offset;
}
