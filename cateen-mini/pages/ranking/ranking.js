// pages/ranking/ranking.js
var auth = require('../../utils/auth');

var STORAGE_KEY = 'mini_share_posts';
var DEFAULT_AVATAR = '/images/default-avatar.jpg';

function now() {
  return Date.now();
}

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

function ensureComment(comment) {
  var createTime = comment.createTime || now();
  var user = comment.user || {};

  return {
    id: comment.id || ('c_' + createTime + '_' + Math.floor(Math.random() * 1000)),
    content: comment.content || '',
    createTime: createTime,
    displayTime: formatTime(createTime),
    user: {
      nickName: user.nickName || comment.userNickName || '食客',
      avatarUrl: user.avatarUrl || comment.userAvatar || DEFAULT_AVATAR
    }
  };
}

function ensurePost(post) {
  var createTime = post.createTime || now();
  var author = post.author || {};

  return {
    id: post.id || ('post_' + createTime),
    content: post.content || '',
    images: Array.isArray(post.images) ? post.images : [],
    createTime: createTime,
    displayTime: formatTime(createTime),
    author: {
      nickName: author.nickName || '食客',
      avatarUrl: author.avatarUrl || DEFAULT_AVATAR
    },
    likeCount: Number(post.likeCount || 0),
    liked: !!post.liked,
    comments: Array.isArray(post.comments) ? post.comments.map(ensureComment) : []
  };
}

function getSeedPosts() {
  var time = now();

  return [
    {
      id: 'seed_1',
      content: '今天的红烧肉超香，配米饭一绝！',
      images: ['/images/foods/hongshao-pork.jpg'],
      createTime: time - 1000 * 60 * 28,
      author: { nickName: '食客A', avatarUrl: DEFAULT_AVATAR },
      likeCount: 12,
      liked: false,
      comments: [
        {
          id: 'seed_c1',
          content: '看着就很下饭！',
          createTime: time - 1000 * 60 * 25,
          user: { nickName: '食客B', avatarUrl: DEFAULT_AVATAR }
        }
      ]
    },
    {
      id: 'seed_2',
      content: '晚餐来一份宫保鸡丁，酸甜又带点辣。',
      images: ['/images/foods/gongbao-chicken.jpg', '/images/foods/mapo-tofu.jpg'],
      createTime: time - 1000 * 60 * 95,
      author: { nickName: '食客C', avatarUrl: DEFAULT_AVATAR },
      likeCount: 8,
      liked: false,
      comments: []
    }
  ];
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
    commentDrafts: {}
  },

  onLoad: function() {
    this.syncTheme();
    this.syncUser();
    this.loadPosts();
  },

  onShow: function() {
    this.syncTheme();
    this.syncUser();
    this.syncCustomTabBar();
  },

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';
    this.setData({ theme: theme, themeClass: 'theme-' + theme });
  },

  syncCustomTabBar: function() {
    if (typeof this.getTabBar !== 'function') {
      return;
    }

    var tabBar = this.getTabBar();
    if (tabBar && tabBar.updateSelected) {
      tabBar.updateSelected();
    }
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
        var files = (res.tempFiles || []).map(function(file) { return file.tempFilePath; });
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

  savePosts: function(posts) {
    wx.setStorageSync(STORAGE_KEY, posts);
  },

  loadPosts: function() {
    var stored = wx.getStorageSync(STORAGE_KEY);
    var source = Array.isArray(stored) && stored.length ? stored : getSeedPosts();
    var posts = source.map(ensurePost).sort(function(a, b) { return b.createTime - a.createTime; });

    if (!stored || !stored.length) {
      this.savePosts(posts);
    }

    this.setData({ posts: posts, commentDrafts: {} });
  },

  publishPost: function() {
    if (!this.data.canPublish || this.data.publishing) {
      return;
    }

    var content = (this.data.draftText || '').trim();
    var images = this.data.selectedImages.slice();
    var user = this.data.userInfo || {};
    var createTime = now();

    var post = ensurePost({
      id: 'post_' + createTime,
      content: content,
      images: images,
      createTime: createTime,
      author: {
        nickName: user.nickName || '食客',
        avatarUrl: user.avatarUrl || DEFAULT_AVATAR
      },
      likeCount: 0,
      liked: false,
      comments: []
    });

    var posts = [post].concat(this.data.posts);

    this.setData({ publishing: true });
    this.savePosts(posts);

    this.setData({
      posts: posts,
      draftText: '',
      selectedImages: [],
      canPublish: false,
      publishing: false
    });

    wx.showToast({ title: '已发布', icon: 'success' });
  },

  toggleLike: function(e) {
    var postId = e.currentTarget.dataset.id;

    var posts = this.data.posts.map(function(post) {
      if (post.id !== postId) {
        return post;
      }

      var liked = !post.liked;
      var count = post.likeCount + (liked ? 1 : -1);

      return Object.assign({}, post, {
        liked: liked,
        likeCount: count < 0 ? 0 : count
      });
    });

    this.setData({ posts: posts });
    this.savePosts(posts);
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

    if (!target || !target.images || !target.images.length) {
      return;
    }

    wx.previewImage({ current: current, urls: target.images });
  },

  onCommentInput: function(e) {
    var postId = e.currentTarget.dataset.id;
    var value = (e.detail.value || '').slice(0, 120);
    var patch = {};
    patch['commentDrafts.' + postId] = value;
    this.setData(patch);
  },

  submitComment: function(e) {
    var postId = e.currentTarget.dataset.id;
    var text = (this.data.commentDrafts[postId] || '').trim();
    var user = this.data.userInfo || {};

    if (!text) {
      wx.showToast({ title: '请输入评论', icon: 'none' });
      return;
    }

    var createTime = now();
    var comment = ensureComment({
      id: 'c_' + createTime,
      content: text,
      createTime: createTime,
      user: {
        nickName: user.nickName || '食客',
        avatarUrl: user.avatarUrl || DEFAULT_AVATAR
      }
    });

    var posts = this.data.posts.map(function(post) {
      if (post.id !== postId) {
        return post;
      }
      var comments = [comment].concat(post.comments || []);
      return Object.assign({}, post, { comments: comments });
    });

    var patch = { posts: posts };
    patch['commentDrafts.' + postId] = '';
    this.setData(patch);
    this.savePosts(posts);

    wx.showToast({ title: '已评论', icon: 'success' });
  },

  onPullDownRefresh: function() {
    this.loadPosts();
    wx.stopPullDownRefresh();
  },

  onShareAppMessage: function() {
    return {
      title: '食友分享',
      path: '/pages/ranking/ranking',
      imageUrl: '/images/foods/hongshao-pork.jpg'
    };
  }
});
