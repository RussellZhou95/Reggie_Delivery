package com.itheima.reggie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

	
	@Autowired
	private CategoryService categoryService;
	
	/**
	 * 新增分类
	 * @param category
	 * @return
	 */
	@PostMapping
	public R<String> save(@RequestBody Category category){
		log.info("category:{}",category);
		categoryService.save(category);
		return R.success("新增分类成功");
	}
	
	/**
	 * 分页查询
	 * @param page
	 * @param pageSize
	 * @return
	 */
	@GetMapping("/page")
	public R<Page> page(int page, int pageSize){
		//构造分页构造器
		Page<Category> pageInfo=new Page<>(page,pageSize);
		//条件构造器
		LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper();
		//添加排序条件，根据sort进行排序
		queryWrapper.orderByAsc(Category::getSort);
		//进行分页查询
		categoryService.page(pageInfo, queryWrapper);
		
		return R.success(pageInfo);
	}
	
	/**
	 * 根据Id删除分类
	 * @param id
	 * @return
	 */
	@DeleteMapping
	public R<String> delete(Long ids){
		log.info("删除分类,id为: {}",ids);
		
//		categoryService.removeById(ids);
		categoryService.remove(ids);
		return R.success("删除成功");
	}
	
	/**
	 * 根据Id修改分类信息
	 * @param category
	 * @return
	 */
	@PutMapping
	public R<String> update(@RequestBody Category category){
		log.info("修改信息分类:{}",category);
		categoryService.updateById(category);
		
		return R.success("修改分类信息成功");
		
	}
	
	/**
	 * 
	 * @param category
	 * @return
	 */
	@GetMapping("/list")
	public R<List<Category>> list(Category category){
		LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<Category>();
		
		queryWrapper.eq(category.getType()!=null, Category::getType, category.getType());
		
		queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
		
		List<Category> categoryList=categoryService.list(queryWrapper);
		return R.success(categoryList);
		
	}
}
