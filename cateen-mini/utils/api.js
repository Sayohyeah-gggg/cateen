// API接口工具函数 - 小程序端
var config = require('./config');

// 请求封装
function request(url, options) {
  options = options || {};
  return new Promise(function(resolve, reject) {
    var token = wx.getStorageSync('token');
    
    console.log('API请求:', {
      url: config.BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {}
    });
    
    // 处理请求数据
    var requestData = options.data || {};
    var requestHeaders = Object.assign({
      'Authorization': token ? 'Bearer ' + token : ''
    }, options.header || {});
    
    // 对于POST/PUT请求，需要特殊处理数据格式
    if (options.method === 'POST' || options.method === 'PUT') {
      requestHeaders['Content-Type'] = 'application/json';
      console.log('POST/PUT请求数据:', requestData);
      console.log('POST/PUT请求数据类型:', typeof requestData);
    }
    
    // 处理GET请求的URL参数
    var finalUrl = config.BASE_URL + url;
    var finalData = requestData;
    
    if ((options.method || 'GET') === 'GET' && requestData && Object.keys(requestData).length > 0) {
      // 将参数拼接到URL中
      var queryParams = [];
      for (var key in requestData) {
        if (requestData[key] !== undefined && requestData[key] !== null) {
          queryParams.push(encodeURIComponent(key) + '=' + encodeURIComponent(requestData[key]));
        }
      }
      if (queryParams.length > 0) {
        finalUrl += (url.indexOf('?') > -1 ? '&' : '?') + queryParams.join('&');
      }
      finalData = {}; // GET请求不需要body数据
    }
    
    console.log('最终请求URL:', finalUrl);
    console.log('最终请求数据:', finalData);
    
    wx.request({
      url: finalUrl,
      method: options.method || 'GET',
      data: finalData,
      header: requestHeaders,
      success: function(res) {
        if (res.statusCode === 200) {
          if (res.data.code === 200) {
            // 处理分页数据转换
            var data = res.data.data;
            if (data && typeof data === 'object' && data.records) {
              // MyBatis-Plus Page对象转换为前端期望的格式
              var convertedData = {
                list: data.records || [],
                hasMore: data.current < data.pages,
                total: data.total || 0,
                current: data.current || 1,
                pages: data.pages || 1
              };
              resolve(convertedData);
            } else {
              resolve(data);
            }
          } else {
            wx.showToast({
              title: res.data.message || '请求失败',
              icon: 'none'
            });
            reject(res.data);
          }
        } else if (res.statusCode === 401) {
          // Token过期，静默处理，让调用方决定如何处理
          wx.removeStorageSync('token');
          reject({ code: 401, message: 'Token已过期', statusCode: 401 });
        } else {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: function(error) {
        wx.showToast({
          title: '网络连接失败',
          icon: 'none'
        });
        reject(error);
      }
    });
  });
}

// API接口定义
var api = {
  // 用户认证
  auth: {
    login: function(data) { return request('/auth/login', { method: 'POST', data }); },
    refresh: function() { return request('/auth/refresh', { method: 'POST' }); }
  },

  // 用户管理
  user: {
    getProfile: function() { return request('/user/profile'); },
    updateProfile: function(data) { return request('/user/profile', { method: 'PUT', data }); },
    getCollections: function(params) {
      // 转换分页参数名称
      var pageSize = params.pageSize;
      var otherParams = {};
      for (var key in params) {
        if (key !== 'pageSize') {
          otherParams[key] = params[key];
        }
      }
      return request('/user/collections', { 
        data: Object.assign({ 
          page: params.page || 1, 
          limit: pageSize || 20
        }, otherParams)
      });
    },
    addCollection: function(foodId) { 
      console.log('addCollection API调用，foodId:', foodId);
      console.log('foodId类型:', typeof foodId);
      console.log('foodId值:', JSON.stringify(foodId));
      
      var requestData = { foodId: foodId };
      console.log('发送的数据对象:', requestData);
      console.log('发送的数据JSON:', JSON.stringify(requestData));
      
      // 同时通过URL参数和Body发送数据
      return request('/user/collections?foodId=' + encodeURIComponent(foodId), { method: 'POST', data: requestData }); 
    },
    removeCollection: function(foodId) { return request('/user/collections/' + foodId, { method: 'DELETE' }); },
    getComments: function(params) {
      // 转换分页参数名称
      var pageSize = params.pageSize;
      var otherParams = {};
      for (var key in params) {
        if (key !== 'pageSize') {
          otherParams[key] = params[key];
        }
      }
      return request('/user/comments', { 
        data: Object.assign({ 
          page: params.page || 1, 
          limit: pageSize || 20
        }, otherParams)
      });
    }
  },

  // 美食信息
  foods: {
    getList: function(params) {
      // 转换分页参数名称
      var pageSize = params.pageSize;
      var otherParams = {};
      for (var key in params) {
        if (key !== 'pageSize') {
          otherParams[key] = params[key];
        }
      }
      return request('/foods', { 
        data: Object.assign({ 
          page: params.page || 1, 
          limit: pageSize || 20
        }, otherParams)
      });
    },
    getDetail: function(foodId) { return request('/foods/' + foodId); }, // 后端使用{id}，但路径参数名不影响实际调用
    getCategories: function() { return request('/foods/categories'); },
    getHotKeywords: function() { return request('/foods/hot-keywords'); },
    getComments: function(foodId, params) {
      // 转换分页参数名称
      var pageSize = params.pageSize;
      var otherParams = {};
      for (var key in params) {
        if (key !== 'pageSize') {
          otherParams[key] = params[key];
        }
      }
      return request('/foods/' + foodId + '/comments', { 
        data: Object.assign({ 
          page: params.page || 1, 
          limit: pageSize || 20
        }, otherParams)
      });
    },
    addComment: function(foodId, data) { return request('/foods/' + foodId + '/comments', { method: 'POST', data }); },
    likeComment: function(commentId) { return request('/comments/' + commentId + '/like', { method: 'POST' }); },
    deleteComment: function(commentId) { return request('/comments/' + commentId, { method: 'DELETE' }); }
  },

  // 排行榜
  ranking: {
    getRanking: function(params) { return request('/ranking', { data: params }); }
  },

  // 幸运转盘
  lucky: {
    getFoods: function() { return request('/lucky/foods'); },
    draw: function() { return request('/lucky/draw', { method: 'POST' }); }
  },

  // 文件上传
  upload: {
    image: function(filePath, type) {
      type = type || 'comment';
      return new Promise(function(resolve, reject) {
        var token = wx.getStorageSync('token');
        
        wx.uploadFile({
          url: config.BASE_URL + '/upload/image',
          filePath: filePath,
          name: 'file',
          formData: { type: type },
          header: {
            'Authorization': token ? 'Bearer ' + token : ''
          },
          success: function(res) {
            var data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data);
            } else {
              reject(data);
            }
          },
          fail: reject
        });
      });
    }
  },

  // AI接口
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

  // 系统配置
  config: {
    getAppConfig: function() { return request('/config/app'); }
  },

};

module.exports = api;









