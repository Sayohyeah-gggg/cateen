// pages/detail/detail.js
var api = require('../../utils/api');
var mockData = require('../../data/mock.js');

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
    var id = options.id;
    console.log('详情页加载，接收到的参数:', options);
    console.log('foodId:', id);
    if (id) {
      this.setData({ foodId: id });
      this.loadFoodDetail();
      this.loadComments();
    } else {
      console.error('未接收到foodId参数');
    }
  },

  onShow: function() {
    // 页面显示时刷新收藏状态（用户可能从其他页面返回）
    if (this.data.foodId) {
      console.log('详情页显示，刷新收藏状态');
      this.refreshCollectionStatus();
    }
  },

  // 加载美食详情
  loadFoodDetail: function() {
    var self = this;
    try {
      this.setData({ loading: true });
      
      // 首先尝试从 API 获取数据
      api.foods.getDetail(this.data.foodId).then(function(foodDetail) {
        // 处理后端的蛇形命名，转换为驼峰命名
        if (foodDetail.is_collected !== undefined) {
          foodDetail.isCollected = foodDetail.is_collected;
        }
        if (foodDetail.image_url !== undefined) {
          foodDetail.imageUrl = foodDetail.image_url;
        }
        if (foodDetail.category_id !== undefined) {
          foodDetail.categoryId = foodDetail.category_id;
        }
        if (foodDetail.category_name !== undefined) {
          foodDetail.categoryName = foodDetail.category_name;
        }
        if (foodDetail.avg_rating !== undefined) {
          foodDetail.avgRating = foodDetail.avg_rating;
        }
        if (foodDetail.rating_count !== undefined) {
          foodDetail.ratingCount = foodDetail.rating_count;
        }
        if (foodDetail.collection_count !== undefined) {
          foodDetail.collectionCount = foodDetail.collection_count;
        }
        if (foodDetail.comment_count !== undefined) {
          foodDetail.commentCount = foodDetail.comment_count;
        }
        if (foodDetail.description !== undefined) {
          // description字段通常已经是驼峰命名，但确保有值
          foodDetail.description = foodDetail.description || '暂无描述';
        }
        
        console.log('处理后的美食详情:', foodDetail);
        
        self.setData({
          foodDetail: foodDetail,
          loading: false
        });
      }).catch(function(apiError) {
        console.warn('API调用失败，使用模拟数据:', apiError);
        
        // API 调用失败时，使用模拟数据
        var mockFood = mockData.foods.find(function(food) { return food.id === self.data.foodId; });
        if (mockFood) {
          self.setData({
            foodDetail: mockFood,
            loading: false
          });
        } else {
          // 如果模拟数据中也没有，使用默认数据
          var defaultFood = {
            id: self.data.foodId,
            name: '美食详情',
            category: '未分类',
            images: ['/images/placeholder.jpg'],
            description: '暂无详细描述',
            avgRating: 0,
            ratingCount: 0,
            tags: [],
            isCollected: false
          };
          
          self.setData({
            foodDetail: defaultFood,
            loading: false
          });
        }
      });
    } catch (error) {
      console.error('加载美食详情失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      });
      this.setData({ loading: false });
    }
  },

  // 加载评论列表
  loadComments: function(page) {
    var self = this;
    if (page === undefined) page = 1;
    
    try {
      // 首先尝试从 API 获取评论数据
      var commentsData;
      api.foods.getComments(this.data.foodId, { page: page, pageSize: 10 }).then(function(data) {
        // 处理API返回的评论数据，确保点赞数量正确初始化
        if (data && data.list) {
          data.list = data.list.map(function(comment) {
            return {
              id: comment.id,
              userAvatar: comment.userAvatar || comment.user_avatar || '/images/default-avatar.jpg',
              userNickName: comment.userNickname || comment.userNickName || comment.user_nickname || '用户',
              rating: comment.rating || 0,
              content: comment.content || '',
              images: comment.images || [],
              likeCount: comment.likeCount || comment.like_count || 0, // 确保点赞数量不为null
              isLiked: comment.isLiked || comment.is_liked || false,
              createTime: comment.createdAt || comment.createTime || comment.create_time || new Date().toLocaleDateString()
            };
          });
        }
        commentsData = data;
        self.processCommentsData(commentsData, page);
      }).catch(function(apiError) {
        console.warn('API调用失败，使用模拟数据:', apiError);
        
        // API 调用失败时，使用模拟数据
        var mockComments = mockData.comments.filter(function(comment) { return comment.foodId === self.data.foodId; });
        commentsData = {
          list: mockComments.map(function(comment) {
            return {
              id: comment.id,
              userAvatar: comment.userAvatar || '/images/default-avatar.jpg',
              userNickName: comment.userNickName,
              rating: comment.rating,
              content: comment.content,
              images: comment.images || [],
              likeCount: comment.likeCount || 0, // 确保点赞数量不为null
              isLiked: comment.isLiked || false,
              createTime: comment.createTime
            };
          }),
          total: mockComments.length,
          hasMore: false
        };
        self.processCommentsData(commentsData, page);
      });
    } catch (error) {
      console.error('加载评论失败:', error);
    }
  },

  // 处理评论数据
  processCommentsData: function(commentsData, page) {
    if (page === 1) {
      this.setData({
        'comments.list': commentsData.list,
        'comments.total': commentsData.total,
        'comments.page': 1,
        'comments.hasMore': commentsData.hasMore
      });
    } else {
      this.setData({
        'comments.list': this.data.comments.list.concat(commentsData.list),
        'comments.page': page,
        'comments.hasMore': commentsData.hasMore
      });
    }
  },

  // 切换收藏状态
  toggleCollect: function() {
    var self = this;
    var app = getApp();
    
    // 检查登录状态
    if (!app.globalData.isLoggedIn) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    try {
      var isCollected = !this.data.foodDetail.isCollected;
      var foodId = this.data.foodId;
      
      console.log('切换收藏状态:', { foodId: foodId, isCollected: isCollected });
      console.log('当前页面数据:', this.data);
      
      if (!foodId) {
        console.error('foodId为空，无法执行收藏操作');
        wx.showToast({
          title: '美食ID缺失',
          icon: 'error'
        });
        return;
      }
      
      // 调用后端API
      if (isCollected) {
        // 添加收藏
        console.log('准备发送收藏请求，foodId:', foodId);
        api.user.addCollection(foodId).then(function() {
          self.setData({
            'foodDetail.isCollected': true
          });
          wx.showToast({
            title: '收藏成功',
            icon: 'success'
          });
          // 刷新个人中心页面的收藏数量
          self.refreshProfileCollectionCount();
        }).catch(function(error) {
          console.error('添加收藏失败:', error);
          wx.showToast({
            title: '收藏失败',
            icon: 'error'
          });
        });
      } else {
        // 取消收藏
        api.user.removeCollection(foodId).then(function() {
          self.setData({
            'foodDetail.isCollected': false
          });
          wx.showToast({
            title: '取消收藏',
            icon: 'success'
          });
          // 刷新个人中心页面的收藏数量
          self.refreshProfileCollectionCount();
        }).catch(function(error) {
          console.error('取消收藏失败:', error);
          wx.showToast({
            title: '操作失败',
            icon: 'error'
          });
        });
      }
    } catch (error) {
      console.error('收藏操作失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'error'
      });
    }
  },

  // 刷新个人中心页面的收藏数量
  refreshProfileCollectionCount: function() {
    var app = getApp();
    // 获取个人中心页面实例并刷新数据
    var pages = getCurrentPages();
    for (var i = 0; i < pages.length; i++) {
      if (pages[i].route === 'pages/profile/profile') {
        pages[i].loadUserInfo();
        break;
      }
    }
  },

  // 刷新收藏状态（从后端重新获取）
  refreshCollectionStatus: function() {
    var self = this;
    
    // 重新从后端获取美食详情（包含最新的收藏状态）
    api.foods.getDetail(this.data.foodId).then(function(foodDetail) {
      console.log('刷新收藏状态成功:', foodDetail);
      
      // 处理后端的蛇形命名（is_collected -> isCollected）
      var isCollected = foodDetail.is_collected !== undefined ? foodDetail.is_collected : foodDetail.isCollected;
      
      console.log('收藏状态:', isCollected);
      
      // 只更新收藏状态，保留其他数据
      self.setData({
        'foodDetail.isCollected': isCollected
      });
    }).catch(function(error) {
      console.error('刷新收藏状态失败:', error);
    });
  },

  // 评分变化
  onRatingChange: function(e) {
    this.setData({
      userRating: e.detail.rating
    });
  },

  // 评论输入
  onCommentInput: function(e) {
    this.setData({
      commentText: e.detail.value
    });
  },

  // 提交评论
  submitComment: function() {
    var self = this;
    if (!this.data.userRating) {
      wx.showToast({
        title: '请先评分',
        icon: 'none'
      });
      return;
    }

    if (!this.data.commentText.trim()) {
      wx.showToast({
        title: '请输入评论内容',
        icon: 'none'
      });
      return;
    }

    try {
      wx.showLoading({ title: '发表中...' });

      // 调用后端API
      api.foods.addComment(self.data.foodId, {
        rating: self.data.userRating,
        content: self.data.commentText
      }).then(function(response) {
        // 添加新评论到列表
        var newComment = {
          id: response.id,
          userAvatar: response.userAvatar || '/images/default-avatar.jpg',
          userNickName: response.userNickName || '我',
          rating: self.data.userRating,
          content: self.data.commentText,
          images: response.images || [],
          likeCount: 0,
          isLiked: false,
          createTime: response.createTime || new Date().toLocaleDateString()
        };

        self.setData({
          'comments.list': [newComment].concat(self.data.comments.list),
          'comments.total': self.data.comments.total + 1,
          userRating: 0,
          commentText: ''
        });

        wx.hideLoading();
        wx.showToast({
          title: '评论成功',
          icon: 'success'
        });
      }).catch(function(error) {
        wx.hideLoading();
        console.error('提交评论失败:', error);
        wx.showToast({
          title: '提交失败',
          icon: 'error'
        });
      });
    } catch (error) {
      wx.hideLoading();
      console.error('提交评论失败:', error);
      wx.showToast({
        title: '提交失败',
        icon: 'error'
      });
    }
  },

  // 切换评论点赞
  toggleCommentLike: function(e) {
    var self = this;
    var commentId = e.currentTarget.dataset.id;
    var comments = this.data.comments.list;
    var commentIndex = comments.findIndex(function(item) { return item.id === commentId; });
    
    if (commentIndex === -1) return;

    try {
      var comment = comments[commentIndex];
      var isLiked = !comment.isLiked;
      var likeCount = isLiked ? (comment.likeCount || 0) + 1 : Math.max((comment.likeCount || 0) - 1, 0);

      // 先更新UI，提供即时反馈
      var updateData = {};
      updateData['comments.list[' + commentIndex + '].isLiked'] = isLiked;
      updateData['comments.list[' + commentIndex + '].likeCount'] = likeCount;
      self.setData(updateData);

      // 调用后端API
      api.foods.likeComment(commentId).then(function(response) {
        console.log('点赞API调用成功:', response);
        // API调用成功，保持当前状态
      }).catch(function(error) {
        console.error('点赞API调用失败:', error);
        // API调用失败，回滚UI状态
        var rollbackData = {};
        rollbackData['comments.list[' + commentIndex + '].isLiked'] = !isLiked;
        rollbackData['comments.list[' + commentIndex + '].likeCount'] = comment.likeCount || 0;
        self.setData(rollbackData);
        
        wx.showToast({
          title: '点赞失败',
          icon: 'error'
        });
      });
    } catch (error) {
      console.error('点赞操作失败:', error);
      wx.showToast({
        title: '操作失败',
        icon: 'error'
      });
    }
  },

  // 加载更多评论
  loadMoreComments: function() {
    if (!this.data.comments.hasMore) return;
    
    var nextPage = this.data.comments.page + 1;
    this.loadComments(nextPage);
  },

  // 分享
  onShareAppMessage: function() {
    return {
      title: '推荐美食：' + this.data.foodDetail.name,
      path: '/pages/detail/detail?id=' + this.data.foodId,
      imageUrl: this.data.foodDetail.images[0]
    };
  }
});


