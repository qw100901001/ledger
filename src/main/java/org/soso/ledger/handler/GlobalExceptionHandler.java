package org.soso.ledger.handler;

import org.soso.ledger.common.Result;
import org.soso.ledger.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }
    // 2. 捕获参数校验异常 (Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(40000, msg);
    }

    // 3. 新增：捕获并发撞名兜底异常 (数据库唯一索引 uk_username 抛出的)
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e) {
        // 并发情况下，两个请求同时 insert，数据库唯一索引会拦住其中一个
        // 这里和 Service 层提前校验的提示语保持一致，前端无感知
        return Result.error(40002, "用户名已存在");
    }

    // 4. 兜底捕获其他异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace();
        return Result.error(500, "系统异常: " + e.getMessage());
    }
}