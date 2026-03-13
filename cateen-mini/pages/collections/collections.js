// pages/collections/collections.js
var api = require('../../utils/api');

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
    this.loadCollections();
  },

  onShow: function() {
    // 页面显示时刷新数据
    this.loadCollections(true);
  },

  // 加载收藏列表
  loadCollections: function(refresh) {
    var self = this;
    if (refresh === undefined) refresh = false;
    
    if (refresh) {
      this.setData({ 
        page: 1, 
        noMore: false,
        refreshing: true 
      });
    }

    try {
      var params = {
        page: this.data.page,
        pageSize: this.data.pageSize
      };
      
      api.user.getCollections(params).then(function(collectionsData) {
        console.log('收藏API返回数据:', collectionsData);
        console.log('收藏列表:', collectionsData.list);
        
        // 转换snake_case字段为camelCase，并转换为前端期望的格式
        var newCollections = collectionsData.list.map(function(food) {
          return {
            id: food.id,
            food: {
              id: food.id,
              name: food.name,
              images: [food.image_url || food.imageUrl], // 处理snake_case字段
              category: food.category_name || food.categoryName,
              avgRating: food.rating,
              price: food.price
            }
          };
        });
        
        console.log('处理后的收藏数据:', newCollections);
        
        newCollections = refresh ? newCollections : self.data.collections.concat(newCollections);

        self.setData({
          collections: newCollections,
          noMore: !collectionsData.hasMore,
          page: refresh ? 2 : self.data.page + 1,
          loading: false,
          refreshing: false
        });
      }).catch(function(error) {
        console.error('加载收藏列表失败:', error);
        self.setData({ 
          loading: false, 
          refreshing: false
        });
      });
    } catch (error) {
      console.error('加载收藏列表失败:', error);
      this.setData({ 
        loading: false, 
        refreshing: false
      });
    }
  },

  // 跳转详情页
  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 取消收藏
  removeCollection: function(e) {
    var self = this;
    var foodId = e.currentTarget.dataset.foodId;
    var collectionId = e.currentTarget.dataset.collectionId;
    
    wx.showModal({
      title: '确认取消收藏',
      content: '确定要取消收藏这道美食吗？',
      success: function(res) {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '处理中...' });
            
            api.user.removeCollection(foodId).then(function() {
              // 更新本地数据
              var collections = self.data.collections.filter(function(item) { return item.id !== collectionId; });
              self.setData({ collections: collections });

              wx.hideLoading();
              wx.showToast({
                title: '取消收藏成功',
                icon: 'success'
              });
            }).catch(function(error) {
              wx.hideLoading();
              console.error('取消收藏失败:', error);
            });
          } catch (error) {
            wx.hideLoading();
            console.error('取消收藏失败:', error);
          }
        }
      }
    });
  },

  // 下拉刷新
  onRefresh: function() {
    this.loadCollections(true);
  },

  // 加载更多
  loadMore: function() {
    if (this.data.noMore) return;
    this.loadCollections();
  },

  // 跳转到首页
  goToIndex: function() {
    wx.switchTab({
      url: '/pages/index/index'
    });
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '我的美食收藏 - 美食评估系统',
      path: '/pages/collections/collections',
      imageUrl: '/images/share-collections.jpg'
    };
  }
});
