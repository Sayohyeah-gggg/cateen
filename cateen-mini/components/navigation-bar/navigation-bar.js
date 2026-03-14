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
    theme: {
      type: String,
      value: 'light'
    }
  },

  data: {
    statusBarHeight: 0,
    navBarHeight: 44
  },

  lifetimes: {
    attached: function() {
      this.setNavBarInfo();
    }
  },

  methods: {
    setNavBarInfo: function() {
      var statusBarHeight = 20;
      var navBarHeight = 44;

      try {
        var windowInfo = wx.getWindowInfo();
        statusBarHeight = windowInfo.statusBarHeight || statusBarHeight;

        var menuButton = wx.getMenuButtonBoundingClientRect();
        if (menuButton && menuButton.height) {
          navBarHeight = (menuButton.top - statusBarHeight) * 2 + menuButton.height;
        }
      } catch (error) {
        navBarHeight = 44;
      }

      this.setData({
        statusBarHeight: statusBarHeight,
        navBarHeight: navBarHeight
      });
    },

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
