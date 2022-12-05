package com.itheima.reggie.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;

import lombok.extern.slf4j.Slf4j;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
	
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
	@PostMapping("/add")
	public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
		
		//设置用户id，指定当前是哪个用户的购物车数据
		Long userId=BaseContext.getCurrentId();
		shoppingCart.setUserId(userId);
		
		Long dishId=shoppingCart.getDishId();

		LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
		
		queryWrapper.eq(ShoppingCart::getUserId, userId);
		
		if(dishId!=null) {
			//添加到购物车的是菜品
			queryWrapper.eq(ShoppingCart::getDishId, dishId);
		}else {
			queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
		}
		
		//查询当前菜品或者套餐是否在购物车中
		//SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
		ShoppingCart shoppingcartOne=shoppingCartService.getOne(queryWrapper);
		if(shoppingcartOne!=null) {
			//如果已经存在，就在原来数量基础上加一
			Integer  number=shoppingcartOne.getNumber();
			shoppingcartOne.setNumber(number+1);
			shoppingCartService.updateById(shoppingcartOne);
		}else {
			//如果不存在，则添加到购物车，数量默认就是一
			shoppingCart.setNumber(1);
			shoppingCart.setCreateTime(LocalDateTime.now());
			shoppingCartService.save(shoppingCart);
			shoppingcartOne=shoppingCart;
		}
		
		return R.success(shoppingcartOne);
	}
	
    /**
     * 查看购物车
     * @return
     */
	@GetMapping("/list")
	public R<List<ShoppingCart>> list(){
		
		LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
		queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
		queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
		
		List<ShoppingCart> list=shoppingCartService.list(queryWrapper);
		
		return R.success(list);
	}
	
	   /**
     * 清空购物车
     * @return
     */
	@DeleteMapping("/clean")
	public R<String> delete(){
		
		 //SQL:delete from shopping_cart where user_id = ?
		LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<ShoppingCart>();
		queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
		
		shoppingCartService.remove(queryWrapper);
		return R.success("清空购物车成功");
	}
}
