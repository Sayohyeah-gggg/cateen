// utils/api.js
var config = require('./config');

function buildQuery(url, data) {
  if (!data) {
    return url;
  }

  var query = [];
  for (var key in data) {
    if (!Object.prototype.hasOwnProperty.call(data, key)) {
      continue;
    }

    var value = data[key];
    if (value === undefined || value === null || value === '') {
      continue;
    }

    query.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
  }

  if (!query.length) {
    return url;
  }

  return url + (url.indexOf('?') > -1 ? '&' : '?') + query.join('&');
}

function normalizePage(data) {
  if (!data || typeof data !== 'object') {
    return data;
  }

  if (Array.isArray(data.records)) {
    return {
      list: data.records,
      hasMore: data.current < data.pages,
      total: data.total || 0,
      current: data.current || 1,
      pages: data.pages || 1
    };
  }

  return data;
}

function request(path, options) {
  options = options || {};

  var method = options.method || 'GET';
  var data = options.data || {};
  var token = wx.getStorageSync('token');

  var headers = Object.assign({}, options.header || {}, {
    Authorization: token ? 'Bearer ' + token : ''
  });

  var url = config.getBaseUrl() + path;
  var body = data;

  if (method === 'GET') {
    url = buildQuery(url, data);
    body = {};
  } else {
    headers['Content-Type'] = 'application/json';
  }

  return new Promise(function(resolve, reject) {
    wx.request({
      url: url,
      method: method,
      data: body,
      header: headers,
      success: function(res) {
        if (res.statusCode === 401) {
          if (!api.isRefreshing && token) {
            api.isRefreshing = true;

            api.auth.refresh().then(function(refreshRes) {
              var newToken = refreshRes.token;
              wx.setStorageSync('token', newToken);
              api.isRefreshing = false;
              api.onRefreshed(newToken);

              var app = getApp();
              if (app && app.globalData) {
                app.globalData.token = newToken;
              }

              var retryHeaders = Object.assign({}, options.header || {}, {
                Authorization: 'Bearer ' + newToken
              });

              wx.request({
                url: url,
                method: method,
                data: body,
                header: retryHeaders,
                success: function(retryRes) {
                  if (retryRes.statusCode === 401) {
                    handleTokenExpired();
                    reject({ code: 401, message: 'Token已过期', statusCode: 401 });
                    return;
                  }

                  if (retryRes.statusCode !== 200) {
                    reject({ code: retryRes.statusCode, message: 'Network error', raw: retryRes });
                    return;
                  }

                  var payload = retryRes.data || {};
                  if (payload.code !== undefined && payload.code !== 200) {
                    reject(payload);
                    return;
                  }

                  resolve(normalizePage(payload.data));
                },
                fail: function(error) {
                  reject(error);
                }
              });
            }).catch(function() {
              api.isRefreshing = false;
              handleTokenExpired();
              reject({ code: 401, message: 'Token刷新失败', statusCode: 401 });
            });
          } else if (api.isRefreshing) {
            api.addRefreshSubscriber(function(newToken) {
              var retryHeaders = Object.assign({}, options.header || {}, {
                Authorization: 'Bearer ' + newToken
              });

              wx.request({
                url: url,
                method: method,
                data: body,
                header: retryHeaders,
                success: function(retryRes) {
                  if (retryRes.statusCode === 401) {
                    handleTokenExpired();
                    reject({ code: 401, message: 'Token已过期', statusCode: 401 });
                    return;
                  }

                  if (retryRes.statusCode !== 200) {
                    reject({ code: retryRes.statusCode, message: 'Network error', raw: retryRes });
                    return;
                  }

                  var payload = retryRes.data || {};
                  if (payload.code !== undefined && payload.code !== 200) {
                    reject(payload);
                    return;
                  }

                  resolve(normalizePage(payload.data));
                },
                fail: function(error) {
                  reject(error);
                }
              });
            });
          } else {
            handleTokenExpired();
            reject({ code: 401, message: 'Token已过期', statusCode: 401 });
          }
          return;
        }

        if (res.statusCode !== 200) {
          reject({ code: res.statusCode, message: 'Network error', raw: res });
          return;
        }

        var payload = res.data || {};
        if (payload.code !== undefined && payload.code !== 200) {
          reject(payload);
          return;
        }

        resolve(normalizePage(payload.data));
      },
      fail: function(error) {
        reject(error);
      }
    });
  });
}

function handleTokenExpired() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('isLoggedIn');
  wx.removeStorageSync('userInfo');

  var app = getApp();
  if (app && app.globalData) {
    app.globalData.token = null;
    app.globalData.userInfo = null;
    app.globalData.isLoggedIn = false;
  }

  var pages = getCurrentPages();
  var currentPage = pages[pages.length - 1];
  if (currentPage && currentPage.data && currentPage.data.isLoggedIn !== undefined) {
    currentPage.setData({ isLoggedIn: false });
  }

  var auth = require('./auth');
  if (auth && auth.clearUserInfo) {
    auth.clearUserInfo();
  }
}

