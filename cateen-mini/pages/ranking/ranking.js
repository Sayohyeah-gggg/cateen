// pages/ranking/ranking.js
var api = require('../../utils/api');

Page({
  data: {
    currentTab: 'overall',
    categoryList: [
      { name: '全部分类', code: 'all' },
      { name: '主食类', code: 'staple' },
      { name: '菜肴类', code: 'dish' },
      { name: '汤羹类', code: 'soup' }
    ],
    timeRangeList: [
      { name: '全部时间', code: 'all' },
      { name: '本周', code: 'week' },
      { name: '本月', code: 'month' }
    ],
    selectedCategoryIndex: 0,
    selectedTimeIndex: 0,
    rankingList: [],
    loading: false
  },

  onLoad: function() {
    console.log('排行榜页面加载');
    this.loadRankingData();
  },

  onShow: function() {
    console.log('排行榜页面显示');
    // 页面显示时刷新数据
    this.loadRankingData();
  },

  // 切换榜单类型
  switchTab: function(e) {
    var tab = e.currentTarget.dataset.tab;
    this.setData({
      currentTab: tab,
      selectedCategoryIndex: 0,
      selectedTimeIndex: 0
    });
    this.loadRankingData();
  },

  // 选择分类
  onCategoryChange: function(e) {
    this.setData({
      selectedCategoryIndex: parseInt(e.detail.value)
    });
    this.loadRankingData();
  },

  // 选择时间范围
  onTimeRangeChange: function(e) {
    this.setData({
      selectedTimeIndex: parseInt(e.detail.value)
    });
    this.loadRankingData();
  },

  // 加载排行榜数据
  loadRankingData: function() {
    var self = this;
    console.log('开始加载排行榜数据...');
    this.setData({ loading: true });

    // 从 API 获取排行榜数据
    var params = {
      type: this.data.currentTab,
      category: this.data.selectedCategoryIndex > 0 ? this.data.categoryList[this.data.selectedCategoryIndex].code : undefined,
      timeRange: this.data.timeRangeList[this.data.selectedTimeIndex].code
    };
    
    console.log('排行榜API参数:', params);
    
    return api.ranking.getRanking(params).then(function(data) {
      console.log('API获取排行榜数据成功:', data);
      self.setData({
        rankingList: data,
        loading: false
      });
      return data;
    }).catch(function(apiError) {
      console.error('API调用失败:', apiError);
      wx.showToast({
        title: '加载排行榜失败',
        icon: 'error'
      });
      
      self.setData({
        rankingList: [],
        loading: false
      });
      
      return Promise.reject(apiError);
    });
  },


  // 跳转到美食详情页
  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    console.log('跳转到美食详情页，foodId:', foodId);
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 下拉刷新
  onPullDownRefresh: function() {
    var self = this;
    this.loadRankingData().then(function() {
      wx.stopPullDownRefresh();
    }).catch(function(error) {
      console.error('下拉刷新失败:', error);
      wx.stopPullDownRefresh();
    });
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '美食排行榜 - 发现最受欢迎的美食',
      path: '/pages/ranking/ranking',
      imageUrl: '/images/foods/gongbao-chicken.jpg'
    };
  }
});


