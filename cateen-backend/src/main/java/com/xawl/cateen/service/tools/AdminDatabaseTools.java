package com.xawl.cateen.service.tools;

import com.xawl.cateen.entity.Comment;
import com.xawl.cateen.entity.Food;
import com.xawl.cateen.entity.ForumComment;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.CommentMapper;
import com.xawl.cateen.mapper.FoodMapper;
import com.xawl.cateen.mapper.ForumCommentMapper;
import com.xawl.cateen.mapper.ForumPostMapper;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.service.CommentService;
import com.xawl.cateen.service.ExcelGeneratorService;
import com.xawl.cateen.service.ForumService;
import com.xawl.cateen.service.PptGeneratorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 管理端数据库工具类
 * 为管理员AI助手提供数据查询能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDatabaseTools {

    private final FoodMapper foodMapper;
    private final ProfileMapper profileMapper;
    private final CommentMapper commentMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final CommentService commentService;
    private final ForumService forumService;
    private final ExcelGeneratorService excelGeneratorService;
    private final PptGeneratorService pptGeneratorService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool("查询用户总数和最近注册的用户信息")
    public String getUserStatistics() {
        try {
            log.info("AI调用工具：查询用户统计");
            Long totalUsers = profileMapper.selectCount(null);
            List<Profile> recentUsers = profileMapper.selectList(
                new LambdaQueryWrapper<Profile>().orderByDesc(Profile::getCreatedAt).last("LIMIT 5")
            );
            StringBuilder result = new StringBuilder();
            result.append(String.format("用户统计：\n总用户数：%d\n\n", totalUsers));
            result.append("最近注册的5位用户：\n");
            for (Profile user : recentUsers) {
                result.append(String.format("- %s（ID: %s）\n  注册时间：%s\n",
                    user.getNickname(), user.getId(),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询用户统计失败", e);
            return "查询用户统计失败：" + e.getMessage();
        }
    }

    @Tool("根据用户ID查询用户详细信息")
    public String getUserDetail(String userId) {
        try {
            log.info("AI调用工具：查询用户详情，ID={}", userId);
            Profile user = profileMapper.selectById(userId);
            if (user == null) {
                return "未找到ID为 " + userId + " 的用户";
            }
            return String.format("用户详情：\n昵称：%s\n用户ID：%s\n微信OpenID：%s\n注册时间：%s\n最后更新：%s",
                user.getNickname(), user.getId(), user.getWechatOpenid(),
                user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "未知",
                user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : "未知");
        } catch (Exception e) {
            log.error("查询用户详情失败", e);
            return "查询用户详情失败：" + e.getMessage();
        }
    }

    @Tool("查询评论统计信息，包括总评论数和最近的评论")
    public String getCommentStatistics() {
        try {
            log.info("AI调用工具：查询评论统计");
            Long totalComments = commentMapper.selectCount(null);
            List<Comment> recentComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreatedAt).last("LIMIT 5")
            );
            StringBuilder result = new StringBuilder();
            result.append(String.format("评论统计：\n总评论数：%d\n\n", totalComments));
            result.append("最近的5条评论：\n");
            for (Comment comment : recentComments) {
                result.append(String.format("- 评论ID: %s\n  用户ID: %s\n  美食ID: %s\n  评分：%.1f\n  内容：%s\n  时间：%s\n\n",
                    comment.getId(), comment.getUserId(), comment.getFoodId(),
                    comment.getRating() != null ? comment.getRating().doubleValue() : 0.0,
                    comment.getContent() != null && comment.getContent().length() > 50
                        ? comment.getContent().substring(0, 50) + "..." : comment.getContent(),
                    comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询评论统计失败", e);
            return "查询评论统计失败：" + e.getMessage();
        }
    }

    @Tool("根据美食ID查询该美食的所有评论")
    public String getCommentsByFood(String foodId) {
        try {
            log.info("AI调用工具：查询美食评论，美食ID={}", foodId);
            List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getFoodId, foodId)
                    .orderByDesc(Comment::getCreatedAt).last("LIMIT 10")
            );
            if (comments.isEmpty()) {
                return "该美食暂无评论";
            }
            StringBuilder result = new StringBuilder(String.format("美食ID %s 的评论（共%d条）：\n\n", foodId, comments.size()));
            for (Comment comment : comments) {
                result.append(String.format("- 评论ID: %s\n  评分：%.1f\n  内容：%s\n  时间：%s\n\n",
                    comment.getId(),
                    comment.getRating() != null ? comment.getRating().doubleValue() : 0.0,
                    comment.getContent(),
                    comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询美食评论失败", e);
            return "查询美食评论失败：" + e.getMessage();
        }
    }

    @Tool("查询美食统计信息，包括总数、平均评分等")
    public String getFoodStatistics() {
        try {
            log.info("AI调用工具：查询美食统计");
            Long totalFoods = foodMapper.selectCount(
                new LambdaQueryWrapper<Food>().eq(Food::getStatus, "active")
            );
            List<Food> topRatedFoods = foodMapper.selectList(
                new LambdaQueryWrapper<Food>().eq(Food::getStatus, "active")
                    .orderByDesc(Food::getRating).last("LIMIT 5")
            );
            StringBuilder result = new StringBuilder();
            result.append(String.format("美食统计：\n在售美食总数：%d\n\n", totalFoods));
            result.append("评分最高的5个美食：\n");
            for (Food food : topRatedFoods) {
                result.append(String.format("- %s：评分 %.1f（%d人评价），价格 %.2f元\n",
                    food.getName(),
                    food.getRating() != null ? food.getRating().doubleValue() : 0.0,
                    food.getRatingCount() != null ? food.getRatingCount() : 0,
                    food.getPrice() != null ? food.getPrice().doubleValue() : 0.0));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询美食统计失败", e);
            return "查询美食统计失败：" + e.getMessage();
        }
    }

    @Tool("搜索用户，支持按昵称模糊搜索")
    public String searchUsers(String keyword) {
        try {
            log.info("AI调用工具：搜索用户，关键词={}", keyword);
            List<Profile> users = profileMapper.selectList(
                new LambdaQueryWrapper<Profile>().like(Profile::getNickname, keyword).last("LIMIT 10")
            );
            if (users.isEmpty()) {
                return "未找到包含关键词 \"" + keyword + "\" 的用户";
            }
            StringBuilder result = new StringBuilder(String.format("搜索到 %d 个用户：\n\n", users.size()));
            for (Profile user : users) {
                result.append(String.format("- %s（ID: %s）\n  注册时间：%s\n",
                    user.getNickname(), user.getId(),
                    user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            return "搜索用户失败：" + e.getMessage();
        }
    }

    @Tool("查询指定时间段内的数据活跃情况")
    public String getActivityStatistics(int days) {
        try {
            log.info("AI调用工具：查询活跃统计，天数={}", days);
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            Long newUsers = profileMapper.selectCount(
                new LambdaQueryWrapper<Profile>().ge(Profile::getCreatedAt, startTime)
            );
            Long newComments = commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>().ge(Comment::getCreatedAt, startTime)
            );
            return String.format("最近%d天的活跃统计：\n新增用户：%d 人\n新增评论：%d 条\n平均每天新增用户：%.1f 人\n平均每天新增评论：%.1f 条",
                days, newUsers, newComments, newUsers.doubleValue() / days, newComments.doubleValue() / days);
        } catch (Exception e) {
            log.error("查询活跃统计失败", e);
            return "查询活跃统计失败：" + e.getMessage();
        }
    }

    @Tool("查询食友分享帖子的统计信息，包括总帖子数、各状态数量和最近发布的帖子")
    public String getForumPostStatistics() {
        try {
            log.info("AI调用工具：查询帖子统计");
            Long totalPosts = forumPostMapper.selectCount(null);
            Long approvedPosts = forumPostMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>().eq(ForumPost::getStatus, "approved")
            );
            Long rejectedPosts = forumPostMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>().eq(ForumPost::getStatus, "rejected")
            );
            List<ForumPost> recentPosts = forumPostMapper.selectList(
                new LambdaQueryWrapper<ForumPost>().orderByDesc(ForumPost::getCreatedAt).last("LIMIT 5")
            );
            StringBuilder result = new StringBuilder();
            result.append(String.format("帖子统计：\n总帖子数：%d\n正常：%d\n已屏蔽：%d\n\n", totalPosts, approvedPosts, rejectedPosts));
            result.append("最近发布的5条帖子：\n");
            for (ForumPost post : recentPosts) {
                String preview = post.getContent() != null && post.getContent().length() > 40
                    ? post.getContent().substring(0, 40) + "..." : post.getContent();
                result.append(String.format("- ID: %s\n  内容：%s\n  点赞：%d  评论：%d  状态：%s\n  时间：%s\n",
                    post.getId(), preview,
                    post.getLikeCount() != null ? post.getLikeCount() : 0,
                    post.getCommentCount() != null ? post.getCommentCount() : 0,
                    post.getStatus(),
                    post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询帖子统计失败", e);
            return "查询帖子统计失败：" + e.getMessage();
        }
    }

    @Tool("根据关键词搜索食友分享帖子内容")
    public String searchForumPosts(String keyword) {
        try {
            log.info("AI调用工具：搜索帖子，关键词={}", keyword);
            List<ForumPost> posts = forumPostMapper.selectList(
                new LambdaQueryWrapper<ForumPost>()
                    .like(ForumPost::getContent, keyword)
                    .orderByDesc(ForumPost::getCreatedAt)
                    .last("LIMIT 10")
            );
            if (posts.isEmpty()) {
                return "未找到包含关键词 \"" + keyword + "\" 的帖子";
            }
            StringBuilder result = new StringBuilder(String.format("搜索到 %d 条帖子：\n\n", posts.size()));
            for (ForumPost post : posts) {
                String preview = post.getContent() != null && post.getContent().length() > 60
                    ? post.getContent().substring(0, 60) + "..." : post.getContent();
                result.append(String.format("- ID: %s\n  内容：%s\n  点赞：%d  评论：%d  状态：%s\n  时间：%s\n",
                    post.getId(), preview,
                    post.getLikeCount() != null ? post.getLikeCount() : 0,
                    post.getCommentCount() != null ? post.getCommentCount() : 0,
                    post.getStatus(),
                    post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("搜索帖子失败", e);
            return "搜索帖子失败：" + e.getMessage();
        }
    }

    @Tool("根据帖子ID查询该帖子的评论列表")
    public String getForumCommentsByPost(String postId) {
        try {
            log.info("AI调用工具：查询帖子评论，帖子ID={}", postId);
            ForumPost post = forumPostMapper.selectById(postId);
            if (post == null) {
                return "未找到ID为 " + postId + " 的帖子";
            }
            List<ForumComment> comments = forumCommentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                    .eq(ForumComment::getPostId, postId)
                    .orderByDesc(ForumComment::getCreatedAt)
                    .last("LIMIT 10")
            );
            if (comments.isEmpty()) {
                return "帖子 " + postId + " 暂无评论";
            }
            StringBuilder result = new StringBuilder(String.format("帖子 %s 的评论（共%d条）：\n\n", postId, comments.size()));
            for (ForumComment comment : comments) {
                result.append(String.format("- 评论ID: %s\n  用户ID: %s\n  内容：%s\n  点赞：%d\n  时间：%s\n",
                    comment.getId(), comment.getUserId(), comment.getContent(),
                    comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                    comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : "未知"));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("查询帖子评论失败", e);
            return "查询帖子评论失败：" + e.getMessage();
        }
    }

    @Tool("删除美食评论，参数为评论ID")
    public String deleteFoodComment(String commentId) {
        try {
            log.info("AI调用工具：删除美食评论，评论ID={}", commentId);
            commentService.deleteComment(commentId);
            return "已删除美食评论：" + commentId;
        } catch (Exception e) {
            log.error("删除美食评论失败", e);
            return "删除美食评论失败：" + e.getMessage();
        }
    }

    @Tool("删除帖子评论，参数为评论ID")
    public String deleteForumComment(String commentId) {
        try {
            log.info("AI调用工具：删除帖子评论，评论ID={}", commentId);
            forumService.adminDeleteComment(commentId);
            return "已删除帖子评论：" + commentId;
        } catch (Exception e) {
            log.error("删除帖子评论失败", e);
            return "删除帖子评论失败：" + e.getMessage();
        }
    }

    @Tool("根据帖子ID读取帖子详细内容")
    public String getForumPostDetail(String postId) {
        try {
            log.info("AI调用工具：读取帖子详情，帖子ID={}", postId);
            ForumPost post = forumPostMapper.selectById(postId);
            if (post == null || post.getDeleted() != null && post.getDeleted() == 1) {
                return "未找到ID为 " + postId + " 的帖子";
            }
            Profile user = profileMapper.selectById(post.getUserId());
            String nickname = user != null && user.getNickname() != null ? user.getNickname() : "未知";
            String content = post.getContent() != null ? post.getContent() : "";
            if (content.length() > 1000) {
                content = content.substring(0, 1000) + "...(内容过长，已截断)";
            }
            String images = post.getImages() != null ? post.getImages() : "无";
            return String.format("帖子详情：\nID：%s\n作者：%s\n状态：%s\n点赞：%d  评论：%d\n发布时间：%s\n图片：%s\n内容：\n%s",
                    post.getId(),
                    nickname,
                    post.getStatus(),
                    post.getLikeCount() != null ? post.getLikeCount() : 0,
                    post.getCommentCount() != null ? post.getCommentCount() : 0,
                    post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : "未知",
                    images,
                    content);
        } catch (Exception e) {
            log.error("读取帖子详情失败", e);
            return "读取帖子详情失败：" + e.getMessage();
        }
    }

    @Tool("读取最新发布的帖子列表，limit为返回数量，建议1-20")
    public String getLatestForumPosts(int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 20));
            log.info("AI调用工具：读取最新帖子列表，limit={}", safeLimit);
            List<ForumPost> posts = forumPostMapper.selectList(
                new LambdaQueryWrapper<ForumPost>()
                    .orderByDesc(ForumPost::getCreatedAt)
                    .last("LIMIT " + safeLimit)
            );
            if (posts.isEmpty()) {
                return "暂无帖子";
            }
            StringBuilder result = new StringBuilder(String.format("最新%d条帖子：\n\n", posts.size()));
            for (ForumPost post : posts) {
                Profile user = profileMapper.selectById(post.getUserId());
                String nickname = user != null && user.getNickname() != null ? user.getNickname() : "未知";
                String preview = post.getContent() != null && post.getContent().length() > 80
                    ? post.getContent().substring(0, 80) + "..." : post.getContent();
                result.append(String.format("- ID: %s\n  作者：%s\n  状态：%s\n  点赞：%d  评论：%d\n  时间：%s\n  内容：%s\n\n",
                    post.getId(),
                    nickname,
                    post.getStatus(),
                    post.getLikeCount() != null ? post.getLikeCount() : 0,
                    post.getCommentCount() != null ? post.getCommentCount() : 0,
                    post.getCreatedAt() != null ? post.getCreatedAt().format(formatter) : "未知",
                    preview));
            }
            return result.toString();
        } catch (Exception e) {
            log.error("读取最新帖子列表失败", e);
            return "读取最新帖子列表失败：" + e.getMessage();
        }
    }

    @Tool("获取最新评论（包含美食评论和帖子评论），limit为返回数量，建议1-20")
    public String getRecentAllComments(int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 20));
            log.info("AI调用工具：获取最新评论（美食+帖子），limit={}", safeLimit);

            List<Comment> foodComments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                    .orderByDesc(Comment::getCreatedAt)
                    .last("LIMIT " + safeLimit)
            );

            List<ForumComment> forumComments = forumCommentMapper.selectList(
                new LambdaQueryWrapper<ForumComment>()
                    .orderByDesc(ForumComment::getCreatedAt)
                    .last("LIMIT " + safeLimit)
            );

            StringBuilder result = new StringBuilder();
            result.append(String.format("最新评论汇总（每类最多%d条）：\n\n", safeLimit));

            result.append("美食评论：\n");
            if (foodComments.isEmpty()) {
                result.append("暂无美食评论\n\n");
            } else {
                for (Comment comment : foodComments) {
                    String content = comment.getContent() != null && comment.getContent().length() > 80
                        ? comment.getContent().substring(0, 80) + "..." : comment.getContent();
                    result.append(String.format("- 类型: food\n  评论ID: %s\n  用户ID: %s\n  美食ID: %s\n  评分: %.1f\n  内容: %s\n  时间: %s\n\n",
                        comment.getId(),
                        comment.getUserId(),
                        comment.getFoodId(),
                        comment.getRating() != null ? comment.getRating().doubleValue() : 0.0,
                        content,
                        comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : "未知"));
                }
            }

            result.append("帖子评论：\n");
            if (forumComments.isEmpty()) {
                result.append("暂无帖子评论\n");
            } else {
                for (ForumComment comment : forumComments) {
                    String content = comment.getContent() != null && comment.getContent().length() > 80
                        ? comment.getContent().substring(0, 80) + "..." : comment.getContent();
                    result.append(String.format("- 类型: forum\n  评论ID: %s\n  用户ID: %s\n  帖子ID: %s\n  内容: %s\n  时间: %s\n\n",
                        comment.getId(),
                        comment.getUserId(),
                        comment.getPostId(),
                        content,
                        comment.getCreatedAt() != null ? comment.getCreatedAt().format(formatter) : "未知"));
                }
            }

            return result.toString();
        } catch (Exception e) {
            log.error("获取最新评论汇总失败", e);
            return "获取最新评论汇总失败：" + e.getMessage();
        }
    }

    @Tool("查询指定时间段内新增的帖子和帖子评论数量")
    public String getForumActivityStatistics(int days) {
        try {
            log.info("AI调用工具：查询帖子活跃统计，天数={}", days);
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            Long newPosts = forumPostMapper.selectCount(
                new LambdaQueryWrapper<ForumPost>().ge(ForumPost::getCreatedAt, startTime)
            );
            Long newForumComments = forumCommentMapper.selectCount(
                new LambdaQueryWrapper<ForumComment>().ge(ForumComment::getCreatedAt, startTime)
            );
            return String.format("最近%d天帖子活跃统计：\n新增帖子：%d 条\n新增帖子评论：%d 条\n平均每天新增帖子：%.1f 条",
                days, newPosts, newForumComments, newPosts.doubleValue() / days);
        } catch (Exception e) {
            log.error("查询帖子活跃统计失败", e);
            return "查询帖子活跃统计失败：" + e.getMessage();
        }
    }

    @Tool("生成Excel报表。types参数为逗号分隔的类型，支持：users=用户, foods=美食, comments=评论, posts=帖子。可组合，如 users,foods 或 users,foods,comments,posts")
    public String generateExcel(String types) {
        try {
            log.info("AI调用工具：生成Excel，类型={}", types);
            String url = excelGeneratorService.generate(types);
            return "Excel 已生成，下载地址：" + url;
        } catch (Exception e) {
            log.error("生成Excel失败", e);
            return "生成Excel失败：" + e.getMessage();
        }
    }

    @Tool("生成PPT报告。types参数为逗号分隔的类型，支持：users=用户, foods=美食, comments=评论, posts=帖子。可组合，如 foods,posts 或 users,foods,comments,posts")
    public String generatePpt(String types) {
        try {
            log.info("AI调用工具：生成PPT，类型={}", types);
            String url = pptGeneratorService.generate(types);
            return "PPT 已生成，下载地址：" + url;
        } catch (Exception e) {
            log.error("生成PPT失败", e);
            return "生成PPT失败：" + e.getMessage();
        }
    }
}
