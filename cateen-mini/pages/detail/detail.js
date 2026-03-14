// pages/detail/detail.js
var api = require('../../utils/api');

function normalizeFoodDetail(raw, foodId) {
  var image = raw.image_url || raw.imageUrl || (raw.images && raw.images[0]) || '/images/foods/gongbao-chicken.jpg';

  return {
    id: raw.id || foodId,
    name: raw.name || '未命名菜品',
    category: raw.category || raw.category_name || '未分类',
    categoryName: raw.categoryName || raw.category_name || raw.category || '未分类',
    images: raw.images && raw.images.length ? raw.images : [image],
    imageUrl: image,
    description: raw.description || '暂无描述',
    avgRating: Number(raw.avgRating || raw.avg_rating || raw.rating || 0),
    ratingCount: Number(raw.ratingCount || raw.rating_count || 0),
    tags: raw.tags || [],
    isCollected: !!(raw.isCollected || raw.is_collected)
  };
}

function normalizeComment(raw) {
  return {
    id: raw.id,
    userAvatar: raw.userAvatar || raw.user_avatar || '/images/default-avatar.jpg',
    userNickName: raw.userNickName || raw.user_nickname || '食客',
    rating: Number(raw.rating || 0),
    content: raw.content || '',
    images: raw.images || [],
    likeCount: Number(raw.likeCount || raw.like_count || 0),
    isLiked: !!(raw.isLiked || raw.is_liked),
    createTime: raw.createTime || raw.createdAt || raw.create_time || ''
  };
}

Page({
  data: {
    foodId: '',
    foodDetail: {
      id: '',
      name: '',
      category: '',
      images: [],
      description: '',
      avgRating: 0,
      ratingCount: 0,
      tags: [],
      isCollected: false
    },
    comments: {
      list: [],
      total: 0,
      page: 1,
      hasMore: true
    },
    userRating: 0,
    commentText: '',
    loading: false
  },

  onLoad: function(options) {
    var foodId = options.id;
    if (!foodId) {
      wx.showToast({ title: '缺少菜品ID', icon: 'none' });
      return;
    }

    this.setData({ foodId: foodId });
    this.loadFoodDetail();
    this.loadComments(1);
  },

  onShow: function() {
    if (this.data.foodId) {
      this.refreshCollectionStatus();
    }
  },

  loadFoodDetail: function() {
    var self = this;
    this.setData({ loading: true });

    api.foods.getDetail(this.data.foodId).then(function(detail) {
      self.setData({
        foodDetail: normalizeFoodDetail(detail || {}, self.data.foodId),
        loading: false
      });
    }).catch(function(error) {
      console.error('load detail failed:', error);
      self.setData({
        loading: false,
        foodDetail: normalizeFoodDetail({}, self.data.foodId)
      });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  loadComments: function(page) {
    var self = this;
    var currentPage = page || 1;

    api.foods.getComments(this.data.foodId, { page: currentPage, pageSize: 10 }).then(function(result) {
      var list = (result.list || []).map(normalizeComment);

      if (currentPage === 1) {
        self.setData({
          'comments.list': list,
          'comments.total': result.total || list.length,
          'comments.page': 1,
          'comments.hasMore': !!result.hasMore
        });
      } else {
        self.setData({
          'comments.list': self.data.comments.list.concat(list),
          'comments.page': currentPage,
          'comments.hasMore': !!result.hasMore
        });
      }
    }).catch(function(error) {
      console.error('load comments failed:', error);
    });
  },

  refreshCollectionStatus: function() {
    var self = this;
    api.foods.getDetail(this.data.foodId).then(function(detail) {
      self.setData({ 'foodDetail.isCollected': !!(detail.isCollected || detail.is_collected) });
    }).catch(function(error) {
      console.warn('refresh collect status failed:', error);
    });
  },

  toggleCollect: function() {
    var self = this;
    var app = getApp();

    if (!app.globalData.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    var foodId = this.data.foodId;
    var shouldCollect = !this.data.foodDetail.isCollected;
    var request = shouldCollect ? api.user.addCollection(foodId) : api.user.removeCollection(foodId);

    request.then(function() {
      self.setData({ 'foodDetail.isCollected': shouldCollect });
      wx.showToast({ title: shouldCollect ? '已收藏' : '已取消收藏', icon: 'success' });
    }).catch(function(error) {
      console.error('collect failed:', error);
      wx.showToast({ title: '操作失败', icon: 'none' });
    });
  },

  onRatingChange: function(e) {
    this.setData({ userRating: Number(e.detail.rating || 0) });
  },

  onCommentInput: function(e) {
    this.setData({ commentText: e.detail.value || '' });
  },

  submitComment: function() {
    var self = this;
    var rating = this.data.userRating;
    var content = (this.data.commentText || '').trim();

    if (!rating) {
      wx.showToast({ title: '请先给出评分', icon: 'none' });
      return;
    }

    if (!content) {
      wx.showToast({ title: '请输入评论', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });

    api.foods.addComment(this.data.foodId, { rating: rating, content: content }).then(function() {
      wx.hideLoading();
      self.setData({ userRating: 0, commentText: '' });
      self.loadComments(1);
      wx.showToast({ title: '评论已提交', icon: 'success' });
    }).catch(function(error) {
      wx.hideLoading();
      console.error('submit failed:', error);
      wx.showToast({ title: '提交失败', icon: 'none' });
    });
  },

  toggleCommentLike: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.id;
    var index = this.data.comments.list.findIndex(function(item) { return item.id === commentId; });

    if (index === -1) {
      return;
    }

    var item = this.data.comments.list[index];
    var nextLiked = !item.isLiked;
    var nextCount = Math.max(0, item.likeCount + (nextLiked ? 1 : -1));

    var patch = {};
    patch['comments.list[' + index + '].isLiked'] = nextLiked;
    patch['comments.list[' + index + '].likeCount'] = nextCount;
    this.setData(patch);

    api.foods.likeComment(commentId).catch(function(error) {
      console.warn('like failed, rollback:', error);
      var rollback = {};
      rollback['comments.list[' + index + '].isLiked'] = item.isLiked;
      rollback['comments.list[' + index + '].likeCount'] = item.likeCount;
      self.setData(rollback);
    });
  },

  loadMoreComments: function() {
    if (!this.data.comments.hasMore) {
      return;
    }

    var nextPage = this.data.comments.page + 1;
    this.loadComments(nextPage);
  },

  onShareAppMessage: function() {
    return {
      title: '菜品详情 - ' + (this.data.foodDetail.name || ''),
      path: '/pages/detail/detail?id=' + this.data.foodId,
      imageUrl: this.data.foodDetail.images[0] || '/images/foods/gongbao-chicken.jpg'
    };
  }
});
