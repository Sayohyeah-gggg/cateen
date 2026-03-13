// pages/lucky/lucky.js
var api = require('../../utils/api');
var mockData = require('../../data/mock.js');

Page({
  data: {
    showBack: false, // 是否显示返回按钮
    wheelFoods: [],
    wheelRotation: 0,
    spinning: false,
    spinDuration: 3,
    showResult: false,
    spinResult: null,
    todayRecommendations: [],
    luckyHistory: [],
    wheelBackground: '',
    // 标签距离圆心的半径（rpx）
    labelRadius: 200,
    // 自定义转盘相关数据
    showCustomModal: false,
    customCount: 0,
    availableFoods: [],
    selectedFoods: [],
    // 预定义的颜色数组，支持更多颜色
    colorPalette: [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD',
      '#FF9FF3', '#54A0FF', '#5F27CD', '#00D2D3', '#FF9F43', '#10AC84',
      '#EE5A24', '#0984E3', '#6C5CE7', '#A29BFE', '#FD79A8', '#FDCB6E'
    ]
  },

  onLoad: function() {
    // 判断是否需要显示返回按钮
    var pages = getCurrentPages();
    this.setData({
      showBack: pages.length > 1
    });
    
    this.loadWheelFoods();
    this.loadTodayRecommendations();
    this.loadLuckyHistory();
  },

  onShow: function() {
    // 页面显示时刷新推荐数据
    this.loadTodayRecommendations();
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    var self = this;
    this.refreshFoods().then(function() {
      wx.stopPullDownRefresh();
    });
  },

  // 加载转盘美食
  loadWheelFoods: function() {
    var self = this;
    try {
      // 首先尝试从 API 获取转盘美食
      var params = {
        page: 1,
        limit: 6, // 默认6个美食用于转盘
        sortBy: 'rating'
      };
      
      api.foods.getList(params).then(function(response) {
        console.log('API获取转盘美食成功:', response);
        
        // 处理API返回的数据
        var luckyFoods = response.list || [];
        
        // 如果API返回的数据不足6个，用模拟数据补充
        if (luckyFoods.length < 6) {
          var mockFoods = mockData.luckyFoods.slice(0, 6 - luckyFoods.length);
          luckyFoods = luckyFoods.concat(mockFoods);
        }
        
        self.processWheelFoods(luckyFoods);
      }).catch(function(apiError) {
        console.warn('API调用失败，使用模拟数据:', apiError);
        // API 调用失败时，使用模拟数据
        var luckyFoods = mockData.luckyFoods;
        self.processWheelFoods(luckyFoods);
      });
    } catch (error) {
      console.error('加载转盘美食失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      });
    }
  },

  // 处理转盘美食数据
  processWheelFoods: function(luckyFoods) {
    var self = this;
    var foodCount = luckyFoods.length;
    
    if (foodCount === 0) {
      this.setData({ wheelFoods: [], wheelBackground: '' });
      return;
    }
    
    // 计算每个扇形的角度：360度 / 菜品数量
    var anglePerItem = 360 / foodCount;
    
    // 生成动态的conic-gradient背景
    var gradientStops = [];
    var labelRadiusPercent = 38; // 标签到圆心的半径（占容器的百分比）
    var wheelFoods = luckyFoods.map(function(food, index) {
      var startAngle = index * anglePerItem;
      var endAngle = (index + 1) * anglePerItem;
      var centerAngle = startAngle + (anglePerItem / 2); // 扇形中心角度
      var color = self.data.colorPalette[index % self.data.colorPalette.length];
      
      // 添加到渐变背景
      gradientStops.push(color + ' ' + startAngle + 'deg ' + endAngle + 'deg');
      
      // 计算标签位置（百分比坐标）
      var rad = (centerAngle * Math.PI) / 180;
      var labelX = 50 + labelRadiusPercent * Math.cos(rad);
      var labelY = 50 + labelRadiusPercent * Math.sin(rad);
      
      return Object.assign({}, food, {
        angle: startAngle,
        color: color,
        startAngle: startAngle,
        endAngle: endAngle,
        labelAngle: centerAngle,
        labelX: labelX,
        labelY: labelY
      });
    });
    
    // 生成完整的conic-gradient背景
    var wheelBackground = 'conic-gradient(' + gradientStops.join(', ') + ')';
    
    this.setData({ 
      wheelFoods: wheelFoods,
      wheelBackground: wheelBackground
    });
  },

  // 刷新美食
  refreshFoods: function() {
    var self = this;
    try {
      wx.showLoading({ title: '刷新中...' });
      
      // 模拟网络请求
      new Promise(function(resolve) { setTimeout(resolve, 1000); }).then(function() {
        // 重新加载数据
        self.loadWheelFoods();
        
        wx.hideLoading();
        wx.showToast({
          title: '刷新成功',
          icon: 'success'
        });
      }).catch(function(error) {
        wx.hideLoading();
        console.error('刷新失败:', error);
        wx.showToast({
          title: '刷新失败',
          icon: 'error'
        });
      });
    } catch (error) {
      wx.hideLoading();
      console.error('刷新失败:', error);
      wx.showToast({
        title: '刷新失败',
        icon: 'error'
      });
    }
  },

  // 开始转动
  startSpin: function() {
    var self = this;
    if (this.data.spinning) return;

    var wheelFoods = this.data.wheelFoods;
    if (wheelFoods.length === 0) {
      wx.showToast({
        title: '请先加载美食',
        icon: 'none'
      });
      return;
    }

    // 随机选择一个结果
    var randomIndex = Math.floor(Math.random() * wheelFoods.length);
    var selectedFood = wheelFoods[randomIndex];
    
    // 计算转动角度 - 使用动态计算
    var anglePerItem = 360 / wheelFoods.length;
    var targetAngle = selectedFood.startAngle + (anglePerItem / 2); // 指向扇形中心
    
    // 添加多圈旋转 + 目标角度
    var spins = 5; // 转5圈
    var finalRotation = this.data.wheelRotation + (spins * 360) + (360 - targetAngle);

    this.setData({
      spinning: true,
      wheelRotation: finalRotation,
      spinResult: selectedFood
    });

    // 转动完成后显示结果
    setTimeout(function() {
      self.setData({
        spinning: false,
        showResult: true
      });
      
      // 添加到历史记录
      self.addToHistory(selectedFood);
      
      // 震动反馈
      wx.vibrateShort();
    }, this.data.spinDuration * 1000);
  },

  // 添加到历史记录
  addToHistory: function(food) {
    var history = this.data.luckyHistory.slice(); // 复制数组
    var historyItem = Object.assign({}, food, {
      spinTime: new Date().toLocaleString()
    });
    
    // 添加到列表开头，最多保留10条
    history.unshift(historyItem);
    if (history.length > 10) {
      history.pop();
    }
    
    this.setData({ luckyHistory: history });
    
    // 保存到本地存储
    wx.setStorageSync('luckyHistory', history);
  },

  // 加载今日推荐
  loadTodayRecommendations: function() {
    try {
      // 从mock数据获取今日推荐
      var mockRecommendations = mockData.todayRecommendations;

      this.setData({ todayRecommendations: mockRecommendations });
    } catch (error) {
      console.error('加载今日推荐失败:', error);
    }
  },

  // 加载抽奖历史
  loadLuckyHistory: function() {
    try {
      var history = wx.getStorageSync('luckyHistory') || [];
      this.setData({ luckyHistory: history });
    } catch (error) {
      console.error('加载历史记录失败:', error);
    }
  },

  // 隐藏结果弹窗
  hideResult: function() {
    this.setData({ showResult: false });
  },

  // 阻止事件冒泡
  stopPropagation: function() {
    // 阻止点击弹窗内容时关闭弹窗
  },

  // 查看详情
  viewDetail: function() {
    if (this.data.spinResult) {
      wx.navigateTo({
        url: '/pages/detail/detail?id=' + this.data.spinResult.id
      });
      this.hideResult();
    }
  },

  // 跳转到详情页
  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 显示自定义转盘弹窗
  showCustomDialog: function() {
    this.setData({
      showCustomModal: true,
      customCount: 0,
      selectedFoods: [],
      availableFoods: []
    });
  },

  // 隐藏自定义转盘弹窗
  hideCustomDialog: function() {
    this.setData({
      showCustomModal: false,
      customCount: 0,
      selectedFoods: [],
      availableFoods: []
    });
  },

  // 处理数量输入
  onCountInput: function(e) {
    var count = parseInt(e.detail.value);
    var self = this;
    
    // 验证输入范围
    if (count < 2 || count > 12) {
      wx.showToast({
        title: '请输入2-12之间的数字',
        icon: 'none'
      });
      return;
    }
    
    this.setData({
      customCount: count,
      selectedFoods: [],
      availableFoods: []
    });
    
    // 加载可选菜品列表
    this.loadAvailableFoods();
  },

  // 加载可选菜品列表
  loadAvailableFoods: function() {
    var self = this;
    
    // 显示加载状态
    wx.showLoading({ title: '加载中...' });
    
    // 从后端API获取所有美食数据
    var params = {
      page: 1,
      limit: 100, // 获取更多数据，确保有足够的选择
      sortBy: 'rating' // 按评分排序
    };
    
    api.foods.getList(params).then(function(response) {
      wx.hideLoading();
      
      console.log('API获取美食列表成功:', response);
      
      // 处理API返回的数据
      var allFoods = response.list || [];
      var availableFoods = allFoods.map(function(food) {
        return Object.assign({}, food, {
          selected: false
        });
      });
      
      console.log('处理后的可选美食:', availableFoods);
      
      self.setData({
        availableFoods: availableFoods
      });
      
    }).catch(function(apiError) {
      wx.hideLoading();
      console.warn('API调用失败，使用模拟数据:', apiError);
      
      // API 调用失败时，使用模拟数据作为备选
      var allFoods = mockData.foods || [];
      var availableFoods = allFoods.map(function(food) {
        return Object.assign({}, food, {
          selected: false
        });
      });
      
      self.setData({
        availableFoods: availableFoods
      });
      
      wx.showToast({
        title: '使用离线数据',
        icon: 'none'
      });
    });
  },

  // 切换菜品选择状态
  toggleFoodSelection: function(e) {
    var foodId = e.currentTarget.dataset.id;
    var availableFoods = this.data.availableFoods;
    var selectedFoods = this.data.selectedFoods;
    var customCount = this.data.customCount;
    
    // 找到对应的菜品
    var foodIndex = availableFoods.findIndex(function(food) {
      return food.id === foodId;
    });
    
    if (foodIndex === -1) return;
    
    var food = availableFoods[foodIndex];
    
    if (food.selected) {
      // 取消选择
      food.selected = false;
      var selectedIndex = selectedFoods.findIndex(function(selectedFood) {
        return selectedFood.id === foodId;
      });
      if (selectedIndex !== -1) {
        selectedFoods.splice(selectedIndex, 1);
      }
    } else {
      // 检查是否已达到选择上限
      if (selectedFoods.length >= customCount) {
        wx.showToast({
          title: '已达到选择上限',
          icon: 'none'
        });
        return;
      }
      
      // 选择菜品
      food.selected = true;
      selectedFoods.push(Object.assign({}, food));
    }
    
    this.setData({
      availableFoods: availableFoods,
      selectedFoods: selectedFoods
    });
  },

  // 确认自定义转盘
  confirmCustomWheel: function() {
    var selectedFoods = this.data.selectedFoods;
    var customCount = this.data.customCount;
    
    if (selectedFoods.length !== customCount) {
      wx.showToast({
        title: '请选择足够数量的菜品',
        icon: 'none'
      });
      return;
    }
    
    // 更新转盘数据
    this.processWheelFoods(selectedFoods);
    
    // 隐藏弹窗
    this.hideCustomDialog();
    
    wx.showToast({
      title: '自定义转盘设置成功',
      icon: 'success'
    });
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '幸运转盘 - 今天吃什么不再纠结',
      path: '/pages/lucky/lucky',
      imageUrl: '/images/foods/yuxiang-rousi.jpg'
    };
  }
});