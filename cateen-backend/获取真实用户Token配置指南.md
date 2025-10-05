# 获取真实用户Token配置指南

## 🎯 目标
配置真实的微信小程序，获取真实用户的Token，而不是使用模拟数据。

## 🛠️ 配置步骤

### 步骤1: 获取微信小程序信息

#### 1.1 登录微信公众平台
- 访问：https://mp.weixin.qq.com/
- 使用小程序管理员账号登录

#### 1.2 获取AppID和AppSecret
1. 进入小程序管理后台
2. 点击"开发" -> "开发管理" -> "开发设置"
3. 找到"开发者ID"部分：
   - **AppID（小程序ID）**：类似 `wx1234567890abcdef`
   - **AppSecret（小程序密钥）**：点击"生成"或"重置"获取

### 步骤2: 更新后端配置

#### 2.1 修改配置文件
编辑 `src/main/resources/application.yml`：

```yaml
# 微信小程序配置
wechat:
  mini:
    # 替换为你的真实AppID
    appid: wx1234567890abcdef
    # 替换为你的真实AppSecret
    secret: your_real_app_secret_here
```

#### 2.2 重启后端服务
```bash
cd xawl_cateen/cateen-backend
mvn clean spring-boot:run
```

### 步骤3: 获取真实用户Token

#### 3.1 在小程序中获取code
在小程序前端代码中：

```javascript
// 小程序登录
wx.login({
  success: function(res) {
    if (res.code) {
      // 获取到code，发送到后端
      console.log('获取到code:', res.code);
      
      // 调用后端登录接口
      wx.request({
        url: 'http://localhost:8080/api/mini/auth/login',
        method: 'POST',
        data: {
          code: res.code,
          userInfo: {
            nickName: '用户昵称',
            avatarUrl: '用户头像URL'
          }
        },
        success: function(response) {
          console.log('登录成功:', response.data);
          // 保存token
          wx.setStorageSync('token', response.data.data.token);
        }
      });
    }
  }
});
```

#### 3.2 在Swagger中测试
使用真实的code（从小程序获取）：

```json
{
  "code": "真实的code_from_miniprogram",
  "userInfo": {
    "nickName": "真实用户昵称",
    "avatarUrl": "真实用户头像URL"
  }
}
```

## 🔧 代码说明

### 当前代码逻辑
```java
private String getOpenIdByCode(String code) {
    // 开发环境测试code直接返回模拟openid
    if ("test_code_001".equals(code) || "test_code_002".equals(code) || "test_code_003".equals(code)) {
        String testOpenId = "test_openid_" + code.substring(code.length() - 3);
        log.info("开发环境测试，返回模拟openid: {}", testOpenId);
        return testOpenId;
    }
    
    // 调用真实微信接口
    String url = String.format(
        "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
        appId, appSecret, code
    );
    
    // 发送请求到微信服务器
    Map<String, Object> response = restTemplate.getForObject(url, Map.class);
    
    if (response != null && response.containsKey("openid")) {
        String openId = (String) response.get("openid");
        log.info("获取微信openid成功: {}", openId);
        return openId;
    } else {
        log.error("获取微信openid失败: {}", response);
        throw new RuntimeException("获取微信openid失败: " + response);
    }
}
```

## 📋 测试流程

### 1. 配置真实信息
- 更新 `application.yml` 中的AppID和AppSecret
- 重启后端服务

### 2. 获取真实code
- 在小程序中调用 `wx.login()` 获取code
- 或者使用微信开发者工具

### 3. 测试登录
- 在Swagger中使用真实code测试登录
- 获取真实用户的Token

### 4. 测试其他接口
- 使用真实Token测试其他接口

## 🎯 关键点

### 1. 环境区分
- **测试环境**：使用 `test_code_001` 等模拟数据
- **生产环境**：使用真实AppID和AppSecret

### 2. 安全考虑
- **AppSecret保密**：不要提交到代码仓库
- **环境变量**：生产环境建议使用环境变量

### 3. 错误处理
- 微信接口可能返回错误
- 需要处理网络异常
- 需要处理微信API限制

## 🚀 快速开始

1. **获取AppID和AppSecret**
2. **更新配置文件**
3. **重启后端服务**
4. **在小程序中获取code**
5. **在Swagger中测试登录**
6. **获取真实Token**

## 🔍 常见问题

### Q: 微信接口返回错误？
A: 检查AppID和AppSecret是否正确，code是否有效

### Q: 如何在小程序中获取code？
A: 使用 `wx.login()` API

### Q: 如何测试真实用户？
A: 使用微信开发者工具或真机调试

**配置完成后，就可以获取真实用户的Token了！**
