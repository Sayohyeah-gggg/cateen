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
    selectedVideo: null, // 选中的视频
    canPublish: false,
    publishing: false,
    mediaType: 'image', // 当前选择类型: image 或 video
    showUploadProgress: false,
    uploadProgress: 0
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
    var hasVideo = !!this.data.selectedVideo;
    this.setData({ canPublish: !!content || hasImage || hasVideo });
  },

  chooseImages: function() {
    var self = this;
    if (this.data.selectedVideo) {
      wx.showToast({ title: '图片和视频只能选择一个', icon: 'none' });
      return;
    }
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
        self.setData({ selectedImages: self.data.selectedImages.concat(files), mediaType: 'image' });
        self.updatePublishState();
      }
    });
  },

  chooseVideo: function() {
    var self = this;
    if (this.data.selectedImages.length > 0) {
      wx.showToast({ title: '图片和视频只能选择一个', icon: 'none' });
      return;
    }
    if (this.data.selectedVideo) {
      wx.showModal({
        title: '提示',
        content: '是否更换视频？',
        success: function(res) {
          if (res.confirm) {
            self._selectVideo();
          }
        }
      });
      return;
    }
    this._selectVideo();
  },

  _selectVideo: function() {
    var self = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['video'],
      sourceType: ['album', 'camera'],
      maxDuration: 60, // 限制60秒
      camera: 'back',
      success: function(res) {
        var videoFile = res.tempFiles && res.tempFiles[0];
        if (videoFile) {
          self.setData({ selectedVideo: videoFile.tempFilePath, mediaType: 'video' });
          self.updatePublishState();
        }
      }
    });
  },

  removeVideo: function() {
    this.setData({ selectedVideo: null, mediaType: 'image' });
    this.updatePublishState();
  },

  previewVideo: function() {
    wx.previewMedia({
      sources: [{
        url: this.data.selectedVideo,
        type: 'video'
      }]
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

  uploadVideo: function(localPath) {
    var self = this;
    if (!localPath) return Promise.resolve(null);

    return new Promise(function(resolve, reject) {
      self.setData({ showUploadProgress: true, uploadProgress: 0 });

      api.upload.video(localPath, function(progress) {
        self.setData({ uploadProgress: Math.min(progress, 99) });
      }).then(function(r) {
        self.setData({ uploadProgress: 100 });
        setTimeout(function() {
          self.setData({ showUploadProgress: false, uploadProgress: 0 });
        }, 500);
        var result = typeof r === 'string' ? r : (r.url || r.videoUrl || r);
        resolve(result);
      }).catch(function(err) {
        self.setData({ showUploadProgress: false, uploadProgress: 0 });
        reject(err);
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
    var localVideo = this.data.selectedVideo;

    this.setData({ publishing: true });
    wx.showLoading({ title: '发布中...' });

    // 根据媒体类型选择上传方式
    var uploadPromise;
    if (localVideo) {
      uploadPromise = this.uploadVideo(localVideo).then(function(videoUrl) {
        return { video: videoUrl };
      });
    } else {
      uploadPromise = this.uploadImages(localImages).then(function(imageUrls) {
        return { images: imageUrls };
      });
    }

    uploadPromise
      .then(function(mediaData) {
        return api.forum.createPost({
          content: content,
          images: mediaData.images || [],
          video: mediaData.video || ''
        });
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
