package com.xawl.cateen.controller.mini;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.mini.UserProfileDTO;
import com.xawl.cateen.service.mini.MiniUserService;
import com.xawl.cateen.util.SecurityUtils;
import com.xawl.cateen.vo.mini.MiniCommentVO;
import com.xawl.cateen.vo.mini.MiniUserProfileVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 小程序端 - 用户信息控制器
 *
 * @author xawl
 * @date 2025-10-05
 */
@Api(tags = "小程序-用户信息")
@Slf4j
@RestController
@RequestMapping("/api/mini/user")
@RequiredArgsConstructor
public class MiniUserController {

    private final MiniUserService miniUserService;

    /**
     * 获取用户资料
     */
    @ApiOperation(value = "获取用户资料", notes = "获取当前用户的详细信息")
    @GetMapping("/profile")
    public Result<MiniUserProfileVO> getProfile() {
        log.info("获取用户资料");
        
        String userId = SecurityUtils.getCurrentUserId();
        MiniUserProfileVO profile = miniUserService.getProfile(userId);
        
        if (profile == null) {
            return Result.error("用户不存在");
        }
        
        return Result.success(profile);
    }

    /**
     * 更新用户资料
     */
    @ApiOperation(value = "更新用户资料", notes = "更新当前用户的昵称、头像等信息")
    @PutMapping("/profile")
    public Result<?> updateProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        log.info("更新用户资料，nickName: {}", profileDTO.getNickName());
        
        String userId = SecurityUtils.getCurrentUserId();
        miniUserService.updateProfile(userId, profileDTO);
        
        return Result.success("更新成功");
    }

    /**
     * 获取评论历史
     */
    @ApiOperation(value = "获取评论历史", notes = "获取当前用户的评论历史")
    @GetMapping("/comments")
    public Result<Page<MiniCommentVO>> getComments(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer limit
    ) {
        log.info("获取用户评论历史，page: {}, limit: {}", page, limit);
        
        String userId = SecurityUtils.getCurrentUserId();
        Page<MiniCommentVO> comments = miniUserService.getComments(userId, page, limit);
        
        return Result.success(comments);
    }
}