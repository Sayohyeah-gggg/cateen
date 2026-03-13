// pages/index/index.js
var api = require('../../utils/api');
var mockData = require('../../data/mock.js');

Page({
  data: {
    categories: [],
    currentCategory: 'all',
    foodList: [],
    loading: true,
    refreshing: false,
    loadingMore: false,
    noMore: false,
    page: 1,
    pageSize: 10,
    searchKeyword: '', // 搜索关键词
    searchTimer: null, // 搜索防抖定时器
    showChatModal: false, // AI聊天框显示状态
    chatMessages: [], // 聊天消息列表
    inputValue: '', // 输入框内容
    sessionId: '' // AI会话ID
  },

  onLoad: function() {
    // 确保用户已登录后再加载数据
    this.ensureLoginAndLoadData();
  },

  onShow: function() {
    // 页面显示时不自动刷新数据，避免覆盖当前分类的数据
    // 只在真正首次加载时才初始化数据
    if (this.data.categories.length === 0 && this.data.foodList.length === 0) {
      this.ensureLoginAndLoadData();
    } else {
      // 检查是否有搜索关键词，如果有则重新搜索
      if (this.data.searchKeyword && this.data.searchKeyword.trim()) {
        this.performSearch(this.data.searchKeyword);
      }
      
      // 延迟检查数据是否被清空
      var self = this;
      setTimeout(function() {
        // 如果数据被意外清空，重新加载
        if (self.data.foodList.length === 0 && self.data.categories.length > 0) {
          self.loadFoodList(true);
        }
      }, 1000);
    }
  },

  // 确保登录并加载数据
  ensureLoginAndLoadData: function() {
    var self = this;
    var app = getApp();
    
    // 如果用户未登录，先进行登录
    if (!app.globalData.isLoggedIn) {
      app.wxLogin().then(function() {
        // 用户已登录，加载真实数据
        self.loadCategories();
        self.loadFoodList(true); // 传递refresh=true，确保刷新数据
        
      }).catch(function(error) {
        console.error('登录失败:', error);
        // 登录失败时使用模拟数据
        self.loadCategories();
        self.loadFoodList(true); // 传递refresh=true，确保刷新数据
      });
    } else {
      // 用户已登录，加载真实数据
      this.loadCategories();
      this.loadFoodList(true); // 传递refresh=true，确保刷新数据
    }
  },


  // 处理搜索输入 - 使用防抖
  handleSearchInput: function(e) {
    var self = this;
    var searchText = e.detail.value.trim();
    
    console.log('搜索输入:', searchText);
    
    // 更新搜索关键词
    this.setData({
      searchKeyword: searchText
    });
    
    // 清除之前的定时器
    if (this.data.searchTimer) {
      clearTimeout(this.data.searchTimer);
    }
    
    // 设置防抖定时器（500ms）
    var timer = setTimeout(function() {
      self.performSearch(searchText);
    }, 500);
    
    this.setData({
      searchTimer: timer
    });
  },

  // 执行搜索
  performSearch: function(keyword) {
    var self = this;
    
    // 重置分页
    this.setData({
      page: 1,
      foodList: [],
      noMore: false
    });
    
    // 如果关键词为空，加载普通列表
    if (!keyword) {
      this.loadFoodList(true);
      return;
    }
    
    // 显示加载状态
    wx.showLoading({ title: '搜索中...' });
    
    // 构建搜索参数（搜索时不限制分类，搜索全部）
    var params = {
      page: 1,
      pageSize: this.data.pageSize,
      keyword: keyword
      // 注意：搜索时不传递category参数，这样可以在所有分类中搜索
    };
    
    // 调用API搜索
    api.foods.getList(params).then(function(response) {
      wx.hideLoading();
      
      console.log('搜索结果:', response);
      
      self.setData({
        foodList: response.list || [],
        noMore: !response.hasMore,
        page: 2
      });
      
      // 如果没有结果，显示提示
      if (response.list.length === 0) {
        wx.showToast({
          title: '未找到相关美食',
          icon: 'none'
        });
      }
      
    }).catch(function(error) {
      wx.hideLoading();
      console.error('搜索失败:', error);
      
      // 降级到本地搜索
      self.performLocalSearch(keyword);
    });
  },

  // 本地搜索（API失败时的降级方案）
  performLocalSearch: function(keyword) {
    var self = this;
    
    // API失败时，显示空结果
    this.setData({
      foodList: [],
      noMore: true
    });
    
    wx.showToast({
      title: '搜索服务暂时不可用',
      icon: 'none'
    });
  },

  // 加载分类数据
  loadCategories: function() {
    var self = this;
    
    api.foods.getCategories().then(function(categories) {
      // 处理分类数据，添加前端需要的字段
      var processedCategories = categories.map(function(category) {
        return {
          id: category.id,
          name: category.name,
          code: category.code || category.id, // 使用code字段，如果没有则使用id
          icon: category.icon || '🍽️', // 添加默认图标
          foodCount: category.food_count || category.foodCount || 0
        };
      });
      
      self.setData({ categories: processedCategories });
    }).catch(function(error) {
      console.error('API调用失败:', error);
      wx.showToast({
        title: '加载分类失败',
        icon: 'error'
      });
      // 不要清空已有的分类数据，保持原有数据
    });
  },

  // 加载美食列表
  loadFoodList: function(refresh) {
    var self = this;
    refresh = refresh || false;
    
    if (refresh) {
      this.setData({ 
        page: 1, 
        noMore: false,
        refreshing: true 
      });
    } else {
      this.setData({ loadingMore: true });
    }

    // 从 API 获取美食列表
    var params = {
      page: this.data.page,
      pageSize: this.data.pageSize,
      category: this.data.currentCategory !== 'all' ? this.data.currentCategory : undefined
    };
    
    api.foods.getList(params).then(function(foodListData) {
      self.handleFoodListData(foodListData, refresh);
    }).catch(function(apiError) {
      console.error('API调用失败，详细错误:', apiError);
      
      wx.showToast({
        title: '加载美食失败: ' + (apiError.message || '网络错误'),
        icon: 'error'
      });
      
      self.setData({
        loading: false,
        refreshing: false,
        loadingMore: false
        // 注意：API失败时不修改foodList，保持原有数据
      });
    });
  },

  // 处理美食列表数据
  handleFoodListData: function(foodListData, refresh) {
    try {
      if (!foodListData || !foodListData.list) {
        console.error('美食列表数据格式错误:', foodListData);
        wx.showToast({
          title: '数据格式错误',
          icon: 'error'
        });
        return;
      }
      
      // 处理后端返回的数据，转换字段格式
      var processedList = foodListData.list.map(function(food) {
        // 将蛇形命名转换为驼峰命名
        var processedFood = {};
        
        // 复制所有原始字段
        for (var key in food) {
          processedFood[key] = food[key];
        }
        
        // 处理图片字段：image_url (字符串) -> images (数组)
        if (food.image_url) {
          processedFood.images = [food.image_url];
        } else if (food.imageUrl) {
          processedFood.images = [food.imageUrl];
        } else if (!processedFood.images || processedFood.images.length === 0) {
          processedFood.images = ['/images/foods/gongbao-chicken.jpg']; // 默认图片
        }
        
        // 处理其他蛇形命名字段
        if (food.category_name !== undefined) {
          processedFood.category = food.category_name;
        }
        if (food.rating !== undefined) {
          processedFood.avgRating = food.rating;
        } else if (food.avg_rating !== undefined) {
          processedFood.avgRating = food.avg_rating;
        }
        if (food.rating_count !== undefined) {
          processedFood.ratingCount = food.rating_count;
        }
        if (food.is_collected !== undefined) {
          processedFood.isCollected = food.is_collected;
        }
        if (food.hot_comment !== undefined) {
          processedFood.topComment = food.hot_comment;
        }
        if (food.description !== undefined) {
          processedFood.description = food.description;
        }
        
        return processedFood;
      });
      
      var newFoodList = refresh ? processedList : this.data.foodList.concat(processedList);

      this.setData({
        foodList: newFoodList,
        noMore: !foodListData.hasMore,
        page: refresh ? 2 : this.data.page + 1,
        loading: false,
        refreshing: false,
        loadingMore: false
      });
    } catch (error) {
      console.error('加载美食列表失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      });
      this.setData({ 
        loading: false, 
        refreshing: false,
        loadingMore: false 
      });
    }
  },

  // 切换分类
  switchCategory: function(e) {
    var categoryCode = e.currentTarget.dataset.code;
    if (categoryCode === this.data.currentCategory) return;
    
    this.setData({
      currentCategory: categoryCode,
      page: 1,
      noMore: false
    });
    
    // 先清空列表，然后加载新数据
    this.setData({
      foodList: []
    });
    
    this.loadFoodList(true);
  },

  // 跳转搜索页面
  goToSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    });
  },

  // 跳转详情页
  goToDetail: function(e) {
    var foodId = e.detail.foodId;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 切换收藏状态
  toggleCollect: function(e) {
    var self = this;
    var foodId = e.detail.foodId;
    var isCollected = e.detail.isCollected;
    
    // 首先尝试使用 API 进行收藏操作
    var apiPromise;
    if (isCollected) {
      apiPromise = api.user.removeCollection(foodId);
    } else {
      apiPromise = api.user.addCollection({ foodId: foodId });
    }
    
    apiPromise.then(function() {
      // API 调用成功，更新本地数据
      self.updateLocalCollectionData(foodId, isCollected);
    }).catch(function(apiError) {
      console.warn('API调用失败，使用本地模拟:', apiError);
      // API 调用失败时，使用本地模拟
      setTimeout(function() {
        self.updateLocalCollectionData(foodId, isCollected);
      }, 300);
    });
  },

  // 更新本地收藏数据
  updateLocalCollectionData: function(foodId, isCollected) {
    try {
      var foodList = this.data.foodList.map(function(food) {
        if (food.id === foodId) {
          var newFood = {};
          for (var key in food) {
            newFood[key] = food[key];
          }
          newFood.isCollected = !isCollected;
          return newFood;
        }
        return food;
      });

      this.setData({ foodList: foodList });

      wx.showToast({
        title: isCollected ? '取消收藏' : '收藏成功',
        icon: 'success'
      });
    } catch (error) {
      console.error('收藏操作失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'error'
      });
    }
  },

  // 下拉刷新
  onRefresh: function() {
    this.loadFoodList(true);
  },

  // 加载更多
  loadMore: function() {
    if (this.data.loadingMore || this.data.noMore) return;
    this.loadFoodList();
  },

  // AI助手点击事件
  onAssistantTap: function(e) {
    console.log('AI助手被点击', e.detail);
    
    // 生成新的会话ID（如果还没有的话）
    var sessionId = this.data.sessionId;
    if (!sessionId) {
      sessionId = 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      this.setData({
        sessionId: sessionId
      });
    }
    
    // 打开AI聊天框
    this.setData({
      showChatModal: true
    });
    
    // 添加触觉反馈
    wx.vibrateShort({
      type: 'light'
    });
  },

  // 关闭AI聊天框
  onCloseChat: function() {
    this.setData({
      showChatModal: false
    });
  },

  // 隐藏AI助手
  onHideAssistant: function() {
    wx.showToast({
      title: 'AI助手已隐藏',
      icon: 'success'
    });
  },

  // 输入框内容变化
  onInputChange: function(e) {
    this.setData({
      inputValue: e.detail.value
    });
  },

  // 发送消息
  sendMessage: function() {
    const content = this.data.inputValue.trim();
    if (!content) return;

    // 添加用户消息
    const userMessage = {
      id: Date.now(),
      type: 'user',
      content: content,
      timestamp: Date.now()
    };

    var newMessages = this.data.chatMessages.slice();
    newMessages.push(userMessage);
    this.setData({
      chatMessages: newMessages,
      inputValue: ''
    });

    // 调用AI服务
    this.callAiService(content);
  },

  // 调用AI服务
  callAiService: function(userInput) {
    var self = this;
    var api = require('../../utils/api');
    
    // 调试：检查API模块是否正确加载
    console.log('API模块:', api);
    console.log('AI接口:', api.ai);
    if (!api.ai) {
      console.error('AI接口未找到，API模块结构:', Object.keys(api));
      return;
    }

    // 显示AI正在输入
    var aiTypingMessage = {
      id: Date.now() + 1,
      type: 'assistant',
      content: 'AI正在思考中...',
      timestamp: Date.now(),
      isTyping: true
    };

    var newMessages = this.data.chatMessages.slice();
    newMessages.push(aiTypingMessage);
    this.setData({
      chatMessages: newMessages
    });

    // 尝试调用AI接口，失败时使用模拟回复
    if (api.ai && api.ai.chat) {
      api.ai.chat(userInput, self.data.sessionId).then(function(response) {
        // 移除正在输入的消息
        var messages = [];
        for (var i = 0; i < self.data.chatMessages.length; i++) {
          if (!self.data.chatMessages[i].isTyping) {
            messages.push(self.data.chatMessages[i]);
          }
        }
        
        // 添加AI回复
        var aiMessage = {
          id: Date.now() + 2,
          type: 'assistant',
          content: response,
          timestamp: Date.now(),
          isTyping: false
        };

        var finalMessages = messages.slice();
        finalMessages.push(aiMessage);
        self.setData({
          chatMessages: finalMessages
        });
      }).catch(function(error) {
        console.error('AI服务调用失败:', error);
        self.showFallbackResponse(userInput);
      });
    } else {
      console.log('AI接口不可用，使用模拟回复');
      self.showFallbackResponse(userInput);
    }
  },

  // 显示备用回复
  showFallbackResponse: function(userInput) {
    var self = this;
    
    setTimeout(function() {
      // 移除正在输入的消息
      var messages = [];
      for (var i = 0; i < self.data.chatMessages.length; i++) {
        if (!self.data.chatMessages[i].isTyping) {
          messages.push(self.data.chatMessages[i]);
        }
      }
      
      // 根据用户输入生成智能回复
      var aiMessage = {
        id: Date.now() + 2,
        type: 'assistant',
        content: self.generateSmartResponse(userInput),
        timestamp: Date.now(),
        isTyping: false
      };

      var finalMessages = messages.slice();
      finalMessages.push(aiMessage);
      self.setData({
        chatMessages: finalMessages
      });
    }, 1500); // 1.5秒后显示回复
  },

  // 生成智能回复
  generateSmartResponse: function(userInput) {
    var input = userInput.toLowerCase();
    
    // 根据用户输入生成不同的回复
    if (input.indexOf('宫保鸡丁') !== -1 || input.indexOf('配料') !== -1 || input.indexOf('介绍') !== -1) {
      return '🍽️ **宫保鸡丁** 经典配料：\n\n' +
             '• 鸡胸肉 300g\n' +
             '• 花生米 50g\n' +
             '• 干辣椒 10个\n' +
             '• 花椒 1小把\n' +
             '• 葱白 2根\n' +
             '• 姜蒜 适量\n' +
             '• 生抽、老抽、料酒、糖、醋\n\n' +
             '💡 制作要点：鸡肉切丁腌制，花生米炸脆，辣椒花椒爆香，最后勾芡收汁！';
    } else if (input.indexOf('推荐') !== -1 || input.indexOf('美食') !== -1 || input.indexOf('好吃') !== -1) {
      return '🤖 为您推荐以下美食：\n\n' +
             '🍽️ **川菜系列**\n' +
             '• 宫保鸡丁 - 麻辣鲜香\n' +
             '• 麻婆豆腐 - 嫩滑爽口\n' +
             '• 水煮鱼 - 麻辣过瘾\n\n' +
             '🍽️ **粤菜系列**\n' +
             '• 白切鸡 - 清淡鲜美\n' +
             '• 叉烧包 - 香甜软糯\n' +
             '• 虾饺 - 晶莹剔透\n\n' +
             '💡 您有什么特殊口味偏好吗？';
    } else if (input.indexOf('辣') !== -1 || input.indexOf('麻辣') !== -1) {
      return '🌶️ **麻辣美食推荐**：\n\n' +
             '• 水煮鱼 - 麻辣鲜香\n' +
             '• 麻婆豆腐 - 嫩滑爽口\n' +
             '• 口水鸡 - 麻辣开胃\n' +
             '• 夫妻肺片 - 麻辣爽脆\n\n' +
             '💡 这些菜品都很有特色，您想了解哪一道的详细做法？';
    } else if (input.indexOf('清淡') !== -1 || input.indexOf('不辣') !== -1) {
      return '🥗 **清淡美食推荐**：\n\n' +
             '• 白切鸡 - 清淡鲜美\n' +
             '• 蒸蛋羹 - 嫩滑营养\n' +
             '• 清炒时蔬 - 健康美味\n' +
             '• 白粥配小菜 - 养胃佳品\n\n' +
             '💡 这些菜品都很适合清淡口味，营养又健康！';
    } else {
      return '🤖 您好！我是AI美食助手，很高兴为您服务！\n\n' +
             '根据您的输入"' + userInput + '"，我为您推荐以下美食：\n\n' +
             '🍽️ **经典推荐**\n' +
             '• 宫保鸡丁 - 川菜经典，麻辣鲜香\n' +
             '• 红烧肉 - 家常美味，肥瘦相间\n' +
             '• 小笼包 - 江南特色，皮薄馅大\n\n' +
             '💡 如果您有特殊需求或偏好，请告诉我，我会为您提供更精准的推荐！';
    }
  },

  // 调试按钮点击事件
  onDebugTap: function() {
    console.log('调试按钮被点击');
    wx.showToast({
      title: '调试按钮工作正常',
      icon: 'success'
    });
    
    // 直接打开AI聊天框
    this.setData({
      showChatModal: true
    });
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '美食展示评分小程序 - 发现美食，分享美好',
      path: '/pages/index/index',
      imageUrl: '/images/share-app.jpg'
    };
  }
});
