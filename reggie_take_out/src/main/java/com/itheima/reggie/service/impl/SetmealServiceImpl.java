package com.itheima.reggie.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;

/**
 * 新增套餐，同时需要保存套餐和菜品的关联关系
 * @param setmealDto
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService{
	
	@Autowired
	private SetmealDishService setmealDishService;
	
	@Transactional
	public void saveWithDish(SetmealDto setMealDto) {
		 //保存套餐的基本信息，操作setmeal，执行insert操作
		this.save(setMealDto);
		
		List<SetmealDish> dishes=setMealDto.getSetmealDishes();
		
		for(SetmealDish d: dishes) {
			d.setSetmealId(setMealDto.getId());
		}
		//保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
		setmealDishService.saveBatch(dishes);
		
	}
    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
	@Transactional
	public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询套餐状态，确定是否可用删除
		LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
		queryWrapper.in(Setmeal::getId, ids);
		queryWrapper.eq(Setmeal::getStatus, 1);
			
		int count=this.count(queryWrapper);
		if(count>0) {
			//如果不能删除，抛出一个业务异常
			throw new CustomException("套餐正在售卖中，不能删除");
		}
		
		//如果可以删除，先删除套餐表中的数据---setmeal
		this.removeByIds(ids);
		//delete from setmeal_dish where setmeal_id in (1,2,3)
		LambdaQueryWrapper<SetmealDish> queryWrapper2=new LambdaQueryWrapper<SetmealDish>();
		queryWrapper2.in(SetmealDish::getSetmealId, ids);
		//删除关系表中的数据----setmeal_dish
		setmealDishService.remove(queryWrapper2);
		
	}

}
