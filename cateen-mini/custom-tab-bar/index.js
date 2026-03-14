// custom-tab-bar/index.js
Component({
  data: {
    selected: 0,
    theme: 'light',
    themeClass: 'theme-light',
    list: [
      {
        pagePath: '/pages/index/index',
        text: '首页',
        icon: '/styles/icon/homePage.svg'
      },
      {
        pagePath: '/pages/ranking/ranking',
        text: '分享',
        icon: '/styles/icon/share.svg'
      },
      {
        pagePath: '/pages/ai-assistant/ai-assistant',
        text: 'AI',
        icon: '/styles/icon/AI.svg'
      },
      {
        pagePath: '/pages/profile/profile',
        text: '我的',
        icon: '/styles/icon/myHome.svg'
      }
    ]
  },

  lifetimes: {
    attached: function() {
      this.syncTheme();
      this.updateSelected();
      this.bindThemeListener();
    },
    detached: function() {
      this.unbindThemeListener();
    }
  },

  methods: {
    bindThemeListener: function() {
      var app = getApp();
      if (!app || !app.subscribeThemeChange) {
        return;
      }

      if (!this.themeListener) {
        this.themeListener = this.onThemeChanged.bind(this);
      }

      app.subscribeThemeChange(this.themeListener);
    },

    unbindThemeListener: function() {
      var app = getApp();
      if (!app || !app.unsubscribeThemeChange || !this.themeListener) {
        return;
      }

      app.unsubscribeThemeChange(this.themeListener);
    },

    onThemeChanged: function(theme) {
      this.setData({
        theme: theme,
        themeClass: 'theme-' + theme
      });
    },

    syncTheme: function() {
      var app = getApp();
      var theme = app && app.getCurrentTheme ? app.getCurrentTheme() : 'light';

      this.setData({
        theme: theme,
        themeClass: 'theme-' + theme
      });
    },

    switchTab: function(e) {
      var path = e.currentTarget.dataset.path;
      var index = Number(e.currentTarget.dataset.index || 0);

      this.setData({ selected: index });

      wx.switchTab({
        url: path
      });
    },

    updateSelected: function() {
      this.syncTheme();

      var pages = getCurrentPages();
      if (!pages || !pages.length) {
        return;
      }

      var current = pages[pages.length - 1];
      var route = '/' + current.route;
      var index = this.data.list.findIndex(function(item) {
        return item.pagePath === route;
      });

      if (index > -1) {
        this.setData({ selected: index });
      }
    }
  }
});
