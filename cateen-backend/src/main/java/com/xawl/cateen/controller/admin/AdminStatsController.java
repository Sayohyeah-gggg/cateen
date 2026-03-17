package com.xawl.cateen.controller.admin;

import com.xawl.cateen.common.Result;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.ForumCommentMapper;
import com.xawl.cateen.mapper.ForumPostMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.mapper.RankingMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端统计数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@Api(tags = "10. 统计数据")
public class AdminStatsController {

    private final FoodMapper foodMapper;
    private final ProfileMapper profileMapper;
    private final CommentMapper commentMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumPostMapper forumPostMapper;
    private final RankingMapper rankingMapper;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    @ApiOperation(value = "获取仪表盘统计数据", notes = "获取美食、用户、评论等统计数据")
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 统计美食总数
            Long totalFoods = foodMapper.selectCount(null);
            stats.put("total_foods", totalFoods);
            
            // 统计用户总数
            Long totalUsers = profileMapper.selectCount(null);
            stats.put("total_users", totalUsers);
            
            // 统计美食评论总数
            Long totalFoodComments = commentMapper.selectCount(null);
            stats.put("total_food_comments", totalFoodComments);

            // 统计帖子评论总数（以帖子comment_count求和为准，避免统计异常）
            Long totalForumComments = forumPostMapper.sumCommentCount();
            stats.put("total_forum_comments", totalForumComments != null ? totalForumComments : 0L);

            // 评论总数（兼容旧字段）
            stats.put("total_comments", totalFoodComments + (totalForumComments != null ? totalForumComments : 0L));
            
            // 统计榜单总数
            Long totalRankings = rankingMapper.selectCount(null);
            stats.put("total_rankings", totalRankings);
            
            // 统计待审核评论数
            Long pendingComments = commentMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xawl.cateen.entity.Comment>()
                    .eq(com.xawl.cateen.entity.Comment::getStatus, "pending")
            );
            stats.put("pending_comments", pendingComments);
            
            // 计算平均评分
            Double avgRating = foodMapper.selectAvgRating();
            stats.put("avg_rating", avgRating != null ? avgRating : 0.0);
            
            log.info("获取仪表盘统计数据成功");
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败：{}", e.getMessage(), e);
            return Result.error("获取统计数据失败");
        }
    }
}
