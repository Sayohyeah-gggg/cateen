package com.xawl.cateen.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xawl.cateen.common.Result;
import com.xawl.cateen.dto.RoleDTO;
import com.xawl.cateen.dto.StatusDTO;
import com.xawl.cateen.service.ProfileService;
import com.xawl.cateen.vo.PageVO;
import com.xawl.cateen.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端 - 用户管理控制器
 *
 * @author xawl
 * @date 2025-10-03
 */
@Api(tags = "管理端-用户管理")
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final ProfileService profileService;

    /**
     * 分页查询用户列表
     */
    @ApiOperation(value = "分页查询用户列表", notes = "支持关键词搜索、角色筛选、状态筛选")
    @GetMapping
    public Result<PageVO<UserVO>> getUserPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long pageNum,
            @ApiParam(value = "每页数量", example = "20") @RequestParam(defaultValue = "20") Long pageSize,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam(value = "角色(admin/user)") @RequestParam(required = false) String role,
            @ApiParam(value = "状态(active/inactive)") @RequestParam(required = false) String status) {
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
    @ApiOperation(value = "获取用户详情", notes = "根据用户ID获取用户的详细信息")
    @GetMapping("/{id}")
    public Result<UserVO> getUserDetail(@ApiParam(value = "用户ID", required = true) @PathVariable String id) {
        log.info("获取用户详情: id={}", id);
        UserVO userVO = profileService.getUserDetail(id);
        return Result.success(userVO);
    }

    /**
     * 更新用户角色
     */
    @ApiOperation(value = "更新用户角色", notes = "修改用户的角色权限")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id, 
            @ApiParam(value = "角色信息", required = true) @Valid @RequestBody RoleDTO dto) {
        log.info("更新用户角色: id={}, role={}", id, dto.getRole());
        profileService.updateRole(id, dto.getRole());
        return Result.success("角色更新成功", null);
    }

    /**
     * 更新用户状态
     */
    @ApiOperation(value = "更新用户状态", notes = "启用或禁用用户账户")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @ApiParam(value = "用户ID", required = true) @PathVariable String id, 
            @ApiParam(value = "状态信息", required = true) @Valid @RequestBody StatusDTO dto) {
        log.info("更新用户状态: id={}, status={}", id, dto.getStatus());
        profileService.updateStatus(id, dto.getStatus());
        return Result.success("状态更新成功", null);
    }

    /**
     * 删除用户
     */
    @ApiOperation(value = "删除用户", notes = "删除指定的用户账户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@ApiParam(value = "用户ID", required = true) @PathVariable String id) {
        log.info("删除用户: id={}", id);
        profileService.deleteUser(id);
        return Result.success("用户删除成功", null);
    }

}
