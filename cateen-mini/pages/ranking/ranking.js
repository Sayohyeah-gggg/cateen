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

    draftText: '',
    selectedImages: [],
    canPublish: false,
    publishing: false,

    posts: [],
    total: 0,
    page: 1,
    limit: 20,
    loading: false,
    noMore: false,

    commentDrafts: {},
    uploadingImages: false
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

  updatePublishState: function() {
    var content = (this.data.draftText || '').trim();
    var hasImage = this.data.selectedImages.length > 0;
    this.setData({ canPublish: !!content || hasImage });
  },

  onDraftInput: function(e) {
    this.setData({ draftText: e.detail.value || '' });
    this.updatePublishState();
  },

  chooseImages: function() {
    var self = this;
    var left = 9 - this.data.selectedImages.length;
    if (left <= 0) {
      wx.showToast({ title: '最多选择9张图片', icon: 'none' });
      return;
    }

    wx.chooseMedia({
      count: left,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        var files = (res.tempFiles || []).map(function(f) { return f.tempFilePath; });
        self.setData({ selectedImages: self.data.selectedImages.concat(files) });
        self.updatePublishState();
      }
    });
  },

  removeSelectedImage: function(e) {
    var index = Number(e.currentTarget.dataset.index);
    var images = this.data.selectedImages.slice();
    images.splice(index, 1);
    this.setData({ selectedImages: images });
    this.updatePublishState();
  },

  previewSelectedImage: function(e) {
    var current = e.currentTarget.dataset.current;
    wx.previewImage({ current: current, urls: this.data.selectedImages });
  },

  /** 上传图片到服务器，返回 URL 数组 */
  uploadImages: function(localPaths) {
    if (!localPaths || !localPaths.length) return Promise.resolve([]);

    var tasks = localPaths.map(function(path) {
      return api.upload.image(path, 'forum');
    });

    return Promise.all(tasks).then(function(results) {
      return results.map(function(r) {
        return typeof r === 'string' ? r : (r.url || r.imageUrl || r);
      });
    });
  },

  /** 加载帖子列表 */
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

  /** 发布帖子 */
  publishPost: function() {
    var self = this;
    if (!this.data.canPublish || this.data.publishing) return;

    if (!this.data.isLoggedIn) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    var content = (this.data.draftText || '').trim();
    var localImages = this.data.selectedImages.slice();

    this.setData({ publishing: true });
    wx.showLoading({ title: '发布中...' });

    // 先上传图片，再发布帖子
    this.uploadImages(localImages)
      .then(function(imageUrls) {
        return api.forum.createPost({ content: content, images: imageUrls });
      })
      .then(function() {
        wx.hideLoading();
        self.setData({
          draftText: '',
          selectedImages: [],
          canPublish: false,
          publishing: false
        });
        wx.showToast({ title: '发布成功', icon: 'success' });
        // 刷新列表
        return self.loadPosts(true);
      })
      .catch(function(err) {
        wx.hideLoading();
        console.error('发布失败:', err);
        self.setData({ publishing: false });
        wx.showToast({ title: err.message || '发布失败', icon: 'none' });
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
