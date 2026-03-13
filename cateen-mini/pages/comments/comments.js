// pages/comments/comments.js
var api = require('../../utils/api');

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
    this.loadComments();
  },

  onShow: function() {
    // 页面显示时刷新数据
    this.loadComments(true);
  },

  // 加载评论列表
  loadComments: function(refresh) {
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
      
      api.user.getComments(params).then(function(commentsData) {
        console.log('用户评论API返回数据:', commentsData);
        console.log('评论列表:', commentsData.list);
        
        // 转换snake_case字段为camelCase
        var processedComments = commentsData.list.map(function(comment) {
          return {
            id: comment.id,
            userNickname: comment.user_nickname || comment.userNickname,
            userAvatar: comment.user_avatar || comment.userAvatar,
            rating: comment.rating,
            content: comment.content,
            images: comment.images,
            likeCount: comment.like_count || comment.likeCount || 0,
            isLiked: comment.is_liked || comment.isLiked || false,
            createdAt: comment.created_at || comment.createdAt,
            foodId: comment.food_id || comment.foodId,  // 关键字段
            foodName: comment.food_name || comment.foodName,
            foodImage: comment.food_image || comment.foodImage
          };
        });
        
        console.log('处理后的评论数据:', processedComments);
        
        var newComments = refresh ? processedComments : self.data.comments.concat(processedComments);

        self.setData({
          comments: newComments,
          noMore: !commentsData.hasMore,
          page: refresh ? 2 : self.data.page + 1,
          loading: false,
          refreshing: false
        });
        
        console.log('设置后的评论数据:', self.data.comments);
      }).catch(function(error) {
        console.error('加载评论列表失败:', error);
        self.setData({ 
          loading: false, 
          refreshing: false
        });
      });
    } catch (error) {
      console.error('加载评论列表失败:', error);
      this.setData({ 
        loading: false, 
        refreshing: false
      });
    }
  },

  // 跳转详情页
  goToDetail: function(e) {
    var foodId = e.currentTarget.dataset.id;
    console.log('点击美食，foodId:', foodId);
    console.log('事件对象:', e);
    console.log('dataset:', e.currentTarget.dataset);
    
    if (!foodId) {
      console.error('foodId为空，无法跳转');
      wx.showToast({
        title: '美食信息错误',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + foodId
    });
  },

  // 点赞评论
  likeComment: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.commentId;
    var isLiked = e.currentTarget.dataset.isLiked;
    
    try {
      api.foods.likeComment(commentId).then(function() {
        // 更新本地数据
        var comments = self.data.comments.map(function(comment) {
          if (comment.id === commentId) {
            return Object.assign({}, comment, {
              likeCount: isLiked ? comment.likeCount - 1 : comment.likeCount + 1,
              isLiked: !isLiked
            });
          }
          return comment;
        });

        self.setData({ comments: comments });

        wx.showToast({
          title: isLiked ? '取消点赞' : '点赞成功',
          icon: 'success'
        });
      }).catch(function(error) {
        console.error('点赞操作失败:', error);
      });
    } catch (error) {
      console.error('点赞操作失败:', error);
    }
  },

  // 删除评论
  deleteComment: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.commentId;
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条评论吗？',
      success: function(res) {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '删除中...' });
            
            api.foods.deleteComment(commentId).then(function() {
              // 更新本地数据
              var comments = self.data.comments.filter(function(comment) { return comment.id !== commentId; });
              self.setData({ comments: comments });

              wx.hideLoading();
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              });
            }).catch(function(error) {
              wx.hideLoading();
              console.error('删除评论失败:', error);
            });
          } catch (error) {
            wx.hideLoading();
            console.error('删除评论失败:', error);
          }
        }
      }
    });
  },

  // 预览图片
  previewImage: function(e) {
    var current = e.currentTarget.dataset.current;
    var urls = e.currentTarget.dataset.urls;
    wx.previewImage({
      current: current,
      urls: urls
    });
  },

  // 下拉刷新
  onRefresh: function() {
    this.loadComments(true);
  },

  // 加载更多
  loadMore: function() {
    if (this.data.noMore) return;
    this.loadComments();
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
      title: '我的美食评论 - 美食评估系统',
      path: '/pages/comments/comments',
      imageUrl: '/images/share-comments.jpg'
    };
  }
});
