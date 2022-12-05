package com.itheima.reggie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orderDetail")
@Slf4j
public class OrderDetailController {
	
	@Autowired
	private OrderDetailService orderDetailService;
}
