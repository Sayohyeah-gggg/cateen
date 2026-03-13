// pages/ai-assistant/ai-assistant.js
var api = require('../../utils/api');

Page({
  data: {
    chatMessages: [],
    inputValue: '',
    sessionId: '',
    isLoading: false,
    scrollToView: '',
    userAvatar: '/images/default-avatar.jpg',
    isTyping: false, // AI是否正在打字
    currentTypingMessageId: null, // 当前正在打字的消息ID
    typingInterval: null // 打字定时器
  },

  onLoad: function() {
    // 生成会话ID
    var sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    this.setData({ sessionId: sessionId });
    
    // 获取用户头像
    var app = getApp();
    if (app.globalData.userInfo && app.globalData.userInfo.avatarUrl) {
      this.setData({
        userAvatar: app.globalData.userInfo.avatarUrl
      });
    }
  },

  // 输入框内容变化
  onInputChange: function(e) {
    this.setData({
      inputValue: e.detail.value
    });
  },

  // 发送快捷问题
  sendQuickQuestion: function(e) {
    var question = e.currentTarget.dataset.question;
    this.setData({ inputValue: question });
    this.sendMessage();
  },

  // 发送消息
  sendMessage: function() {
    var content = this.data.inputValue.trim();
    if (!content || this.data.isLoading) return;

    // 添加用户消息
    var userMessage = {
      id: Date.now(),
      type: 'user',
      content: content,
      time: this.formatTime(new Date())
    };

    var newMessages = this.data.chatMessages.slice();
    newMessages.push(userMessage);
    
    this.setData({
      chatMessages: newMessages,
      inputValue: '',
      isLoading: true,
      scrollToView: 'msg-' + userMessage.id
    });

    // 调用AI服务
    this.callAiService(content);
  },

  // 调用AI服务
  callAiService: function(userInput) {
    var self = this;
    
    // 尝试调用AI接口
    if (api.ai && api.ai.chat) {
      api.ai.chat(userInput, self.data.sessionId)
        .then(function(response) {
          self.addAiMessage(response);
        })
        .catch(function(error) {
          console.error('AI服务调用失败:', error);
          
          // 如果是401错误(Token过期),使用本地智能回复
          if (error && (error.code === 401 || error.statusCode === 401)) {
            console.log('Token过期,使用本地智能回复');
            self.addAiMessage(self.generateSmartResponse(userInput));
          } else {
            // 其他错误也使用本地回复
            self.addAiMessage(self.generateSmartResponse(userInput));
          }
        });
    } else {
      // AI接口不可用，使用模拟回复
      setTimeout(function() {
        self.addAiMessage(self.generateSmartResponse(userInput));
      }, 1500);
    }
  },

  // 添加AI回复消息(支持流式显示)
  addAiMessage: function(content) {
    var self = this;
    
    // 创建AI消息对象
    var aiMessage = {
      id: Date.now() + 1,
      type: 'assistant',
      content: '', // 初始为空,逐字显示
      fullContent: content, // 完整内容
      time: this.formatTime(new Date())
    };

    var newMessages = this.data.chatMessages.slice();
    newMessages.push(aiMessage);
    
    this.setData({
      chatMessages: newMessages,
      isLoading: false,
      isTyping: true,
      currentTypingMessageId: aiMessage.id,
      scrollToView: 'msg-' + aiMessage.id
    });

    // 开始逐字显示
    this.typeMessage(aiMessage.id, content);
  },

  // 逐字显示消息
  typeMessage: function(messageId, fullContent) {
    var self = this;
    var currentIndex = 0;
    var typingSpeed = 30; // 每个字符显示间隔(毫秒)
    var scrollCounter = 0; // 滚动计数器
    
    // 清除之前的定时器
    if (self.data.typingInterval) {
      clearInterval(self.data.typingInterval);
    }
    
    var typingInterval = setInterval(function() {
      if (currentIndex < fullContent.length) {
        // 每次显示1-3个字符(模拟真实打字速度)
        var charsToAdd = Math.min(Math.floor(Math.random() * 3) + 1, fullContent.length - currentIndex);
        currentIndex += charsToAdd;
        scrollCounter++;
        
        // 更新消息内容
        var messages = self.data.chatMessages.slice();
        var messageIndex = messages.findIndex(function(msg) { return msg.id === messageId; });
        
        if (messageIndex !== -1) {
          messages[messageIndex].content = fullContent.substring(0, currentIndex);
          
          // 每5个字符滚动一次,减少性能消耗
          var shouldScroll = scrollCounter % 5 === 0 || currentIndex >= fullContent.length;
          
          self.setData({
            chatMessages: messages,
            scrollToView: shouldScroll ? 'msg-' + messageId : self.data.scrollToView
          });
        }
      } else {
        // 打字完成
        clearInterval(typingInterval);
        self.setData({
          isTyping: false,
          currentTypingMessageId: null,
          typingInterval: null,
          scrollToView: 'msg-' + messageId // 最后滚动到底部
        });
      }
    }, typingSpeed);
    
    // 保存定时器引用
    self.setData({
      typingInterval: typingInterval
    });
  },

  // 生成智能回复
  generateSmartResponse: function(userInput) {
    var input = userInput.toLowerCase();
    
    // 根据用户输入生成不同的回复
    if (input.indexOf('宫保鸡丁') !== -1 || input.indexOf('配料') !== -1 || input.indexOf('怎么做') !== -1) {
      return '🍽️ 宫保鸡丁经典做法：\n\n' +
             '【主要食材】\n' +
             '• 鸡胸肉 300g\n' +
             '• 花生米 50g\n' +
             '• 干辣椒 10个\n' +
             '• 花椒 1小把\n' +
             '• 葱姜蒜 适量\n\n' +
             '【调料】\n' +
             '• 生抽、老抽、料酒\n' +
             '• 糖、醋、盐\n' +
             '• 淀粉、香油\n\n' +
             '💡 制作要点：\n' +
             '1. 鸡肉切丁腌制15分钟\n' +
             '2. 花生米炸至金黄酥脆\n' +
             '3. 辣椒花椒爆香\n' +
             '4. 快速翻炒，最后勾芡收汁';
    } else if (input.indexOf('推荐') !== -1 || input.indexOf('川菜') !== -1) {
      return '🌶️ 为您推荐经典川菜：\n\n' +
             '1. 宫保鸡丁 - 麻辣鲜香，酸甜适口\n' +
             '2. 麻婆豆腐 - 麻辣烫嫩，色泽红亮\n' +
             '3. 水煮鱼 - 麻辣过瘾，鱼肉鲜嫩\n' +
             '4. 回锅肉 - 肥而不腻，香气扑鼻\n' +
             '5. 鱼香肉丝 - 酸甜咸辣，味道丰富\n\n' +
             '💡 川菜特点：麻辣鲜香，注重调味，善用辣椒和花椒。\n\n' +
             '您想了解哪道菜的详细做法？';
    } else if (input.indexOf('清淡') !== -1 || input.indexOf('不辣') !== -1) {
      return '🥗 清淡美食推荐：\n\n' +
             '1. 清蒸鲈鱼 - 鲜嫩清香，原汁原味\n' +
             '2. 白灼虾 - 保持鲜味，营养丰富\n' +
             '3. 蒸蛋羹 - 嫩滑细腻，易于消化\n' +
             '4. 清炒时蔬 - 清爽健康，色泽鲜艳\n' +
             '5. 小米粥 - 养胃暖心，营养温和\n\n' +
             '💡 清淡饮食建议：\n' +
             '• 少油少盐，保持食材原味\n' +
             '• 多用蒸、煮、炖的烹饪方式\n' +
             '• 搭配新鲜蔬菜，营养均衡';
    } else if (input.indexOf('今天') !== -1 || input.indexOf('吃什么') !== -1 || input.indexOf('吃啥') !== -1) {
      return '🍽️ 今日美食推荐：\n\n' +
             '根据季节和营养搭配，为您推荐：\n\n' +
             '【午餐建议】\n' +
             '• 主食：扬州炒饭\n' +
             '• 主菜：红烧肉\n' +
             '• 汤品：西湖牛肉羹\n\n' +
             '【晚餐建议】\n' +
             '• 主食：小笼包\n' +
             '• 主菜：糖醋排骨\n' +
             '• 配菜：清炒时蔬\n\n' +
             '💡 营养均衡，荤素搭配，祝您用餐愉快！';
    } else {
      return '🤖 您好！我是AI美食助手。\n\n' +
             '我可以帮您：\n' +
             '• 推荐各类美食\n' +
             '• 提供烹饪教程\n' +
             '• 解答美食相关问题\n' +
             '• 根据口味推荐菜品\n\n' +
             '您可以问我：\n' +
             '• "推荐一些川菜"\n' +
             '• "宫保鸡丁怎么做"\n' +
             '• "有什么清淡的美食"\n' +
             '• "今天吃什么好"\n\n' +
             '请告诉我您想了解什么？';
    }
  },

  // 格式化时间
  formatTime: function(date) {
    var hour = date.getHours();
    var minute = date.getMinutes();
    return (hour < 10 ? '0' + hour : hour) + ':' + (minute < 10 ? '0' + minute : minute);
  },

  // 页面卸载时清理定时器
  onUnload: function() {
    if (this.data.typingInterval) {
      clearInterval(this.data.typingInterval);
    }
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: 'AI美食助手 - 您的专属美食顾问',
      path: '/pages/ai-assistant/ai-assistant',
      imageUrl: '/images/foods/gongbao-chicken.jpg'
    };
  }
});
