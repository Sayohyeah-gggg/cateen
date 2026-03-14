// pages/ranking/ranking.js
var auth = require('../../utils/auth');
var api = require('../../utils/api');

var DEFAULT_AVATAR = '/images/default-avatar.jpg';

function formatTime(dateStr) {
  if (!dateStr) return '';
  var date = new Date(dateStr);
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

/** 将后端返回的帖子数据规范化为前端格式（兼容驼峰和下划线字段） */
function normalizePost(item) {
  var comments = (item.comments || []).map(function(c) {
    return {
      id: c.id,
      content: c.content,
      displayTime: formatTime(c.created_at || c.createdAt),
      user: {
        nickName: c.user_nickname || c.userNickname || '食客',
        avatarUrl: c.user_avatar || c.userAvatar || DEFAULT_AVATAR
      }
    };
  });

  // 图片列表：兼容 image_list（下划线）和 imageList（驼峰）
  var imageList = item.image_list || item.imageList;

  return {
    id: item.id,
    content: item.content || '',
    images: Array.isArray(imageList) ? imageList : [],
    displayTime: formatTime(item.created_at || item.createdAt),
    author: {
      nickName: item.user_nickname || item.userNickname || '食客',
      avatarUrl: item.user_avatar || item.userAvatar || DEFAULT_AVATAR
    },
    likeCount: Number(item.like_count != null ? item.like_count : (item.likeCount || 0)),
    liked: !!(item.liked),
    commentCount: Number(item.comment_count != null ? item.comment_count : (item.commentCount || 0)),
    comments: comments,
    commentsLoaded: false
  };
}

Page({
  data: {
    theme: 'light',
    themeClass: 'theme-light',

    isLoggedIn: false,
    userInfo: null,

    posts: [],
    total: 0,
    page: 1,
    limit: 20,
    loading: false,
    noMore: false,

    commentDrafts: {}
  },

  onLoad: function() {
    this.syncTheme();
    this.syncUser();
    this.loadPosts(true);
  },

  onShow: function() {
    this.syncTheme();
    this.syncUser();
    this.syncCustomTabBar();
    // 从发布页返回后刷新列表
    if (this._needRefresh) {
      this._needRefresh = false;
      this.loadPosts(true);
      wx.showToast({ title: '发布成功', icon: 'success' });
    }
  },

  onPullDownRefresh: function() {
    this.loadPosts(true).finally(function() {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom: function() {
    if (!this.data.noMore && !this.data.loading) {
      this.loadPosts(false);
    }
  },

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';
    this.setData({ theme: theme, themeClass: 'theme-' + theme });
  },

  syncCustomTabBar: function() {
    if (typeof this.getTabBar !== 'function') return;
    var tabBar = this.getTabBar();
    if (tabBar && tabBar.updateSelected) tabBar.updateSelected();
  },

  syncUser: function() {
    this.setData({
      isLoggedIn: auth.isLoggedIn(),
      userInfo: auth.getCurrentUser()
    });
  },

  /** 跳转到发布页 */
  loadPosts: function(refresh) {
    var self = this;
    if (this.data.loading) return Promise.resolve();

    var page = refresh ? 1 : this.data.page;
    this.setData({ loading: true });

    return api.forum.getPosts({ page: page, limit: this.data.limit })
      .then(function(result) {
        var list = (result.list || []).map(normalizePost);
        var posts = refresh ? list : self.data.posts.concat(list);
        var total = result.total || 0;

        self.setData({
          posts: posts,
          total: total,
          page: page + 1,
          noMore: posts.length >= total,
          loading: false
        });
      })
      .catch(function(err) {
        console.error('加载帖子失败:', err);
        self.setData({ loading: false });
        wx.showToast({ title: '加载失败', icon: 'none' });
      });
  },

  /** 跳转到发布页 */
  goPublish: function() {
    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    var self = this;
    // 重置刷新标志
    this._needRefresh = false;
    wx.navigateTo({
      url: '/pages/publish/publish'
    });
  },

  /** 点赞/取消点赞 */
  toggleLike: function(e) {
    var self = this;
    var postId = e.currentTarget.dataset.id;

    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    // 乐观更新 UI
    var posts = this.data.posts.map(function(post) {
      if (post.id !== postId) return post;
      var liked = !post.liked;
      return Object.assign({}, post, {
        liked: liked,
        likeCount: post.likeCount + (liked ? 1 : -1)
      });
    });
    this.setData({ posts: posts });

    api.forum.toggleLike(postId).catch(function(err) {
      console.error('点赞失败:', err);
      // 回滚
      self.loadPosts(true);
    });
  },

  previewPostImage: function(e) {
    var postId = e.currentTarget.dataset.postId;
    var current = e.currentTarget.dataset.current;
    var target = null;

    for (var i = 0; i < this.data.posts.length; i++) {
      if (this.data.posts[i].id === postId) {
        target = this.data.posts[i];
        break;
      }
    }

    if (!target || !target.images || !target.images.length) return;
    wx.previewImage({ current: current, urls: target.images });
  },

  onCommentInput: function(e) {
    var postId = e.currentTarget.dataset.id;
    var value = (e.detail.value || '').slice(0, 120);
    var patch = {};
    patch['commentDrafts.' + postId] = value;
    this.setData(patch);
  },

  /** 提交评论 */
  submitComment: function(e) {
    var self = this;
    var postId = e.currentTarget.dataset.id;
    var text = (this.data.commentDrafts[postId] || '').trim();

    if (!text) {
      wx.showToast({ title: '请输入评论', icon: 'none' });
      return;
    }

    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    var user = this.data.userInfo || {};

    // 乐观更新
    var tempComment = {
      id: 'temp_' + Date.now(),
      content: text,
      displayTime: '',
      user: {
        nickName: user.nickName || '食客',
        avatarUrl: user.avatarUrl || DEFAULT_AVATAR
      }
    };

    var posts = this.data.posts.map(function(post) {
      if (post.id !== postId) return post;
      return Object.assign({}, post, {
        comments: (post.comments || []).concat([tempComment]),
        commentCount: post.commentCount + 1
      });
    });

    var patch = { posts: posts };
    patch['commentDrafts.' + postId] = '';
    this.setData(patch);

    api.forum.createComment(postId, text)
      .then(function() {
        wx.showToast({ title: '评论成功', icon: 'success' });
      })
      .catch(function(err) {
        console.error('评论失败:', err);
        wx.showToast({ title: '评论失败', icon: 'none' });
        // 回滚
        self.loadPosts(true);
      });
  },

  onShareAppMessage: function() {
    return {
      title: '食友分享',
      path: '/pages/ranking/ranking',
      imageUrl: '/images/foods/hongshao-pork.jpg'
    };
  }
});
