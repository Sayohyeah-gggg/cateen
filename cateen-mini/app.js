// app.js
var api = require('./utils/api');

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false
  },

  onLaunch: function() {
    console.log('小程序启动');
  },

  // 微信登录 - 获取code并尝试获取用户信息
  wxLogin: function() {
    var self = this;
    return new Promise(function(resolve, reject) {
      console.log('开始微信登录...');
      
      wx.login({
        success: function(res) {
          console.log('获取到code:', res.code);
          
          // 检查是否获取到有效的code
          if (!res.code) {
            console.error('未获取到有效的登录code');
            wx.showToast({
              title: '登录失败，请重试',
              icon: 'none'
            });
            reject(new Error('未获取到有效的登录code'));
            return;
          }
          
          // 尝试获取用户信息
          wx.getUserProfile({
            desc: '用于完善用户资料',
            success: function(userRes) {
              console.log('获取用户信息成功，但使用随机网名和头像');
              // 生成随机网名和头像
              var randomNickname = self.generateRandomNickname();
              var randomAvatar = self.generateRandomAvatar(randomNickname);
              
              var customUserInfo = {
                nickName: randomNickname,
                avatarUrl: randomAvatar,
                gender: userRes.userInfo.gender,
                country: userRes.userInfo.country,
                province: userRes.userInfo.province,
                city: userRes.userInfo.city,
                language: userRes.userInfo.language
              };
              console.log('使用随机网名:', randomNickname);
              console.log('使用随机头像:', randomAvatar);
              
              self.loginToBackend(res.code, customUserInfo)
                .then(resolve)
                .catch(function(err) {
                  console.error('后端登录失败，使用模拟数据:', err);
                  // 后端登录失败时，使用模拟数据继续
                  self.useMockLogin(resolve);
                });
            },
            fail: function(err) {
              console.log('用户拒绝授权或获取用户信息失败，生成随机网名和头像');
              // 如果用户拒绝授权，生成随机网名和头像
              var randomNickname = self.generateRandomNickname();
              var randomAvatar = self.generateRandomAvatar(randomNickname);
              
              var mockUserInfo = {
                nickName: randomNickname,
                avatarUrl: randomAvatar,
                gender: Math.random() > 0.5 ? 1 : 2,
                country: '中国',
                province: '广东',
                city: '深圳',
                language: 'zh_CN'
              };
              console.log('使用随机网名:', randomNickname);
              console.log('使用随机头像:', randomAvatar);
              
              self.loginToBackend(res.code, mockUserInfo)
                .then(resolve)
                .catch(function(err) {
                  console.error('后端登录失败，使用模拟数据:', err);
                  // 后端登录失败时，使用模拟数据继续
                  self.useMockLogin(resolve);
                });
            }
          });
        },
        fail: function(err) {
          console.error('微信登录失败:', err);
          
          // 显示友好的错误提示
          if (err.errMsg && err.errMsg.indexOf('access_token expired') > -1) {
            wx.showModal({
              title: '登录提示',
              content: '微信登录态已过期，请关闭并重新打开小程序，或清除缓存后重试',
              showCancel: false,
              confirmText: '我知道了'
            });
          } else {
            wx.showToast({
              title: '登录失败，请重试',
              icon: 'none'
            });
          }
          
          // 使用模拟登录继续
          console.log('登录失败，使用模拟数据继续');
          self.useMockLogin(resolve);
        }
      });
    });
  },
  
  // 生成随机网名
  generateRandomNickname: function() {
    var prefixes = ['孤独的', '快乐的', '忧郁的', '神秘的', '可爱的', '帅气的', '温柔的', '冷酷的', '阳光的', '月光下的', '星空中的', '梦幻的', '浪漫的', '自由的', '勇敢的', '善良的', '聪明的', '幽默的', '安静的', '活泼的'];
    
    var nouns = ['旅人', '诗人', '画家', '音乐家', '舞者', '剑客', '骑士', '法师', '猎人', '探险家', '美食家', '吃货', '猫咪', '小狗', '兔子', '熊猫', '企鹅', '海豚', '蝴蝶', '萤火虫', '星星', '月亮', '太阳', '云朵', '雨滴', '雪花', '樱花', '玫瑰', '向日葵', '薰衣草'];
    
    var suffixes = ['', '呀', '啊', '呢', '哦', '吖', '酱', '君', '桑', '殿', 'sama'];
    
    var prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
    var noun = nouns[Math.floor(Math.random() * nouns.length)];
    var suffix = suffixes[Math.floor(Math.random() * suffixes.length)];
    
    return prefix + noun + suffix;
  },
  
  // 生成随机头像URL
  generateRandomAvatar: function(seed) {
    // 使用 DiceBear Avatars API 生成随机头像
    var styles = ['adventurer', 'avataaars', 'bottts', 'fun-emoji', 'identicon', 'lorelei', 'micah', 'miniavs', 'open-peeps', 'personas'];
    var style = styles[Math.floor(Math.random() * styles.length)];
    
    // 如果没有提供seed，生成一个随机seed
    if (!seed) {
      seed = 'user_' + Date.now() + '_' + Math.random().toString(36).substring(7);
    }
    
    // 返回头像URL
    return 'https://api.dicebear.com/7.x/' + style + '/svg?seed=' + encodeURIComponent(seed);
  },
  
  // 使用模拟登录数据
  useMockLogin: function(resolve) {
    var randomNickname = this.generateRandomNickname();
    var randomAvatar = this.generateRandomAvatar(randomNickname);
    
    var mockLoginData = {
      token: 'mock_token_' + Date.now(),
      user_id: 'mock_user_' + Date.now(),
      open_id: 'mock_openid',
      nick_name: randomNickname,
      avatar_url: randomAvatar,
      is_new_user: false
    };
    
    // 保存模拟登录信息
    wx.setStorageSync('token', mockLoginData.token);
    wx.setStorageSync('isLoggedIn', true);
    wx.setStorageSync('userInfo', {
      userId: mockLoginData.user_id,
      openId: mockLoginData.open_id,
      nickName: mockLoginData.nick_name,
      avatarUrl: mockLoginData.avatar_url,
      isNewUser: mockLoginData.is_new_user
    });

    // 更新全局数据
    this.globalData.token = mockLoginData.token;
    this.globalData.userInfo = {
      userId: mockLoginData.user_id,
      openId: mockLoginData.open_id,
      nickName: mockLoginData.nick_name,
      avatarUrl: mockLoginData.avatar_url,
      isNewUser: mockLoginData.is_new_user
    };
    this.globalData.isLoggedIn = true;

    console.log('使用模拟登录数据:', mockLoginData);
    resolve(mockLoginData);
  },

  // 调用后端登录接口
  loginToBackend: function(code, userInfo) {
    var self = this;
    return new Promise(function(resolve, reject) {
      console.log('调用后端登录接口...');
      console.log('原始用户信息:', userInfo);
      
      // 后端期望的数据格式：直接传递 nickName 和 avatarUrl
      var requestData = {
        code: code,
        nickName: userInfo.nickName || '',
        avatarUrl: userInfo.avatarUrl || ''
      };
      
      console.log('发送给后端的数据:', requestData);
      
      api.auth.login(requestData)
        .then(function(loginData) {
          console.log('后端登录返回的数据:', loginData);
          
          // 后端返回的是驼峰式字段名，需要适配
          var userId = loginData.userId || loginData.user_id;
          var openId = loginData.openId || loginData.open_id;
          var nickName = loginData.nickName || loginData.nick_name;
          var avatarUrl = loginData.avatarUrl || loginData.avatar_url;
          var isNewUser = loginData.isNewUser !== undefined ? loginData.isNewUser : loginData.is_new_user;
          
          // 保存登录信息
          wx.setStorageSync('token', loginData.token);
          wx.setStorageSync('isLoggedIn', true);
          wx.setStorageSync('userInfo', {
            userId: userId,
            openId: openId,
            nickName: nickName,
            avatarUrl: avatarUrl || '/images/default-avatar.jpg',
            isNewUser: isNewUser
          });

          // 更新全局数据
          self.globalData.token = loginData.token;
          self.globalData.userInfo = {
            userId: userId,
            openId: openId,
            nickName: nickName,
            avatarUrl: avatarUrl || '/images/default-avatar.jpg',
            isNewUser: isNewUser
          };
          self.globalData.isLoggedIn = true;

          console.log('登录成功，token已保存:', loginData.token);
          console.log('全局用户信息已更新:', self.globalData.userInfo);
          resolve(loginData);
        })
        .catch(function(err) {
          console.error('后端登录请求失败:', err);
          reject(err);
        });
    });
  },

  // 退出登录
  logout: function() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('isLoggedIn');
    wx.removeStorageSync('userInfo');
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
    console.log('用户已退出登录');
  }
})
