package com.xawl.cateen.service.mini;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.vo.FoodVO;
import com.xawl.cateen.vo.mini.LuckyDrawResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 幸运抽奖服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LuckyDrawService {

    private final FoodMapper foodMapper;
    private final Random random = new Random();

    /**
     * 获取转盘美食列表
     * 随机返回6-8个评分较高的美食
     */
    public List<FoodVO> getRandomFoods() {
        // 随机数量：6-8个
        int count = 6 + random.nextInt(3);
        
        // 查询评分较高的美食（评分>=4.0，状态为active）
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Food::getStatus, "active")
                .ge(Food::getRating, 4.0)
                .orderByDesc(Food::getRating)
                .last("LIMIT 20"); // 先取前20个高分美食
        
        List<Food> foods = foodMapper.selectList(wrapper);
        
        if (foods.isEmpty()) {
            // 如果没有高分美食，随机取active状态的美食
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Food::getStatus, "active")
                    .last("LIMIT 20");
            foods = foodMapper.selectList(wrapper);
        }
        
        // 打乱顺序并取前count个
        Collections.shuffle(foods);
        List<Food> selectedFoods = foods.stream()
                .limit(count)
                .collect(Collectors.toList());
        
        // 转换为VO
        return selectedFoods.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 执行抽奖
     */
    public LuckyDrawResultVO draw() {
        // 1. 获取转盘美食列表
        List<FoodVO> foods = getRandomFoods();
        
        if (foods.isEmpty()) {
            throw new RuntimeException("暂无可抽奖的美食");
        }
        
        // 2. 随机抽取一个（可以添加权重算法）
        int index = random.nextInt(foods.size());
        FoodVO selectedFood = foods.get(index);
        
        // 3. 计算转盘角度（转3-5圈后停在目标位置）
        int baseRotation = (3 + random.nextInt(3)) * 360; // 转3-5圈
        int singleAngle = 360 / foods.size(); // 每个扇形的角度
        int targetAngle = singleAngle * index; // 目标位置的角度
        int finalAngle = baseRotation + targetAngle; // 最终旋转角度
        
        log.info("幸运抽奖结果 - 美食: {}, 索引: {}, 角度: {}", 
                selectedFood.getName(), index, finalAngle);
        
        // 4. 返回结果
        return LuckyDrawResultVO.builder()
                .food(selectedFood)
                .angle(finalAngle)
                .index(index)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 转换为VO
     */
    private FoodVO convertToVO(Food food) {
        FoodVO vo = new FoodVO();
        BeanUtils.copyProperties(food, vo);
        return vo;
    }
}
