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
    var per = 360 / foods.length;
    var target = selected.startAngle + per / 2;
    var finalRotation = this.data.wheelRotation + 360 * 5 + (360 - target);

    this.setData({
      spinning: true,
      wheelRotation: finalRotation,
      spinResult: selected
    });

    var self = this;
    setTimeout(function() {
      self.setData({ spinning: false, showResult: true });
      self.addToHistory(selected);
      wx.vibrateShort();
    }, this.data.spinDuration * 1000);
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

  loadTodayRecommendations: function() {
    var list = (mockData.todayRecommendations || mockData.foods || []).map(withSelected);
    this.setData({ todayRecommendations: list.slice(0, 4) });
  },

  loadLuckyHistory: function() {
    var history = wx.getStorageSync(STORAGE_KEY) || [];
    this.setData({ luckyHistory: history });
  },

  hideResult: function() {
    this.setData({ showResult: false });
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
