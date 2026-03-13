// components/ai-chat-modal/ai-chat-modal.js
Component({
  properties: {
    // 是否显示聊天框
    visible: {
      type: Boolean,
      value: false
    },
    // 聊天标题
    title: {
      type: String,
      value: 'AI美食助手'
    }
  },

  data: {
    // 消息列表
    messages: [
      {
        id: 1,
        type: 'assistant',
        content: '您好！我是您的专属美食推荐助手 🤖\n\n我可以根据您的口味偏好、营养需求、用餐场景等，为您推荐最合适的美食。\n\n请告诉我您今天想吃什么，或者有什么特殊需求吗？',
        timestamp: Date.now(),
        isTyping: false
      }
    ],
    // 输入框内容
    inputValue: '',
    // 是否正在发送消息
    isSending: false,
    // 是否正在AI回复
    isAiTyping: false,
    // 滚动到底部的标识
    scrollToBottom: false
  },

  observers: {
    'visible': function(visible) {
      if (visible) {
        // 聊天框打开时，滚动到底部
        this.scrollToBottom();
      }
    }
  },

  methods: {
    // 关闭聊天框
    closeChat() {
      this.triggerEvent('close');
    },

    // 输入框内容变化
    onInputChange(e) {
      this.setData({
        inputValue: e.detail.value
      });
    },

    // 发送消息
    sendMessage() {
      const content = this.data.inputValue.trim();
      if (!content || this.data.isSending) return;

      // 添加用户消息
      const userMessage = {
        id: Date.now(),
        type: 'user',
        content: content,
        timestamp: Date.now(),
        isTyping: false
      };

      this.setData({
        messages: [...this.data.messages, userMessage],
        inputValue: '',
        isSending: true,
        scrollToBottom: true
      });

      // 调用真实AI服务
      this.callAiService(content);
    },

    // 调用真实AI服务
    callAiService(userInput) {
      const self = this;
      const api = require('../../utils/api');
      
      // 显示AI正在输入状态
      setTimeout(() => {
        self.setData({
          isAiTyping: true,
          scrollToBottom: true
        });
      }, 500);

      // 调用AI聊天API
      api.ai.chat(userInput).then(function(response) {
        const aiMessage = {
          id: Date.now() + 1,
          type: 'assistant',
          content: response,
          timestamp: Date.now(),
          isTyping: false
        };

        self.setData({
          messages: [...self.data.messages, aiMessage],
          isSending: false,
          isAiTyping: false,
          scrollToBottom: true
        });
      }).catch(function(error) {
        console.error('AI服务调用失败:', error);
        
        // 降级到模拟回复
        const aiResponse = self.generateAiResponse(userInput);
        const aiMessage = {
          id: Date.now() + 1,
          type: 'assistant',
          content: aiResponse,
          timestamp: Date.now(),
          isTyping: false
        };

        self.setData({
          messages: [...self.data.messages, aiMessage],
          isSending: false,
          isAiTyping: false,
          scrollToBottom: true
        });
      });
    },

    // 生成AI回复（模拟）
    generateAiResponse(userInput) {
      const responses = [
        '根据您的需求，我为您推荐几道美食：\n\n🍜 **红烧肉** - 经典家常菜，肥瘦相间，入口即化\n🥟 **小笼包** - 皮薄馅大，汤汁丰富\n🍲 **西湖牛肉羹** - 清淡营养，适合养生\n\n您对哪道菜比较感兴趣呢？',
        '我理解您的需求！让我为您推荐一些适合的美食：\n\n根据您的描述，我建议您可以尝试：\n\n1. **宫保鸡丁** - 麻辣鲜香，下饭神器\n2. **麻婆豆腐** - 嫩滑爽口，素食首选\n3. **扬州炒饭** - 营养均衡，老少皆宜\n\n这些菜品都很有特色，您想了解哪一道的详细信息吗？',
        '好的！我为您精心挑选了几道美食：\n\n🌟 **推荐理由**：\n- 营养搭配合理\n- 口味层次丰富\n- 制作难度适中\n\n🍽️ **推荐菜品**：\n1. 宫保鸡丁 - 川菜经典\n2. 红烧肉 - 家常美味\n3. 小笼包 - 江南特色\n\n您还有什么特殊要求吗？比如口味偏好、营养需求等？',
        '感谢您的信任！作为您的专属美食助手，我会根据您的喜好为您推荐最合适的美食。\n\n💡 **小贴士**：\n- 可以告诉我您的口味偏好（辣、甜、咸、酸）\n- 告诉我用餐场景（早餐、午餐、晚餐、夜宵）\n- 告诉我营养需求（高蛋白、低脂、素食等）\n\n这样我就能为您推荐更精准的美食了！'
      ];

      // 根据用户输入的关键词选择回复
      const input = userInput.toLowerCase();
      if (input.includes('推荐') || input.includes('想吃')) {
        return responses[0];
      } else if (input.includes('川菜') || input.includes('辣')) {
        return responses[1];
      } else if (input.includes('营养') || input.includes('健康')) {
        return responses[2];
      } else {
        return responses[3];
      }
    },

    // 滚动到底部
    scrollToBottom() {
      this.setData({
        scrollToBottom: true
      });
    },

    // 清空输入框
    clearInput() {
      this.setData({
        inputValue: ''
      });
    },

    // 长按消息复制
    onMessageLongPress(e) {
      const messageId = e.currentTarget.dataset.id;
      const message = this.data.messages.find(msg => msg.id === messageId);
      
      if (message && message.type === 'assistant') {
        wx.setClipboardData({
          data: message.content,
          success: () => {
            wx.showToast({
              title: '已复制到剪贴板',
              icon: 'success'
            });
          }
        });
      }
    },

    // 重新发送消息
    resendMessage(e) {
      const messageId = e.currentTarget.dataset.id;
      const message = this.data.messages.find(msg => msg.id === messageId);
      
      if (message && message.type === 'user') {
        this.setData({
          inputValue: message.content
        });
      }
    }
  }
});
