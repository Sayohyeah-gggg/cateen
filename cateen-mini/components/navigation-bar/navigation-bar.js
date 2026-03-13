// components/navigation-bar/navigation-bar.js
Component({
  properties: {
    title: {
      type: String,
      value: ''
    },
    showBack: {
      type: Boolean,
      value: false
    },
    bgColor: {
      type: String,
      value: '#FF6B35'
    },
    textColor: {
      type: String,
      value: '#ffffff'
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 0
  },

  lifetimes: {
    attached: function() {
      this.setNavBarInfo();
    }
  },

  methods: {
    // 设置导航栏信息
    setNavBarInfo: function() {
      var windowInfo = wx.getWindowInfo();
      var statusBarHeight = windowInfo.statusBarHeight;
      
      // 获取胶囊按钮信息
      var menuButton = wx.getMenuButtonBoundingClientRect();
      
      // 计算导航栏高度
      var navBarHeight = (menuButton.top - statusBarHeight) * 2 + menuButton.height;
      
      this.setData({
        statusBarHeight: statusBarHeight,
        navBarHeight: navBarHeight
      });
    },

    // 返回上一页
    goBack: function() {
      var pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    }
  }
});