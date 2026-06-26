package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.Result;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
@Hidden
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result errow(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }
}
