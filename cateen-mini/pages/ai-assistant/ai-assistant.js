// pages/ai-assistant/ai-assistant.js
var api = require('../../utils/api');

Page({
  data: {
    theme: 'light',
    themeClass: 'theme-light',
    chatMessages: [],
    inputValue: '',
    sessionId: '',
    isLoading: false,
    scrollToView: '',
    userAvatar: '/images/default-avatar.jpg',
    isTyping: false,
    typingInterval: null
  },

  onLoad: function() {
    var sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).slice(2, 10);
    this.setData({ sessionId: sessionId });

    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';
    this.setData({ theme: theme, themeClass: 'theme-' + theme });

    if (app.globalData.userInfo && app.globalData.userInfo.avatarUrl) {
      this.setData({ userAvatar: app.globalData.userInfo.avatarUrl });
    }
  },

  onShow: function() {
    this.syncCustomTabBar();
    this.syncTheme();
  },

  onUnload: function() {
    if (this.data.typingInterval) {
      clearInterval(this.data.typingInterval);
    }
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

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';
    this.setData({ theme: theme, themeClass: 'theme-' + theme });
  },

  onInputChange: function(e) {
    this.setData({ inputValue: e.detail.value || '' });
  },

  sendQuickQuestion: function(e) {
    var question = e.currentTarget.dataset.question || '';
    this.setData({ inputValue: question });
    this.sendMessage();
  },

  sendMessage: function() {
    var content = (this.data.inputValue || '').trim();
    if (!content || this.data.isLoading) {
      return;
    }

    var userMessage = {
      id: Date.now(),
      type: 'user',
      content: content,
      time: this.formatTime(new Date())
    };

    var list = this.data.chatMessages.slice();
    list.push(userMessage);

    this.setData({
      chatMessages: list,
      inputValue: '',
      isLoading: true,
      scrollToView: 'msg-' + userMessage.id
    });

    this.callAiService(content);
  },

  callAiService: function(userInput) {
    var self = this;

    if (api.ai && api.ai.chat) {
      api.ai.chat(userInput, self.data.sessionId).then(function(reply) {
        self.addAiMessage(reply);
      }).catch(function(error) {
        console.warn('ai api failed:', error);
        self.addAiMessage(self.generateSmartResponse(userInput));
      });
      return;
    }

    setTimeout(function() {
      self.addAiMessage(self.generateSmartResponse(userInput));
    }, 700);
  },

  addAiMessage: function(content) {
    var message = {
      id: Date.now() + 1,
      type: 'assistant',
      content: '',
      time: this.formatTime(new Date())
    };

    var list = this.data.chatMessages.slice();
    list.push(message);

    this.setData({
      chatMessages: list,
      isLoading: false,
      isTyping: true,
      scrollToView: 'msg-' + message.id
    });

    this.typeMessage(message.id, String(content || ''));
  },

  typeMessage: function(messageId, fullText) {
    var self = this;
    var index = 0;

    if (this.data.typingInterval) {
      clearInterval(this.data.typingInterval);
    }

    var timer = setInterval(function() {
      if (index >= fullText.length) {
        clearInterval(timer);
        self.setData({
          isTyping: false,
          typingInterval: null,
          scrollToView: 'msg-' + messageId
        });
        return;
      }

      index += 2;
      var part = fullText.slice(0, index);
      var list = self.data.chatMessages.slice();
      var i = list.findIndex(function(item) { return item.id === messageId; });

      if (i > -1) {
        list[i].content = part;
        self.setData({ chatMessages: list, scrollToView: 'msg-' + messageId });
      }
    }, 22);

    this.setData({ typingInterval: timer });
  },

  generateSmartResponse: function(userInput) {
    var text = String(userInput || '').toLowerCase();

    if (text.indexOf('eat') > -1 || text.indexOf('recommend') > -1) {
      return '可以试试宫保鸡丁、红烧肉和西湖牛肉羹。';
    }

    if (text.indexOf('cook') > -1 || text.indexOf('gongbao') > -1) {
      return '宫保鸡丁：先腌鸡丁，花生米炸香，最后大火快炒收汁。';
    }

    if (text.indexOf('light') > -1) {
      return '清淡口味可以试试蒸蛋、清炒时蔬、菌菇汤。';
    }

    if (text.indexOf('spicy') > -1 || text.indexOf('hot') > -1) {
      return '想吃辣的：麻婆豆腐、水煮鱼、辣子鸡都不错。';
    }

    return '我可以帮你做美食推荐、口味筛选和简单做法说明。';
  },

  formatTime: function(date) {
    var h = date.getHours();
    var m = date.getMinutes();
    var hh = h < 10 ? '0' + h : '' + h;
    var mm = m < 10 ? '0' + m : '' + m;
    return hh + ':' + mm;
  },

  onShareAppMessage: function() {
    return {
      title: '智能美食推荐助手',
      path: '/pages/ai-assistant/ai-assistant',
      imageUrl: '/images/foods/gongbao-chicken.jpg'
    };
  }
});
