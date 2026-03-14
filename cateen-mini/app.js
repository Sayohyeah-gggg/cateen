// app.js
var api = require('./utils/api');

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false,
    theme: 'light'
  },

  onLaunch: function() {
    this.initTheme();

    var token = wx.getStorageSync('token');
    var userInfo = wx.getStorageSync('userInfo');
    var isLoggedIn = wx.getStorageSync('isLoggedIn');

    if (token && isLoggedIn) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo || null;
      this.globalData.isLoggedIn = true;
    }

    console.log('App launched');
  },

  initTheme: function() {
    var storedTheme = wx.getStorageSync('theme');
    var theme = 'light';

    if (storedTheme === 'light' || storedTheme === 'dark') {
      theme = storedTheme;
    } else {
      try {
        var systemInfo = wx.getSystemInfoSync();
        theme = systemInfo.theme === 'dark' ? 'dark' : 'light';
      } catch (error) {
        theme = 'light';
      }
    }

    this.globalData.theme = theme;
    wx.setStorageSync('theme', theme);
    this.themeListeners = [];
  },

  getCurrentTheme: function() {
    return this.globalData.theme || 'light';
  },

  setTheme: function(theme) {
    if (theme !== 'light' && theme !== 'dark') {
      return;
    }

    if (this.globalData.theme === theme) {
      return;
    }

    this.globalData.theme = theme;
    wx.setStorageSync('theme', theme);

    if (this.themeListeners && this.themeListeners.length) {
      this.themeListeners.forEach(function(listener) {
        if (typeof listener === 'function') {
          listener(theme);
        }
      });
    }
  },

  subscribeThemeChange: function(listener) {
    if (typeof listener !== 'function') {
      return;
    }

    if (!this.themeListeners) {
      this.themeListeners = [];
    }

    if (this.themeListeners.indexOf(listener) === -1) {
      this.themeListeners.push(listener);
    }
  },

  unsubscribeThemeChange: function(listener) {
    if (!this.themeListeners || typeof listener !== 'function') {
      return;
    }

    this.themeListeners = this.themeListeners.filter(function(item) {
      return item !== listener;
    });
  },

  wxLogin: function() {
    var self = this;

    return new Promise(function(resolve, reject) {
      wx.login({
        success: function(loginRes) {
          if (!loginRes.code) {
            reject(new Error('login code missing'));
            return;
          }

          wx.getUserProfile({
            desc: 'Used to complete your profile',
            success: function() {
              var nickname = self.generateRandomNickname();
              var avatar = self.generateRandomAvatar(nickname);
              self.loginToBackend(loginRes.code, {
                nickName: nickname,
                avatarUrl: avatar
              }).then(resolve).catch(function(error) {
                console.warn('backend login failed, using mock:', error);
                self.useMockLogin(resolve);
              });
            },
            fail: function() {
              var nickname = self.generateRandomNickname();
              var avatar = self.generateRandomAvatar(nickname);
              self.loginToBackend(loginRes.code, {
                nickName: nickname,
                avatarUrl: avatar
              }).then(resolve).catch(function(error) {
                console.warn('backend login failed, using mock:', error);
                self.useMockLogin(resolve);
              });
            }
          });
        },
        fail: function(error) {
          console.warn('wx.login failed, using mock:', error);
          self.useMockLogin(resolve);
        }
      });
    });
  },

  generateRandomNickname: function() {
    var prefixes = ['快乐', '酷酷', '聪明', '勇敢', '疾风', '幸运'];
    var nouns = ['吃货', '同学', '厨神', '探索者', '伙伴', '食客'];
    return prefixes[Math.floor(Math.random() * prefixes.length)] + nouns[Math.floor(Math.random() * nouns.length)];
  },

  generateRandomAvatar: function(seed) {
    var styles = ['adventurer', 'avataaars', 'bottts', 'fun-emoji', 'micah', 'personas'];
    var style = styles[Math.floor(Math.random() * styles.length)];
    var avatarSeed = seed || ('user_' + Date.now());
    return 'https://api.dicebear.com/7.x/' + style + '/svg?seed=' + encodeURIComponent(avatarSeed);
  },

  useMockLogin: function(resolve) {
    var nickname = this.generateRandomNickname();
    var avatar = this.generateRandomAvatar(nickname);
    var loginData = {
      token: 'mock_token_' + Date.now(),
      user_id: 'mock_user_' + Date.now(),
      open_id: 'mock_openid',
      nick_name: nickname,
      avatar_url: avatar,
      is_new_user: false
    };

    wx.setStorageSync('token', loginData.token);
    wx.setStorageSync('isLoggedIn', true);
    wx.setStorageSync('userInfo', {
      userId: loginData.user_id,
      openId: loginData.open_id,
      nickName: loginData.nick_name,
      avatarUrl: loginData.avatar_url,
      isNewUser: loginData.is_new_user
    });

    this.globalData.token = loginData.token;
    this.globalData.userInfo = {
      userId: loginData.user_id,
      openId: loginData.open_id,
      nickName: loginData.nick_name,
      avatarUrl: loginData.avatar_url,
      isNewUser: loginData.is_new_user
    };
    this.globalData.isLoggedIn = true;

    resolve(loginData);
  },

  loginToBackend: function(code, userInfo) {
    var self = this;

    return new Promise(function(resolve, reject) {
      api.auth.login({
        code: code,
        nickName: userInfo.nickName || '',
        avatarUrl: userInfo.avatarUrl || ''
      }).then(function(loginData) {
        var userId = loginData.userId || loginData.user_id;
        var openId = loginData.openId || loginData.open_id;
        var nickName = loginData.nickName || loginData.nick_name || '用户';
        var avatarUrl = loginData.avatarUrl || loginData.avatar_url || '/images/default-avatar.jpg';
        var isNewUser = loginData.isNewUser !== undefined ? loginData.isNewUser : loginData.is_new_user;

        wx.setStorageSync('token', loginData.token);
        wx.setStorageSync('isLoggedIn', true);
        wx.setStorageSync('userInfo', {
          userId: userId,
          openId: openId,
          nickName: nickName,
          avatarUrl: avatarUrl,
          isNewUser: isNewUser
        });

        self.globalData.token = loginData.token;
        self.globalData.userInfo = {
          userId: userId,
          openId: openId,
          nickName: nickName,
          avatarUrl: avatarUrl,
          isNewUser: isNewUser
        };
        self.globalData.isLoggedIn = true;

        resolve(loginData);
      }).catch(function(error) {
        reject(error);
      });
    });
  },

  logout: function() {
    wx.removeStorageSync('token');
    wx.removeStorageSync('isLoggedIn');
    wx.removeStorageSync('userInfo');
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
  }
});
