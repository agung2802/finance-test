package com.test.api.utils;

import lombok.Data;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-18 14:40
 * @Description: 异常
 */
@Data
public class TestException extends RuntimeException{
    private String code;
    private String message;

    public TestException(){
        super();

    }
    public TestException(String code,String message){
        super(message);
        this.code = code;
        this.message = message;
    }
}
