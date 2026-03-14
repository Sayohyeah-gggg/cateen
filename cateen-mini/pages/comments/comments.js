// pages/comments/comments.js
var api = require('../../utils/api');

function normalizeComment(item) {
  return {
    id: item.id,
    userNickname: item.user_nickname || item.userNickname || '食客',
    userAvatar: item.user_avatar || item.userAvatar || '/images/default-avatar.jpg',
    rating: Number(item.rating || 0),
    content: item.content || '',
    images: item.images || [],
    likeCount: Number(item.like_count || item.likeCount || 0),
    isLiked: !!(item.is_liked || item.isLiked),
    createdAt: item.created_at || item.createdAt || '',
    foodId: item.food_id || item.foodId,
    foodName: item.food_name || item.foodName || '未命名菜品',
    foodImage: item.food_image || item.foodImage || '/images/foods/gongbao-chicken.jpg'
  };
}

Page({
  data: {
    comments: [],
    loading: true,
    refreshing: false,
    noMore: false,
    page: 1,
    pageSize: 10
  },

  onLoad: function() {
    this.loadComments(true);
  },

  onShow: function() {
    this.loadComments(true);
  },

  onPullDownRefresh: function() {
    var self = this;
    this.loadComments(true).finally(function() {
      wx.stopPullDownRefresh();
    });
  },

  loadComments: function(refresh) {
    var self = this;
    var isRefresh = !!refresh;

    if (isRefresh) {
      this.setData({ page: 1, noMore: false, refreshing: true, loading: true });
    }

    return api.user.getComments({ page: this.data.page, pageSize: this.data.pageSize }).then(function(result) {
      var list = (result.list || []).map(normalizeComment);
      var merged = isRefresh ? list : self.data.comments.concat(list);

      self.setData({
        comments: merged,
        noMore: !result.hasMore,
        page: isRefresh ? 2 : self.data.page + 1,
        loading: false,
        refreshing: false
      });
    }).catch(function(error) {
      console.error('load comments failed:', error);
      self.setData({ loading: false, refreshing: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    if (!foodId) {
      wx.showToast({ title: '缺少菜品ID', icon: 'none' });
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + foodId });
  },

  likeComment: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.commentId;
    var isLiked = !!e.currentTarget.dataset.isLiked;

    var list = this.data.comments.map(function(item) {
      if (item.id !== commentId) {
        return item;
      }

      return Object.assign({}, item, {
        isLiked: !isLiked,
        likeCount: Math.max(0, item.likeCount + (isLiked ? -1 : 1))
      });
    });

    this.setData({ comments: list });

    api.foods.likeComment(commentId).catch(function(error) {
      console.warn('like failed, reload list:', error);
      self.loadComments(true);
    });
  },

  deleteComment: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.commentId;

    wx.showModal({
      title: '删除评论',
      content: '确定要删除这条评论吗？',
      success: function(res) {
        if (!res.confirm) {
          return;
        }

        wx.showLoading({ title: '删除中...' });

        api.foods.deleteComment(commentId).then(function() {
          var list = self.data.comments.filter(function(item) { return item.id !== commentId; });
          self.setData({ comments: list });
          wx.hideLoading();
          wx.showToast({ title: '已删除', icon: 'success' });
        }).catch(function(error) {
          wx.hideLoading();
          console.error('delete failed:', error);
          wx.showToast({ title: '删除失败', icon: 'none' });
        });
      }
    });
  },

  previewImage: function(e) {
    var current = e.currentTarget.dataset.current;
    var urls = e.currentTarget.dataset.urls || [];
    wx.previewImage({ current: current, urls: urls });
  },

  loadMore: function() {
    if (this.data.noMore) {
      return;
    }
    this.loadComments(false);
  },

  goToIndex: function() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  onShareAppMessage: function() {
    return {
      title: '我的评论',
      path: '/pages/comments/comments',
      imageUrl: '/images/foods/hongshao-pork.jpg'
    };
  }
});
