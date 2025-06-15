package com.luckyDay.model.json;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:01
 */
@Data
@ToString
public class ScenesJson implements Serializable {
    private TypeJson terror;
    private TypeJson politician;
    private TypeJson pulp;
    private TypeJson antispam;

}
