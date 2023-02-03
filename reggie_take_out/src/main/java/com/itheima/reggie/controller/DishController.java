package com.itheima.reggie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
	
	@Autowired
	private DishService dishService;
	@Autowired
	private DishFlavorService dishFlavorService;
	@Autowired
	private CategoryService categoryService;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 新增菜品
	 * @param dishDto
	 * @return
	 */
	@PostMapping
	public R<String> save(@RequestBody DishDto dishDto){
		
		log.info(dishDto.toString());
		
		dishService.saveWithFlavor(dishDto);
		//清理所有菜品的缓存数据
		//Set keys = redisTemplate.keys("dish_*");
		//redisTemplate.delete(keys);

		//清理某个分类下面的菜品缓存数据
		String key="dish_"+dishDto.getCategoryId()+"_1";
		redisTemplate.delete(key);

		return R.success("新增菜品成功");
		
	}
	
	
	
	/**
	 * 菜品信息分页查询
	 * @param page
	 * @param pageSize
	 * @param name
	 * @return
	 */
	@GetMapping("/page")
	public R<Page> page(int page, int pageSize, String name){
		//构造分页构造器
		Page<Dish> pageInfo=new Page<>(page,pageSize);
		Page<DishDto> dishDtoPage=new Page<>(page,pageSize);
		
		//条件构造器
		LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
		//添加过滤条件
		queryWrapper.like(name!=null, Dish::getName, name);
		//添加排序条件
		queryWrapper.orderByDesc(Dish::getUpdateTime);
		//进行分页查询
		dishService.page(pageInfo, queryWrapper);
		
		//对象拷贝
		BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
		
		List<Dish> records=pageInfo.getRecords();
		List<DishDto> list=new ArrayList<>();
		for(Dish d:records) {
			DishDto dishDto=new DishDto();
			
			
			BeanUtils.copyProperties(d,dishDto);
			Long categoryId=d.getCategoryId();//分类id
			//根据id查询分类对象
			Category category=categoryService.getById(categoryId);
			if(category!=null) {
				dishDto.setCategoryName(category.getName());
			}
			list.add(dishDto);
		}
//        List<DishDto> list = records.stream().map((item) -> {
//            DishDto dishDto = new DishDto();
//
//            BeanUtils.copyProperties(item,dishDto);
//
//            Long categoryId = item.getCategoryId();//分类id
//            //根据id查询分类对象
//            Category category = categoryService.getById(categoryId);
//
//            if(category != null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);
//            }
//            return dishDto;
//        }).collect(Collectors.toList());
	
		dishDtoPage.setRecords(list);
		return R.success(dishDtoPage);
		
	}
	
	/**
	 * 根据Id查询菜品信息和对应的口味信息
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public R<DishDto> get(@PathVariable Long id){
		
		DishDto dishDto=dishService.getByIdWithFlavor(id);
		
		return R.success(dishDto);
	}
	
	/**
	 * 修改菜品信息
	 * @param dishDto
	 * @return
	 */
	@PutMapping
	public R<String> update(@RequestBody DishDto dishDto){

		dishService.updateWithFlavor(dishDto);

		//清理所有菜品的缓存数据
		//Set keys = redisTemplate.keys("dish_*");
		//redisTemplate.delete(keys);

		//清理某个分类下面的菜品缓存数据
		String key="dish_"+dishDto.getCategoryId()+"_1";
		redisTemplate.delete(key);
		return R.success("更新菜品成功");
		
	}
	
    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//	@GetMapping("/list")
//	public R<List<Dish>> list(Dish dish){
//		//构造查询条件
//		LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<Dish>();
//		queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId, dish.getCategoryId());
//		//添加条件，查询状态为1（起售状态）的菜品
//		queryWrapper.eq(Dish::getStatus, 1);
//		 //添加排序条件
//		queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//		
//		List<Dish> dishes=dishService.list(queryWrapper);
//		return R.success(dishes);
//	}
	
	@GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
		List<DishDto> dishDtoList=null;
		//动态获取key
		String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();

		//先从redis中获取缓存数据
		 dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

		if (dishDtoList!=null){
			//如果存在，直接返回，无需查询数据库
			return R.success(dishDtoList);
		}

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

		//如果不存在，需要查询数据库，将查询到的菜品数据缓存到redis
		redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
