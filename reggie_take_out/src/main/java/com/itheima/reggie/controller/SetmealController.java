package com.itheima.reggie.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealService;

import lombok.extern.slf4j.Slf4j;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

	@Autowired
	private SetmealService setmealService;
	@Autowired
	private CategoryService categoryService;
	
	@PostMapping
	public R<String> save(@RequestBody SetmealDto setMealDto){
		
		log.info("套餐信息,{}",setMealDto);
		setmealService.saveWithDish(setMealDto);
		
		return R.success("新增套餐成功");
	}
	
	
	@GetMapping("/page")
	public R<Page> page(int page, int pageSize, String name){
		
		//构造分页构造器
		Page<Setmeal> pageInfo=new Page<>(page,pageSize);
		Page<SetmealDto> setmealDtoPage=new Page<>();
		//条件构造器
		LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
		//添加过滤条件
		queryWrapper.like(name!=null, Setmeal::getName,name);
		//添加排序条件
		queryWrapper.orderByDesc(Setmeal::getUpdateTime);
		//进行分页查询
		setmealService.page(pageInfo,queryWrapper);
		
		BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
		
		List<Setmeal> records=pageInfo.getRecords();
		List<SetmealDto> list=new ArrayList<>();
		
		for(Setmeal s: records) {
			SetmealDto setmealDto=new SetmealDto();
			BeanUtils.copyProperties(s, setmealDto);
			
			Long categoryId=s.getCategoryId();
			//根据id查询分类对象
			Category category=categoryService.getById(categoryId);
			
			if(category!=null) {
				setmealDto.setCategoryName(category.getName());
			}
			list.add(setmealDto);
		}
		
		setmealDtoPage.setRecords(list);
		return R.success(setmealDtoPage);
	}
	
	@DeleteMapping
	public R<String> delete(@RequestParam List<Long> ids) {
		log.info("ids:{}",ids);
//		setmealService.removeById(ids);
		setmealService.removeWithDish(ids);
		
		return R.success("删除套餐成功");
	}
	

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
	@GetMapping("/list")
	public R<List<Setmeal>> list(Setmeal setmeal){
		
		LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<Setmeal>();
		queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId, setmeal.getCategoryId());
		queryWrapper.eq(setmeal.getStatus()!=null, Setmeal::getStatus,setmeal.getStatus());
		queryWrapper.orderByAsc(Setmeal::getUpdateTime);
		List<Setmeal> setmealList=setmealService.list(queryWrapper);
		return R.success(setmealList);
	}
}
