package com.test.api.controller;

import com.test.api.utils.Response;
import com.test.api.utils.TestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author: Faisal Mulya Santosa
 * @create: 2023-05-18 15:29
 * @Description: Unified handling of exceptions
 */
@Slf4j
@RestControllerAdvice
public class DefaultExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public Response serverError(Exception e){
        String msg = "";
        if(e instanceof NullPointerException){
            log.error("{}",e);
            msg = "A null pointer exception occurred";
        }else if(e instanceof RuntimeException){
            log.error("{}",e);
            msg = "A runtime exception occurred";
        }else{
            log.error("{}",e);
            msg = "An unknown exception occurred";
        }
        return new Response("300",msg,null);
    }

    @ExceptionHandler(value = TestException.class)
    public Response  paramError(TestException e){
        log.error("{}",e);
        return  new Response(e.getCode(),e.getMessage(),null);
    }
}
