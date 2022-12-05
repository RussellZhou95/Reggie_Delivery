package com.itheima.reggie.common;



import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice(annotations= {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
	/**
	 * 异常处理方法
	 * @param ex
	 * @return
	 */
	@ExceptionHandler(SQLIntegrityConstraintViolationException.class)
	public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
		log.error(ex.getMessage());

		if(ex.getMessage().contains("Duplicate entry")) {
			String []split=ex.getMessage().split(" ");
			String errorMessage=split[2]+" 已存在";
			return R.error(errorMessage);
		}
		
		return R.error("未知错误");
	}
	
    /**
     * 异常处理方法
     * @return
     */
	@ExceptionHandler(CustomException.class)
	public R<String> exceptionHandler(CustomException ex){
		log.error(ex.getMessage());

		
		return R.error(ex.getMessage());
	}
}
