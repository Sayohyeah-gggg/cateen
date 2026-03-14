// pages/lucky/lucky.js
var api = require('../../utils/api');
var mockData = require('../../data/mock');

var STORAGE_KEY = 'luckyHistory';

function formatTime(timestamp) {
  var date = new Date(timestamp);
  var month = date.getMonth() + 1;
  var day = date.getDate();
  var hour = date.getHours();
  var minute = date.getMinutes();

  var mm = month < 10 ? '0' + month : '' + month;
  var dd = day < 10 ? '0' + day : '' + day;
  var hh = hour < 10 ? '0' + hour : '' + hour;
  var mi = minute < 10 ? '0' + minute : '' + minute;

  return mm + '-' + dd + ' ' + hh + ':' + mi;
}

function withSelected(food) {
  var images = food.images && food.images.length ? food.images : [food.image_url || food.imageUrl || '/images/foods/gongbao-chicken.jpg'];
  return Object.assign({}, food, {
    images: images,
    selected: !!food.selected
  });
}

Page({
  data: {
    showBack: false,
    wheelFoods: [],
    wheelRotation: 0,
    spinning: false,
    spinDuration: 3,
    showResult: false,
    spinResult: null,
    todayRecommendations: [],
    luckyHistory: [],
    wheelBackground: '',
    labelRadius: 200,

    // 光效相关
    showGlow: false,
    highlightIndex: -1, // 当前高亮的区域索引
    highlightAngle: 0,  // 高亮区域的角度位置
    highlightColor: 'rgba(255, 215, 0, 0.6)', // 金色半透明高亮
    glowOpacity: 0,

    showCustomModal: false,
    customCount: 0,
    customCountInput: '',
    availableFoods: [],
    selectedFoods: [],

    colorPalette: [
      '#f9a8d4', '#f87171', '#6ee7b7', '#fde68a', '#a5b4fc', '#34d399',
      '#facc15', '#60a5fa', '#fb7185', '#7dd3fc', '#c7d2fe', '#86efac'
    ]
  },

  onLoad: function() {
    var pages = getCurrentPages();
    this.setData({ showBack: pages.length > 1 });

    this.loadWheelFoods();
    this.loadTodayRecommendations();
    this.loadLuckyHistory();
  },

  onShow: function() {
    this.loadTodayRecommendations();
  },

  onPullDownRefresh: function() {
    var self = this;
    this.refreshFoods().finally(function() {
      wx.stopPullDownRefresh();
    });
  },

  loadWheelFoods: function() {
    var self = this;

    return api.foods.getList({ page: 1, pageSize: 8 }).then(function(result) {
      var list = (result.list || []).map(withSelected);
      if (!list.length) {
        list = (mockData.luckyFoods || []).map(withSelected);
      }

      self.processWheelFoods(list.slice(0, 6));
    }).catch(function(error) {
      console.warn('load wheel foods failed, use mock:', error);
      var list = (mockData.luckyFoods || []).map(withSelected);
      self.processWheelFoods(list.slice(0, 6));
    });
  },

  processWheelFoods: function(foods) {
    if (!foods || !foods.length) {
      this.setData({ wheelFoods: [], wheelBackground: '' });
      return;
    }

    var per = 360 / foods.length;
    var stops = [];
    var list = foods.map(function(food, index) {
      var start = index * per;
      var end = (index + 1) * per;
      var center = start + per / 2;
      var color = this.data.colorPalette[index % this.data.colorPalette.length];

      stops.push(color + ' ' + start + 'deg ' + end + 'deg');

      return Object.assign({}, food, {
        startAngle: start,
        endAngle: end,
        labelAngle: center,
        color: color
      });
    }, this);

    this.setData({
      wheelFoods: list,
      wheelBackground: 'conic-gradient(' + stops.join(', ') + ')'
    });
  },

  refreshFoods: function() {
    var self = this;
    wx.showLoading({ title: '刷新中...' });

    return this.loadWheelFoods().then(function() {
      wx.hideLoading();
      wx.showToast({ title: '已刷新', icon: 'success' });
    }).catch(function() {
      wx.hideLoading();
      wx.showToast({ title: '刷新失败', icon: 'none' });
    });
  },

  startSpin: function() {
    if (this.data.spinning || !this.data.wheelFoods.length) {
      return;
    }

    var foods = this.data.wheelFoods;
    var index = Math.floor(Math.random() * foods.length);
    var selected = foods[index];
    
    // 设置目标索引
    this.setData({
      spinning: true,
      spinResult: selected,
      highlightIndex: index,
      showGlow: true,
      glowOpacity: 1
    });

    var self = this;
    
    // 第一阶段：显示目标区域发光（0.5 秒）
    setTimeout(function() {
      self.startHighlightRotation(index);
    }, 500);
  },

  addToHistory: function(food) {
    var history = this.data.luckyHistory.slice();
    history.unshift(Object.assign({}, food, { spinTime: formatTime(Date.now()) }));

    if (history.length > 10) {
      history = history.slice(0, 10);
    }

    this.setData({ luckyHistory: history });
    wx.setStorageSync(STORAGE_KEY, history);
  },

  /** 开始高亮区域旋转 */
  startHighlightRotation: function(targetIndex) {
    var self = this;
    var foods = this.data.wheelFoods;
    var per = 360 / foods.length;
    
    // 计算目标区域的角度（从顶部指针位置开始）
    // 顶部是 0 度，顺时针旋转
    var targetSegmentAngle = targetIndex * per;
    
    // 总旋转圈数（至少 5 圈）
    var totalRotations = 360 * 5;
    
    // 最终需要到达的角度（让目标区域停在顶部指针位置）
    // 转盘逆时针旋转，所以角度要反过来算
    var finalAngle = totalRotations + (360 - targetSegmentAngle);
    
    // 当前已旋转角度
    var currentRotation = 0;
    
    // 初始速度（每帧移动的度数）
    var speed = 15;
    
    // 减速系数
    var deceleration = 0.98;
    
    // 最小速度阈值
    var minSpeed = 0.2;
    
    function animate() {
      // 计算剩余距离
      var remaining = finalAngle - currentRotation;
      
      // 当接近目标时减速（最后 2 圈）
      if (remaining < 720) {
        speed = speed * deceleration;
      }
      
      // 如果速度太慢或已到达目标，停止动画并显示结果
      if (speed < minSpeed || currentRotation >= finalAngle) {
        // 强制设置到最终位置
        currentRotation = finalAngle;
        
        // 一次性更新所有状态：转盘位置、停止转动、显示结果
        self.setData({
          wheelRotation: currentRotation,  // 转盘精确停在目标位置
          spinning: false,                  // 停止转动状态
          showResult: true,                 // 显示结果框
          showGlow: false,                  // 隐藏高亮
          glowOpacity: 0                    // 透明度归零
        });
        
        // 记录历史和震动反馈
        self.addToHistory(self.data.wheelFoods[targetIndex]);
        wx.vibrateShort();
        return;
      }
      
      // 更新旋转角度
      currentRotation += speed;
      if (currentRotation > finalAngle) {
        currentRotation = finalAngle;
      }
      
      // 计算当前应该高亮的区域索引
      // 随着转盘旋转，高亮区域也在变化
      var normalizedRotation = currentRotation % 360;
      var currentIndex = Math.floor(((360 - normalizedRotation) % 360) / per);
      
      // 更新高亮区域和转盘角度
      self.setData({
        wheelRotation: currentRotation,
        highlightIndex: currentIndex,
        highlightAngle: currentIndex * per
      });
      
      // 继续下一帧
      setTimeout(animate, 16); // 约 60fps
    }
    
    // 开始动画
    animate();
  },

  loadTodayRecommendations: function() {
    var self = this;
    
    // 先显示模拟数据作为占位
    var mockList = (mockData.todayRecommendations || mockData.foods || []).map(withSelected);
    self.setData({ 
      todayRecommendations: mockList.slice(0, 4) 
    });
    
    // 然后尝试从排行榜 API 获取真实数据
    wx.showLoading({ title: '加载中...' });

    api.ranking.getRanking({ type: 'rating', page: 1, pageSize: 10 })
      .then(function(result) {
        wx.hideLoading();
        console.log('排行榜数据:', result);
        
        // result 可能是数组（直接返回 list）或对象（包含 list 属性）
        var rankingList = Array.isArray(result) ? result : (result.list || []);
        
        if (rankingList.length === 0) {
          console.warn('排行榜数据为空');
          return;
        }
        
        // 将排行榜数据转换为食物格式
        var foods = rankingList.map(function(item) {
          return withSelected({
            id: item.food_id,
            name: item.food_name,
            images: [item.food_image],
            category: item.category_name,
            description: '',
            avgRating: item.rating,
            ratingCount: item.rating_count
          });
        });

        // 去重并取前 4 个
        var uniqueFoods = [];
        var ids = {};
        for (var i = 0; i < foods.length; i++) {
          if (!ids[foods[i].id]) {
            ids[foods[i].id] = true;
            uniqueFoods.push(foods[i]);
          }
        }

        // 只有获取到真实数据才更新
        if (uniqueFoods.length > 0) {
          self.setData({ 
            todayRecommendations: uniqueFoods.slice(0, 4) 
          });
          console.log('今日推荐已更新为排行榜数据:', uniqueFoods);
        }
      })
      .catch(function(error) {
        wx.hideLoading();
        console.error('加载推荐失败:', error);
        // 已经显示了模拟数据，不需要额外处理
      });
  },

  loadLuckyHistory: function() {
    var history = wx.getStorageSync(STORAGE_KEY) || [];
    this.setData({ luckyHistory: history });
  },

  hideResult: function() {
    // 关闭结果框时，确保 spinning 状态为 false（防止转盘继续转动）
    this.setData({ 
      showResult: false,
      spinning: false
    });
  },

  stopPropagation: function() {},

  viewDetail: function() {
    if (!this.data.spinResult || !this.data.spinResult.id) {
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + this.data.spinResult.id });
    this.hideResult();
  },

  goToDetail: function(e) {
    var id = e.currentTarget.dataset.id;
    if (!id) {
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + id });
  },

  showCustomDialog: function() {
    this.setData({
      showCustomModal: true,
      customCount: 0,
      customCountInput: '',
      availableFoods: [],
      selectedFoods: []
    });
  },

  hideCustomDialog: function() {
    this.setData({
      showCustomModal: false,
      customCount: 0,
      customCountInput: '',
      availableFoods: [],
      selectedFoods: []
    });
  },

  onCountInput: function(e) {
    var raw = (e.detail.value || '').replace(/[^0-9]/g, '');
    raw = raw.replace(/^0+/, '');

    if (!raw) {
      this.setData({
        customCount: 0,
        customCountInput: '',
        availableFoods: [],
        selectedFoods: []
      });
      return;
    }

    var count = Number(raw);

    if (count < 2 || count > 12) {
      wx.showToast({ title: '请输入 2-12 之间的数字', icon: 'none' });
      this.setData({
        customCount: 0,
        customCountInput: '',
        availableFoods: [],
        selectedFoods: []
      });
      return;
    }

    this.setData({
      customCount: count,
      customCountInput: raw,
      availableFoods: [],
      selectedFoods: []
    });
    this.loadAvailableFoods();
  },

  loadAvailableFoods: function() {
    var self = this;
    wx.showLoading({ title: '加载中...' });

    api.foods.getList({ page: 1, pageSize: 100 }).then(function(result) {
      wx.hideLoading();
      var list = (result.list || []).map(withSelected).map(function(food) {
        return Object.assign({}, food, { selected: false });
      });
      self.setData({ availableFoods: list });
    }).catch(function(error) {
      wx.hideLoading();
      console.warn('load available foods failed, use mock:', error);
      var list = (mockData.foods || []).map(withSelected).map(function(food) {
        return Object.assign({}, food, { selected: false });
      });
      self.setData({ availableFoods: list });
      wx.showToast({ title: '使用本地数据', icon: 'none' });
    });
  },

  toggleFoodSelection: function(e) {
    var id = e.currentTarget.dataset.id;
    var count = this.data.customCount;
    var list = this.data.availableFoods.slice();
    var selected = this.data.selectedFoods.slice();
    var index = list.findIndex(function(item) { return item.id === id; });

    if (index === -1) {
      return;
    }

    var item = Object.assign({}, list[index]);

    if (item.selected) {
      item.selected = false;
      selected = selected.filter(function(food) { return food.id !== id; });
    } else {
      if (selected.length >= count) {
        wx.showToast({ title: '已达选择上限', icon: 'none' });
        return;
      }
      item.selected = true;
      selected.push(Object.assign({}, item));
    }

    list[index] = item;
    this.setData({ availableFoods: list, selectedFoods: selected });
  },

  confirmCustomWheel: function() {
    if (this.data.selectedFoods.length !== this.data.customCount) {
      wx.showToast({ title: '已选数量与设定不一致', icon: 'none' });
      return;
    }

    this.processWheelFoods(this.data.selectedFoods);
    this.hideCustomDialog();
    wx.showToast({ title: '转盘已更新', icon: 'success' });
  },

  onShareAppMessage: function() {
    return {
      title: '幸运转盘',
      path: '/pages/lucky/lucky',
      imageUrl: '/images/foods/hongshao-pork.jpg'
    };
  }
});
