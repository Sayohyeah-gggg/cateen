package com.xawl.cateen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.RoleDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.ProfileService;
import com.xawl.cateen.vo.PageVO;
import com.xawl.cateen.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户管理控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<PageVO<UserVO>> getUserPage(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {
        log.info("分页查询用户列表: pageNum={}, pageSize={}", pageNum, pageSize);
        Page<UserVO> page = profileService.getUserPage(pageNum, pageSize, keyword, role, status);
        
        PageVO<UserVO> pageVO = PageVO.<UserVO>builder()
                .list(page.getRecords())
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .build();
        
        return Result.success(pageVO);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@PathVariable String id) {
        log.info("获取用户详情: id={}", id);
        UserVO userVO = profileService.getUserDetail(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable String id, @Valid @RequestBody RoleDTO dto) {
        log.info("更新用户角色: id={}, role={}", id, dto.getRole());
        profileService.updateRole(id, dto.getRole());
        return Result.success("角色更新成功", null);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable String id, @Valid @RequestBody StatusDTO dto) {
        log.info("更新用户状态: id={}, status={}", id, dto.getStatus());
        profileService.updateStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        log.info("删除用户: id={}", id);
        profileService.deleteUser(id);
        return Result.success("用户删除成功", null);
    }

}

