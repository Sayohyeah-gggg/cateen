package com.xawl.cateen.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.ResultCode;
import com.xawl.cateen.constant.StatusConstants;
import com.xawl.cateen.dto.RankingDTO;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.FoodCategory;
import com.xawl.cateen.entity.Ranking;
import com.xawl.cateen.entity.RankingFood;
import com.xawl.cateen.exception.BusinessException;
import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodCategoryMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.RankingFoodMapper;
import com.xawl.cateen.mapper.RankingMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.xawl.cateen.service.RankingService;
import com.xawl.cateen.util.UserContext;
import com.xawl.cateen.vo.RankingVO;
import com.xawl.cateen.vo.mini.MiniRankingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 榜单服务实现类
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;
    private final RankingFoodMapper rankingFoodMapper;
    private final FoodMapper foodMapper;
    private final FoodCategoryMapper foodCategoryMapper;
    private final CommentMapper commentMapper;

    @Override
    public Page<RankingVO> getRankingPage(Long pageNum, Long pageSize, String keyword, String type, String status) {
        Page<RankingVO> page = new Page<>(pageNum, pageSize);
        rankingMapper.selectRankingPage(page, keyword, type, status);
        return page;
    }

    @Override
    public RankingVO getRankingDetail(String id) {
        RankingVO rankingVO = rankingMapper.selectRankingDetailById(id);
        if (rankingVO == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }
        return rankingVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RankingVO createRanking(RankingDTO dto) {
        // 创建榜单
        Ranking ranking = new Ranking();
        ranking.setId(IdUtil.getSnowflakeNextIdStr());
        ranking.setTitle(dto.getTitle());
        ranking.setDescription(dto.getDescription());
        ranking.setType(StrUtil.isNotBlank(dto.getType()) ? dto.getType() : "general");
        ranking.setStatus(StatusConstants.ACTIVE);
        ranking.setCreatedBy(UserContext.getUserId());

        rankingMapper.insert(ranking);

        // 创建榜单美食关联
        if (CollUtil.isNotEmpty(dto.getFoodIds())) {
            List<RankingFood> relations = IntStream.range(0, dto.getFoodIds().size())
                    .mapToObj(index -> {
                        RankingFood relation = new RankingFood();
                        relation.setId(IdUtil.getSnowflakeNextIdStr());
                        relation.setRankingId(ranking.getId());
                        relation.setFoodId(dto.getFoodIds().get(index));
                        relation.setRankPosition(index + 1);
                        relation.setCreatedAt(LocalDateTime.now());
                        return relation;
                    })
                    .collect(Collectors.toList());
            rankingFoodMapper.batchInsert(relations);
        }

        return getRankingDetail(ranking.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRanking(String id, RankingDTO dto) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        // 更新榜单信息
        ranking.setTitle(dto.getTitle());
        ranking.setDescription(dto.getDescription());
        if (StrUtil.isNotBlank(dto.getType())) {
            ranking.setType(dto.getType());
        }

        rankingMapper.updateById(ranking);

        // 更新榜单美食关联
        if (dto.getFoodIds() != null) {
            // 删除旧的关联
            rankingFoodMapper.deleteByRankingId(id);

            // 创建新的关联
            if (CollUtil.isNotEmpty(dto.getFoodIds())) {
                List<RankingFood> relations = IntStream.range(0, dto.getFoodIds().size())
                        .mapToObj(index -> {
                            RankingFood relation = new RankingFood();
                            relation.setId(IdUtil.getSnowflakeNextIdStr());
                            relation.setRankingId(id);
                            relation.setFoodId(dto.getFoodIds().get(index));
                            relation.setRankPosition(index + 1);
                            relation.setCreatedAt(LocalDateTime.now());
                            return relation;
                        })
                        .collect(Collectors.toList());
                rankingFoodMapper.batchInsert(relations);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRanking(String id) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        rankingMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRankingStatus(String id, String status) {
        Ranking ranking = rankingMapper.selectById(id);
        if (ranking == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "榜单不存在");
        }

        ranking.setStatus(status);
        rankingMapper.updateById(ranking);
    }

    @Override
    public List<MiniRankingVO> getMiniRanking(String type, String category, String timeRange, Integer limit) {
        // 简化实现：直接从美食表查询，按照type和category筛选
        LambdaQueryWrapper<Food> wrapper = new LambdaQueryWrapper<>();
        
        // 分类筛选 - 修复undefined值处理问题
        if (StrUtil.isNotBlank(category) && !"undefined".equals(category)) {
            wrapper.eq(Food::getCategoryId, category);
        }
        
        // 先获取所有美食，然后按实时评分排序
        List<Food> foods = foodMapper.selectList(wrapper);
        
        // 转换为MiniRankingVO并计算实时评分
        List<MiniRankingVO> rankings = new ArrayList<>();
        for (Food food : foods) {
            // 获取分类名称
            String categoryName = "";
            if (StrUtil.isNotBlank(food.getCategoryId())) {
                FoodCategory foodCategory = foodCategoryMapper.selectById(food.getCategoryId());
                if (foodCategory != null) {
                    categoryName = foodCategory.getName();
                }
            }
            
            // 计算实时评分
            BigDecimal realRating = calculateRealRating(food.getId());
            Integer realRatingCount = calculateRealRatingCount(food.getId());
            
            rankings.add(MiniRankingVO.builder()
                    .rank(0) // 先设置为0，后面重新排序
                    .food_id(food.getId())
                    .food_name(food.getName())
                    .food_image(food.getImageUrl())
                    .category_name(categoryName)
                    .rating(realRating)
                    .rating_count(realRatingCount)
                    .trend("same") // 简化实现，固定返回same
                    .build());
        }
        
        // 按评分倒序排序
        if ("rating".equals(type)) {
            rankings.sort((a, b) -> b.getRating().compareTo(a.getRating()));
        } else if ("popular".equals(type)) {
            rankings.sort((a, b) -> b.getRating_count().compareTo(a.getRating_count()));
        } else if ("new".equals(type)) {
            // 按创建时间排序需要重新查询
            rankings.sort((a, b) -> {
                Food foodA = foods.stream().filter(f -> f.getId().equals(a.getFood_id())).findFirst().orElse(null);
                Food foodB = foods.stream().filter(f -> f.getId().equals(b.getFood_id())).findFirst().orElse(null);
                if (foodA != null && foodB != null) {
                    return foodB.getCreatedAt().compareTo(foodA.getCreatedAt());
                }
                return 0;
            });
        } else {
            // 默认按评分排序
            rankings.sort((a, b) -> b.getRating().compareTo(a.getRating()));
        }
        
        // 限制数量并重新设置排名
        if (rankings.size() > limit) {
            rankings = rankings.subList(0, limit);
        }
        
        // 重新设置排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }
        
        return rankings;
    }

    /**
     * 实时计算评分
     */
    private BigDecimal calculateRealRating(String foodId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFoodId, foodId)
                .isNotNull(Comment::getRating)
                .gt(Comment::getRating, 0);
        
        List<Comment> comments = commentMapper.selectList(wrapper);
        
        if (comments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double totalRating = comments.stream()
                .mapToDouble(Comment::getRating)
                .sum();
        
        return BigDecimal.valueOf(totalRating / comments.size()).setScale(1, RoundingMode.HALF_UP);
    }
    
    /**
     * 实时计算评分人数
     */
    private Integer calculateRealRatingCount(String foodId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getFoodId, foodId)
                .isNotNull(Comment::getRating)
                .gt(Comment::getRating, 0);
        
        return commentMapper.selectCount(wrapper).intValue();
    }

}

