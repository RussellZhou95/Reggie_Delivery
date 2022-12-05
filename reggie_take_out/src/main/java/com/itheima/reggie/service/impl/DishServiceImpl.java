package com.itheima.reggie.service.impl;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService{

	@Autowired
	private DishFlavorService dishFlavorService;
	
	
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
	@Transactional
	public void saveWithFlavor(DishDto dishDto) {
		//保存菜品的基本信息到菜品表dish
		this.save(dishDto);
		
		Long dishId=dishDto.getId();//菜品id
		//菜品口味
		List<DishFlavor> flavors=dishDto.getFlavors();
		
		for(DishFlavor d: flavors) {
			d.setDishId(dishId);;
		}
		 //保存菜品口味数据到菜品口味表dish_flavor
		dishFlavorService.saveBatch(flavors);
	}


	 /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     *//**
	 * 
	 */
	public DishDto getByIdWithFlavor(Long id) {
		
		Dish dish=this.getById(id);
		
		DishDto dishDto=new DishDto();
		
		BeanUtils.copyProperties(dish, dishDto);

		LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<DishFlavor>();
		queryWrapper.eq(DishFlavor::getDishId, dish.getId());
		
		List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);
		dishDto.setFlavors(flavors);
		return dishDto;
	}


	
	public void updateWithFlavor(DishDto dishDto) {
		//更新dish表基本信息
		this.updateById(dishDto);
		
		 //清理当前菜品对应口味数据---dish_flavor表的delete操作
		LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<DishFlavor>();
		queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
		
		dishFlavorService.remove(queryWrapper);
		//添加当前提交过来的口味数据---dish_flavor表的insert操作
		List<DishFlavor> flavors=dishDto.getFlavors();
		
		for(DishFlavor df: flavors) {
			df.setDishId(dishDto.getId());
		}
		dishFlavorService.saveBatch(flavors);
	}

}
