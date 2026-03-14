// pages/publish/publish.js
var auth = require('../../utils/auth');
var api = require('../../utils/api');

Page({
  data: {
    theme: 'light',
    themeClass: 'theme-light',
    userInfo: null,
    draftText: '',
    selectedImages: [],
    canPublish: false,
    publishing: false
  },

  onLoad: function() {
    this.syncTheme();
    this.setData({
      userInfo: auth.getCurrentUser()
    });
  },

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';
    this.setData({ theme: theme, themeClass: 'theme-' + theme });
  },

  onDraftInput: function(e) {
    this.setData({ draftText: e.detail.value || '' });
    this.updatePublishState();
  },

  updatePublishState: function() {
    var content = (this.data.draftText || '').trim();
    var hasImage = this.data.selectedImages.length > 0;
    this.setData({ canPublish: !!content || hasImage });
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

  removeImage: function(e) {
    var index = Number(e.currentTarget.dataset.index);
    var images = this.data.selectedImages.slice();
    images.splice(index, 1);
    this.setData({ selectedImages: images });
    this.updatePublishState();
  },

  previewImage: function(e) {
    var current = e.currentTarget.dataset.current;
    wx.previewImage({ current: current, urls: this.data.selectedImages });
  },

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

  publishPost: function() {
    var self = this;
    if (!this.data.canPublish || this.data.publishing) return;

    if (!auth.isLoggedIn()) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    var content = (this.data.draftText || '').trim();
    var localImages = this.data.selectedImages.slice();

    this.setData({ publishing: true });
    wx.showLoading({ title: '发布中...' });

    this.uploadImages(localImages)
      .then(function(imageUrls) {
        return api.forum.createPost({ content: content, images: imageUrls });
      })
      .then(function() {
        wx.hideLoading();
        // 通知上一页刷新
        var pages = getCurrentPages();
        var prevPage = pages[pages.length - 2];
        if (prevPage) {
          // 标记需要刷新
          prevPage._needRefresh = true;
          // 立即调用刷新方法（如果存在）
          if (typeof prevPage.loadPosts === 'function') {
            prevPage._needRefresh = false;
            prevPage.loadPosts(true).then(function() {
              wx.showToast({ title: '发布成功', icon: 'success' });
            });
          }
        } else {
          wx.showToast({ title: '发布成功', icon: 'success' });
        }
        // 延迟返回，让用户看到提示
        setTimeout(function() {
          wx.navigateBack();
        }, 500);
      })
      .catch(function(err) {
        wx.hideLoading();
        console.error('发布失败:', err);
        self.setData({ publishing: false });
        wx.showToast({ title: err.message || '发布失败', icon: 'none' });
      });
  }
});
