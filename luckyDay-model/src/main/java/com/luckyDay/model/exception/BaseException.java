package com.luckyDay.model.exception;

import lombok.Data;

@Data
public class BaseException extends RuntimeException {

    String msg;

    public BaseException(String msg){
        this.msg = msg;
    }

}