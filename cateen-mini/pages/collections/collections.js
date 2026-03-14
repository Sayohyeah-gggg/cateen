// pages/collections/collections.js
var api = require('../../utils/api');

function normalizeFood(item) {
  var image = item.image_url || item.imageUrl || (item.images && item.images[0]) || '/images/foods/gongbao-chicken.jpg';

  return {
    id: item.id,
    food: {
      id: item.id,
      name: item.name || '未命名菜品',
      images: [image],
      category: item.category_name || item.categoryName || item.category || '未分类',
      avgRating: Number(item.rating || item.avgRating || 0),
      price: item.price || 0
    }
  };
}

Page({
  data: {
    collections: [],
    loading: true,
    refreshing: false,
    noMore: false,
    page: 1,
    pageSize: 10
  },

  onLoad: function() {
    this.loadCollections(true);
  },

  onShow: function() {
    this.loadCollections(true);
  },

  onPullDownRefresh: function() {
    var self = this;
    this.loadCollections(true).finally(function() {
      wx.stopPullDownRefresh();
    });
  },

  loadCollections: function(refresh) {
    var self = this;
    var isRefresh = !!refresh;

    if (isRefresh) {
      this.setData({ page: 1, noMore: false, refreshing: true, loading: true });
    }

    return api.user.getCollections({ page: this.data.page, pageSize: this.data.pageSize }).then(function(result) {
      var list = (result.list || []).map(normalizeFood);
      var merged = isRefresh ? list : self.data.collections.concat(list);

      self.setData({
        collections: merged,
        noMore: !result.hasMore,
        page: isRefresh ? 2 : self.data.page + 1,
        loading: false,
        refreshing: false
      });
    }).catch(function(error) {
      console.error('load collections failed:', error);
      self.setData({ loading: false, refreshing: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    if (!foodId) {
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + foodId });
  },

  removeCollection: function(e) {
    var self = this;
    var foodId = e.currentTarget.dataset.foodId;
    var collectionId = e.currentTarget.dataset.collectionId;

    wx.showModal({
      title: '取消收藏',
      content: '确定要取消收藏这道菜吗？',
      success: function(res) {
        if (!res.confirm) {
          return;
        }

        wx.showLoading({ title: '处理中...' });

        api.user.removeCollection(foodId).then(function() {
          var list = self.data.collections.filter(function(item) { return item.id !== collectionId; });
          self.setData({ collections: list });
          wx.hideLoading();
          wx.showToast({ title: '已取消', icon: 'success' });
        }).catch(function(error) {
          wx.hideLoading();
          console.error('remove failed:', error);
          wx.showToast({ title: '操作失败', icon: 'none' });
        });
      }
    });
  },

  loadMore: function() {
    if (this.data.noMore) {
      return;
    }
    this.loadCollections(false);
  },

  goToIndex: function() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  onNoop: function() {},

  onShareAppMessage: function() {
    return {
      title: '我的收藏',
      path: '/pages/collections/collections',
      imageUrl: '/images/foods/gongbao-chicken.jpg'
    };
  }
});