function mapPageParams(params) {
  params = params || {};

  var pageSize = params.pageSize;
  var next = {};

  for (var key in params) {
    if (!Object.prototype.hasOwnProperty.call(params, key)) {
      continue;
    }
    if (key === 'pageSize') {
      continue;
    }
    next[key] = params[key];
  }

  next.page = params.page || 1;
  next.limit = pageSize || params.limit || 20;
  return next;
}

var api = {
  auth: {
    login: function(data) {
      return request('/auth/login', { method: 'POST', data: data });
    },
    refresh: function() {
      return request('/auth/refresh', { method: 'POST' });
    }
  },

  isRefreshing: false,
  refreshSubscribers: [],

  addRefreshSubscriber: function(callback) {
    this.refreshSubscribers.push(callback);
  },

  onRefreshed: function(token) {
    this.refreshSubscribers.forEach(function(callback) {
      callback(token);
    });
    this.refreshSubscribers = [];
  },

  user: {
    getProfile: function() {
      return request('/user/profile');
    },
    updateProfile: function(data) {
      return request('/user/profile', { method: 'PUT', data: data });
    },
    getTasteProfile: function() {
      return request('/user/taste-profile');
    },
    getCollections: function(params) {
      return request('/user/collections', { data: mapPageParams(params) });
    },
    addCollection: function(foodId) {
      return request('/user/collections?foodId=' + encodeURIComponent(foodId), {
        method: 'POST',
        data: { foodId: foodId }
      });
    },
    removeCollection: function(foodId) {
      return request('/user/collections/' + foodId, { method: 'DELETE' });
    },
    getComments: function(params) {
      return request('/user/comments', { data: mapPageParams(params) });
    }
  },

  foods: {
    getList: function(params) {
      return request('/foods', { data: mapPageParams(params) });
    },
    getDetail: function(foodId) {
      return request('/foods/' + foodId);
    },
    getCategories: function() {
      return request('/foods/categories');
    },
    getHotKeywords: function() {
      return request('/foods/hot-keywords');
    },
    getComments: function(foodId, params) {
      return request('/foods/' + foodId + '/comments', { data: mapPageParams(params) });
    },
    addComment: function(foodId, data) {
      return request('/foods/' + foodId + '/comments', { method: 'POST', data: data });
    },
    likeComment: function(commentId) {
      return request('/comments/' + commentId + '/like', { method: 'POST' });
    },
    deleteComment: function(commentId) {
      return request('/comments/' + commentId, { method: 'DELETE' });
    }
  },

  ranking: {
    getRanking: function(params) {
      return request('/ranking', { data: params || {} });
    }
  },

  lucky: {
    getFoods: function() {
      return request('/lucky/foods');
    },
    draw: function() {
      return request('/lucky/draw', { method: 'POST' });
    }
  },

  upload: {
    image: function(filePath, type) {
      var uploadType = type || 'comment';

      return new Promise(function(resolve, reject) {
        var token = wx.getStorageSync('token');

        wx.uploadFile({
          url: config.getBaseUrl() + '/upload/image',          filePath: filePath,
          name: 'file',
          formData: { type: uploadType },
          header: {
            Authorization: token ? 'Bearer ' + token : ''
          },
          success: function(res) {
            try {
              var data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve(data.data);
              } else {
                reject(data);
              }
            } catch (error) {
              reject(error);
            }
          },
          fail: function(error) {
            reject(error);
          }
        });
      });
    },
    video: function(filePath, onProgress) {
      return new Promise(function(resolve, reject) {
        var token = wx.getStorageSync('token');

        var task = wx.uploadFile({
          url: config.getBaseUrl() + '/upload/video',
          filePath: filePath,
          name: 'file',
          header: {
            Authorization: token ? 'Bearer ' + token : ''
          },
          success: function(res) {
            try {
              var data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve(data.data);
              } else {
                reject(data);
              }
            } catch (error) {
              reject(error);
            }
          },
          fail: function(error) {
            reject(error);
          }
        });

        if (typeof onProgress === 'function') {
          task.onProgressUpdate(function(res) {
            onProgress(res.progress, res.totalBytesSent, res.totalBytesExpectedToSend);
          });
        }
      });
    }
  },

  ai: {
    chat: function(message, sessionId) {
      return request('/ai/chat', {
        method: 'POST',
        data: {
          message: message,
          sessionId: sessionId || 'default_session'
        }
      });
    },
    recommend: function(message) {
      return request('/ai/recommend', {
        method: 'POST',
        data: { message: message }
      });
    },
    getStatus: function() {
      return request('/ai/status');
    }
  },

  forum: {
    getPosts: function(params) {
      return request('/forum/posts', { data: mapPageParams(params) });
    },
    createPost: function(data) {
      return request('/forum/posts', { method: 'POST', data: data });
    },
    deletePost: function(postId) {
      return request('/forum/posts/' + postId, { method: 'DELETE' });
    },
    getComments: function(postId, params) {
      return request('/forum/posts/' + postId + '/comments', { data: mapPageParams(params) });
    },
    createComment: function(postId, content) {
      return request('/forum/posts/' + postId + '/comments', { method: 'POST', data: { content: content } });
    },
    toggleLike: function(postId) {
      return request('/forum/posts/' + postId + '/like', { method: 'POST' });
    }
  },

  config: {
    getAppConfig: function() {
      return request('/config/app');
    }
  }
};

module.exports = api;
