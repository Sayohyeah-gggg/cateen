package com.xawl.cateen.service.mini;

import com.xawl.cateen.dto.mini.WxLoginDTO;
import com.xawl.cateen.entity.Profile;
import com.xawl.cateen.mapper.ProfileMapper;
import com.xawl.cateen.util.JwtUtil;
import com.xawl.cateen.vo.mini.WxLoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信认证服务
 *
 * @author xawl
 * @date 2025-10-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatAuthService {

    private final ProfileMapper profileMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${wechat.mini.appid:your_appid}")
    private String appId;

    @Value("${wechat.mini.secret:your_secret}")
    private String appSecret;

    /**
     * 微信小程序登录
     */
    public WxLoginVO wxLogin(WxLoginDTO loginDTO) {
        // 1. 调用微信接口获取openid和session_key
        String openId = getOpenIdByCode(loginDTO.getCode());
        
        // 2. 查询或创建用户
        Profile user = getOrCreateUser(openId, loginDTO.getNickName(), loginDTO.getAvatarUrl());
        
        // 3. 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getNickname(), user.getRole());
        
        // 4. 返回结果
        return WxLoginVO.builder()
                .token(token)
                .userId(user.getId())
                .openId(user.getWechatOpenid())
                .nickName(user.getNickname())
                .avatarUrl(user.getAvatar())
                .isNewUser(user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)))
                .build();
    }

    /**
     * 通过code获取openid
     */
    private String getOpenIdByCode(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("openid")) {
                String openId = (String) response.get("openid");
                log.info("获取微信openid成功: {}", openId);
                return openId;
            } else {
                log.error("获取微信openid失败: {}", response);
                // 开发环境模拟
                if ("test_code_123".equals(code)) {
                    return "test_openid_" + UUID.randomUUID().toString().substring(0, 8);
                }
                throw new RuntimeException("获取微信openid失败");
            }
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            // 开发环境模拟
            if ("test_code_123".equals(code)) {
                return "test_openid_" + UUID.randomUUID().toString().substring(0, 8);
            }
            throw new RuntimeException("调用微信接口失败: " + e.getMessage());
        }
    }

    /**
     * 查询或创建用户
     */
    private Profile getOrCreateUser(String openId, String nickName, String avatarUrl) {
        // 查询用户
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("wechat_openid", openId);
        Profile user = profileMapper.selectByMap(queryMap).stream().findFirst().orElse(null);
        
        if (user == null) {
            // 创建新用户
            user = new Profile();
            user.setId(UUID.randomUUID().toString().replace("-", ""));
            user.setWechatOpenid(openId);
            user.setNickname(nickName != null ? nickName : "微信用户");
            user.setAvatar(avatarUrl);
            user.setRole("user");
            user.setStatus("active");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            profileMapper.insert(user);
            log.info("创建新用户成功: {}", user.getId());
        } else {
            // 更新用户信息
            if (nickName != null && !nickName.equals(user.getNickname())) {
                user.setNickname(nickName);
            }
            if (avatarUrl != null && !avatarUrl.equals(user.getAvatar())) {
                user.setAvatar(avatarUrl);
            }
            user.setUpdatedAt(LocalDateTime.now());
            profileMapper.updateById(user);
            log.info("更新用户信息成功: {}", user.getId());
        }
        
        return user;
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String userId) {
        // 查询用户信息
        Profile user = profileMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return jwtUtil.generateToken(user.getId(), user.getNickname(), user.getRole());
    }
}
