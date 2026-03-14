package com.xawl.cateen.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xawl.cateen.entity.ForumComment;
import com.xawl.cateen.entity.ForumLike;
import com.xawl.cateen.entity.ForumPost;
import com.xawl.cateen.mapper.ForumCommentMapper;
import com.xawl.cateen.mapper.ForumLikeMapper;
import com.xawl.cateen.mapper.ForumPostMapper;
import com.xawl.cateen.vo.ForumCommentVO;
import com.xawl.cateen.vo.ForumPostVO;
import com.xawl.cateen.vo.PageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostMapper postMapper;
    private final ForumCommentMapper commentMapper;
    private final ForumLikeMapper likeMapper;
    private final ObjectMapper objectMapper;

    /**
     * 分页获取帖子列表
     */
    public PageVO<ForumPostVO> getPostPage(int page, int limit, String currentUserId) {
        Page<ForumPostVO> pageParam = new Page<>(page, limit);
        Page<ForumPostVO> result = postMapper.selectPostPage(pageParam, currentUserId);

        // 将 images JSON字符串转为 List，直接用 VO 中的 images 字段，无需再查库
        result.getRecords().forEach(vo -> {
            vo.setImageList(parseImages(vo.getImages()));
            vo.setImages(null); // 清空原始字段，不暴露给前端
        });

        return PageVO.<ForumPostVO>builder()
                .list(result.getRecords())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .total(result.getTotal())
                .pages(result.getPages())
                .build();
    }

    /**
     * 发布帖子
     */
    @Transactional
    public ForumPost createPost(String userId, String content, List<String> images) {
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setContent(content);
        post.setStatus("approved");
        post.setLikeCount(0);
        post.setCommentCount(0);

        if (images != null && !images.isEmpty()) {
            try {
                post.setImages(objectMapper.writeValueAsString(images));
            } catch (Exception e) {
                log.warn("序列化图片列表失败", e);
            }
        }

        postMapper.insert(post);
        return post;
    }

    /**
     * 删除帖子（仅本人）
     */
    @Transactional
    public void deletePost(String postId, String userId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该帖子");
        }
        postMapper.deleteById(postId);
    }

    /**
     * 获取帖子评论列表
     */
    public PageVO<ForumCommentVO> getCommentPage(String postId, int page, int limit) {
        Page<ForumCommentVO> pageParam = new Page<>(page, limit);
        Page<ForumCommentVO> result = commentMapper.selectCommentPage(pageParam, postId);

        return PageVO.<ForumCommentVO>builder()
                .list(result.getRecords())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .total(result.getTotal())
                .pages(result.getPages())
                .build();
    }

    /**
     * 发布评论
     */
    @Transactional
    public ForumComment createComment(String postId, String userId, String content) {
        // 检查帖子是否存在
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }

        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.updateById(post);

        return comment;
    }

    /**
     * 点赞/取消点赞帖子，返回 true=已点赞，false=已取消
     */
    @Transactional
    public boolean toggleLike(String postId, String userId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }

        LambdaQueryWrapper<ForumLike> wrapper = new LambdaQueryWrapper<ForumLike>()
                .eq(ForumLike::getPostId, postId)
                .eq(ForumLike::getUserId, userId);

        ForumLike existing = likeMapper.selectOne(wrapper);

        if (existing != null) {
            // 取消点赞
            likeMapper.delete(wrapper);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            postMapper.updateById(post);
            return false;
        } else {
            // 点赞
            ForumLike like = new ForumLike();
            like.setPostId(postId);
            like.setUserId(userId);
            likeMapper.insert(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            return true;
        }
    }

    /**
     * 管理端分页查询帖子（支持关键词、状态筛选）
     */
    public PageVO<ForumPostVO> adminGetPostPage(int page, int limit, String keyword, String status) {
        Page<ForumPostVO> pageParam = new Page<>(page, limit);
        Page<ForumPostVO> result = postMapper.adminSelectPostPage(pageParam, keyword, status);
        result.getRecords().forEach(vo -> {
            vo.setImageList(parseImages(vo.getImages()));
            vo.setImages(null);
        });
        return PageVO.<ForumPostVO>builder()
                .list(result.getRecords())
                .pageNum(result.getCurrent())
                .pageSize(result.getSize())
                .total(result.getTotal())
                .pages(result.getPages())
                .build();
    }

    /**
     * 管理端更新帖子状态
     */
    @Transactional
    public void adminUpdatePostStatus(String postId, String status) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }
        post.setStatus(status);
        postMapper.updateById(post);
    }

    /**
     * 管理端强制删除帖子
     */
    @Transactional
    public void adminDeletePost(String postId) {
        ForumPost post = postMapper.selectById(postId);
        if (post == null || post.getDeleted() == 1) {
            throw new RuntimeException("帖子不存在");
        }
        postMapper.deleteById(postId);
    }

    private List<String> parseImages(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析图片JSON失败: {}", imagesJson);
            return Collections.emptyList();
        }
    }
}
