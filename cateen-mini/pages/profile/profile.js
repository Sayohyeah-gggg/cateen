// pages/profile/profile.js
var auth = require('../../utils/auth');
var api = require('../../utils/api');

Page({
  data: {
    isLoggedIn: false,
    userInfo: {
      avatarUrl: '',
      nickName: '',
      collectCount: 0,
      commentCount: 0
    },
    recentCollections: [],
    recentComments: [],
    showAboutModal: false
  },

  onLoad: function() {
    this.loadUserInfo();
    this.loadRecentData();
  },

  onShow: function() {
    // 页面显示时刷新数据
    console.log('个人中心页面显示，当前登录状态:', this.data.isLoggedIn);
    this.loadUserInfo();
    this.loadRecentData();
  },

  // 加载用户信息
  loadUserInfo: function() {
    var self = this;
    try {
      // 使用auth工具检查登录状态
      var isLoggedIn = auth.isLoggedIn();
      console.log('auth.isLoggedIn() 返回:', isLoggedIn);
      this.setData({ isLoggedIn: isLoggedIn });
      console.log('页面登录状态已更新为:', isLoggedIn);

      if (isLoggedIn) {
        // 获取用户信息
        var userInfo = auth.getCurrentUser();
        if (userInfo) {
          // 从API获取用户统计数据
          try {
            Promise.all([
              api.user.getCollections({ page: 1, pageSize: 1 }),
              api.user.getComments({ page: 1, pageSize: 1 })
            ]).then(function(results) {
              var collectionsResult = results[0];
              var commentsResult = results[1];
              
              var userStats = {
                collectCount: collectionsResult.total || 0,
                commentCount: commentsResult.total || 0
              };
              
              self.setData({ 
                userInfo: Object.assign({}, userInfo, userStats)
              });
            }).catch(function(error) {
              console.error('获取用户统计数据失败:', error);
              // 如果获取统计数据失败，使用默认值
              self.setData({ 
                userInfo: Object.assign({}, userInfo, {
                  collectCount: 0,
                  commentCount: 0
                })
              });
            });
          } catch (error) {
            console.error('获取用户统计数据失败:', error);
            // 如果获取统计数据失败，使用默认值
            this.setData({ 
              userInfo: Object.assign({}, userInfo, {
                collectCount: 0,
                commentCount: 0
              })
            });
          }
        }
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    }
  },

  // 微信登录
  onWechatLogin: function() {
    var self = this;
    var app = getApp();
    
    wx.showLoading({ title: '登录中...' });
    
    // 使用全局的微信登录方法
    app.wxLogin()
      .then(function(loginData) {
        console.log('登录成功，获取到的数据:', loginData);
        
        // 从全局数据获取用户信息
        var userInfo = app.globalData.userInfo;
        console.log('全局用户信息:', userInfo);
        
        // 更新页面数据
        self.setData({
          isLoggedIn: true,
          userInfo: {
            nickName: userInfo ? userInfo.nickName : '美食爱好者',
            avatarUrl: userInfo ? userInfo.avatarUrl : '/images/default-avatar.jpg',
            collectCount: 0,
            commentCount: 0
          }
        });
        
        // 延迟500ms后重新加载相关数据，确保token已生效
        setTimeout(function() {
          self.loadUserInfo();
          self.loadRecentData();
        }, 500);
        
        wx.hideLoading();
        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });
        
      })
      .catch(function(error) {
        wx.hideLoading();
        console.error('登录失败:', error);
        wx.showToast({
          title: '登录失败',
          icon: 'error'
        });
      });
  },

  // 退出登录
  onLogout: function() {
    var self = this;
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: function(res) {
        if (res.confirm) {
          var app = getApp();
          
          // 使用全局的退出登录方法
          app.logout();
          
          // 清除本地存储的登录状态
          var auth = require('../../utils/auth');
          auth.clearUserInfo();
          
          self.setData({
            isLoggedIn: false,
            userInfo: {
              avatarUrl: '',
              nickName: '',
              collectCount: 0,
              commentCount: 0
            },
            recentCollections: [],
            recentComments: []
          });
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          });
        }
      }
    });
  },

  // 加载最近数据
  loadRecentData: function() {
    var self = this;
    if (!this.data.isLoggedIn) {
      return;
    }
    
    try {
      Promise.all([
        api.user.getCollections({ page: 1, pageSize: 5 }),
        api.user.getComments({ page: 1, pageSize: 3 })
      ]).then(function(results) {
        var collectionsResult = results[0];
        var commentsResult = results[1];
        
        var recentCollections = collectionsResult.list || [];
        var recentComments = commentsResult.list || [];

        self.setData({
          recentCollections: recentCollections,
          recentComments: recentComments
        });
      }).catch(function(error) {
        console.error('加载最近数据失败:', error);
      });
    } catch (error) {
      console.error('加载最近数据失败:', error);
    }
  },


  // 跳转到幸运转盘
  goToLucky: function() {
    wx.navigateTo({
      url: '/pages/lucky/lucky'
    });
  },

  // 跳转到收藏列表
  goToCollections: function() {
    if (!this.data.isLoggedIn) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: '/pages/collections/collections'
    });
  },

  // 跳转到评论列表  
  goToComments: function() {
    if (!this.data.isLoggedIn) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: '/pages/comments/comments'
    });
  },

  // 跳转到详情页
  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 显示关于我们
  showAbout: function() {
    this.setData({ showAboutModal: true });
  },

  // 显示用户协议
  showAgreement: function() {
    wx.showModal({
      title: '用户协议',
      content: '这里是用户协议的内容...\n\n1. 用户在使用本应用时应遵守相关法律法规\n2. 禁止发布违法违规内容\n3. 尊重他人隐私和权益\n4. 如有问题请联系客服',
      showCancel: false,
      confirmText: '我知道了'
    });
  },

  // 显示隐私政策
  showPrivacy: function() {
    wx.showModal({
      title: '隐私政策',
      content: '这里是隐私政策的内容...\n\n1. 我们重视您的隐私保护\n2. 仅收集必要的用户信息\n3. 不会向第三方泄露您的个人信息\n4. 您可以随时删除个人数据',
      showCancel: false,
      confirmText: '我知道了'
    });
  },

  // 隐藏弹窗
  hideModal: function() {
    this.setData({ showAboutModal: false });
  },

  // 阻止事件冒泡
  stopPropagation: function() {
    // 阻止点击弹窗内容时关闭弹窗
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    var self = this;
    Promise.all([
      this.loadUserInfo(),
      this.loadRecentData()
    ]).then(function() {
      wx.stopPullDownRefresh();
    });
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '美食展示评分小程序 - 发现美食，分享美好',
      path: '/pages/index/index',
      imageUrl: '/images/foods/yuxiang-rousi.jpg'
    };
  }
});