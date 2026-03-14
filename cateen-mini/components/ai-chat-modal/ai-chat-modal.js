// components/ai-chat-modal/ai-chat-modal.js
var api = require('../../utils/api');

Component({
  properties: {
    visible: { type: Boolean, value: false },
    title: { type: String, value: 'AI助手' }
  },

  data: {
    messages: [
      {
        id: 1,
        type: 'assistant',
        content: '你好，我是AI助手，可以向我提美食建议。',
        timestamp: '刚刚'
      }
    ],
    inputValue: '',
    isSending: false,
    isAiTyping: false,
    scrollToBottom: false
  },

  observers: {
    visible: function(v) {
      if (v) {
        this.scrollToBottom();
      }
    }
  },

  methods: {
    closeChat: function() {
      this.triggerEvent('close');
    },

    onInputChange: function(e) {
      this.setData({ inputValue: e.detail.value || '' });
    },

    sendMessage: function() {
      var content = (this.data.inputValue || '').trim();
      if (!content || this.data.isSending) {
        return;
      }

      var userMessage = {
        id: Date.now(),
        type: 'user',
        content: content,
        timestamp: this.formatTime()
      };

      this.setData({
        messages: this.data.messages.concat([userMessage]),
        inputValue: '',
        isSending: true,
        scrollToBottom: true,
        isAiTyping: true
      });

      this.callAiService(content);
    },

    callAiService: function(userInput) {
      var self = this;

      if (api.ai && api.ai.chat) {
        api.ai.chat(userInput).then(function(reply) {
          self.pushAssistantMessage(reply);
        }).catch(function() {
          self.pushAssistantMessage(self.generateAiResponse(userInput));
        });
      } else {
        setTimeout(function() {
          self.pushAssistantMessage(self.generateAiResponse(userInput));
        }, 600);
      }
    },

    pushAssistantMessage: function(content) {
      var msg = {
        id: Date.now() + 1,
        type: 'assistant',
        content: content,
        timestamp: this.formatTime()
      };

      this.setData({
        messages: this.data.messages.concat([msg]),
        isSending: false,
        isAiTyping: false,
        scrollToBottom: true
      });
    },

    generateAiResponse: function(input) {
      var text = (input || '').toLowerCase();
      if (text.indexOf('recommend') > -1 || text.indexOf('eat') > -1) {
        return '今天可以试试宫保鸡丁、红烧肉、西湖牛肉羹。';
      }
      if (text.indexOf('light') > -1) {
        return '清淡一些可以考虑蒸蛋、时蔬、菌菇汤。';
      }
      if (text.indexOf('spicy') > -1) {
        return '想吃辣的可以点麻婆豆腐、辣子鸡、水煮鱼。';
      }
      return '我可以给你提供美食推荐和简单做法说明。';
    },

    formatTime: function() {
      var d = new Date();
      var h = d.getHours();
      var m = d.getMinutes();
      return (h < 10 ? '0' + h : h) + ':' + (m < 10 ? '0' + m : m);
    },

    scrollToBottom: function() {
      this.setData({ scrollToBottom: true });
    },

    clearInput: function() {
      this.setData({ inputValue: '' });
    },

    onMessageLongPress: function(e) {
      var id = e.currentTarget.dataset.id;
      var message = this.data.messages.find(function(item) {
        return item.id === id;
      });

      if (!message || message.type !== 'assistant') {
        return;
      }

      wx.setClipboardData({ data: message.content || '' });
    },

    resendMessage: function(e) {
      var id = e.currentTarget.dataset.id;
      var message = this.data.messages.find(function(item) {
        return item.id === id;
      });

      if (!message || message.type !== 'user') {
        return;
      }

      this.setData({ inputValue: message.content || '' });
    }
  }
});
