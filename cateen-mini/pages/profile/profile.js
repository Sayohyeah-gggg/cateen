// pages/profile/profile.js
var auth = require('../../utils/auth');
var api = require('../../utils/api');

function normalizeTasteProfile(profile) {
  if (!profile) return null;

  var prefs = profile.preferences || [];
  var tags = profile.tags || [];

  var commentCount = profile.commentCount != null ? profile.commentCount : (profile.comment_count || 0);
  var summaryText = commentCount > 0 ? ('基于' + commentCount + '条评论') : '基于历史评分';

  return {
    avgRating: profile.avgRating != null ? profile.avgRating : (profile.avg_rating || 0),
    commentCount: commentCount,
    summaryText: summaryText,
    tagCount: tags.length,
    preferences: prefs.map(function(pref) {
      return {
        type: pref.type || pref.preference_type,
        label: pref.label || pref.preference,
        count: pref.count || 0,
        avgScore: pref.avgScore != null ? pref.avgScore : (pref.avg_score || 0),
        percentage: pref.percentage != null ? pref.percentage : (pref.percentage || 0),
        color: pref.color
      };
    }),
    tags: tags.map(function(tag) {
      return {
        id: tag.id || tag.tag_id,
        name: tag.name || tag.tag_name,
        color: tag.color || tag.tag_color,
        count: tag.count || 0
      };
    })
  };
}

Page({
  data: {
    theme: 'light',
    themeClass: 'theme-light',
    currentTheme: 'light',

    isLoggedIn: false,
    userInfo: {
      avatarUrl: '/images/default-avatar.jpg',
      nickName: '游客',
      collectCount: 0,
      commentCount: 0
    },

    tasteProfile: null,

    showAboutModal: false
  },

  onLoad: function() {
    this.syncTheme();
    this.refreshProfile();
  },

  onShow: function() {
    this.syncTheme();
    this.refreshProfile();
    this.syncCustomTabBar();
  },

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';

    this.setData({
      theme: theme,
      currentTheme: theme,
      themeClass: 'theme-' + theme
    });
  },

  syncCustomTabBar: function() {
    if (typeof this.getTabBar !== 'function') {
      return;
    }

    var tabBar = this.getTabBar();
    if (tabBar && tabBar.updateSelected) {
      tabBar.updateSelected();
    }
  },

  refreshProfile: function() {
    var loggedIn = auth.isLoggedIn();
    var user = auth.getCurrentUser() || {};

    this.setData({
      isLoggedIn: loggedIn,
      userInfo: {
        avatarUrl: user.avatarUrl || '/images/default-avatar.jpg',
        nickName: user.nickName || '吃货朋友',
        collectCount: 0,
        commentCount: 0
      }
    });

    if (loggedIn) {
      this.loadUserStats();
    }
  },

  loadUserStats: function() {
    var self = this;

    Promise.all([
      api.user.getCollections({ page: 1, pageSize: 1 }),
      api.user.getComments({ page: 1, pageSize: 1 }),
      api.user.getTasteProfile()
    ]).then(function(results) {
      var collections = results[0] || {};
      var comments = results[1] || {};
      var tasteProfile = normalizeTasteProfile(results[2] || null);

      self.setData({
        'userInfo.collectCount': collections.total || 0,
        'userInfo.commentCount': comments.total || 0,
        tasteProfile: tasteProfile
      });
    }).catch(function(error) {
      console.warn('load stats failed:', error);
    });
  },

  onWechatLogin: function() {
    var self = this;
    var app = getApp();

    wx.showLoading({ title: '登录中...' });

    app.wxLogin().then(function() {
      wx.hideLoading();
      self.refreshProfile();
      wx.showToast({ title: '登录成功', icon: 'success' });
    }).catch(function(error) {
      wx.hideLoading();
      console.error('login failed:', error);
      wx.showToast({ title: '登录失败', icon: 'none' });
    });
  },

  onLogout: function() {
    var self = this;

    wx.showModal({
      title: '退出登录',
      content: '确定要退出当前账号吗？',
      success: function(res) {
        if (!res.confirm) {
          return;
        }

        var app = getApp();
        app.logout();
        auth.clearUserInfo();
        self.refreshProfile();
        wx.showToast({ title: '已退出', icon: 'success' });
      }
    });
  },

  switchTheme: function(e) {
    var targetTheme = e.currentTarget.dataset.theme;
    if (targetTheme !== 'light' && targetTheme !== 'dark') {
      return;
    }

    var app = getApp();
    if (app.setTheme) {
      app.setTheme(targetTheme);
    }

    this.syncTheme();
    wx.showToast({
      title: targetTheme === 'dark' ? '已切换深色' : '已切换明亮',
      icon: 'none'
    });
  },

  ensureLogin: function(actionText) {
    if (this.data.isLoggedIn) {
      return true;
    }

    wx.showToast({
      title: '请先登录',
      icon: 'none'
    });

    return false;
  },

  goToLucky: function() {
    wx.navigateTo({ url: '/pages/lucky/lucky' });
  },

  goToCollections: function() {
    if (!this.ensureLogin('查看收藏')) {
      return;
    }
    wx.navigateTo({ url: '/pages/collections/collections' });
  },

  goToComments: function() {
    if (!this.ensureLogin('查看评论')) {
      return;
    }
    wx.navigateTo({ url: '/pages/comments/comments' });
  },

  showAbout: function() {
    this.setData({ showAboutModal: true });
  },

  showAgreement: function() {
    wx.showModal({
      title: '用户协议',
      content: '请遵守平台规则合理使用本小程序。',
      showCancel: false,
      confirmText: '好的'
    });
  },

  showPrivacy: function() {
    wx.showModal({
      title: '隐私政策',
      content: '我们仅收集提供服务所需的最少信息。',
      showCancel: false,
      confirmText: '好的'
    });
  },

  hideModal: function() {
    this.setData({ showAboutModal: false });
  },

  stopPropagation: function() {},

  onPullDownRefresh: function() {
    this.refreshProfile();
    wx.stopPullDownRefresh();
  },

  onShareAppMessage: function() {
    return {
      title: '我的主页 - 食堂优选',
      path: '/pages/profile/profile',
      imageUrl: '/images/default-avatar.jpg'
    };
  }
});
