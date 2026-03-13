// 通用工具函数

/**
 * 格式化时间
 * @param {Date|string|number} date 日期
 * @param {string} format 格式化字符串
 * @returns {string} 格式化后的时间字符串
 */
function formatTime(date, format) {
  if (format === undefined) format = 'YYYY-MM-DD HH:mm:ss';
  if (!date) return '';
  
  var d = new Date(date);
  var year = d.getFullYear();
  var month = String(d.getMonth() + 1).padStart(2, '0');
  var day = String(d.getDate()).padStart(2, '0');
  var hour = String(d.getHours()).padStart(2, '0');
  var minute = String(d.getMinutes()).padStart(2, '0');
  var second = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second);
}

/**
 * 获取相对时间
 * @param {Date|string|number} date 日期
 * @returns {string} 相对时间字符串
 */
function getRelativeTime(date) {
  if (!date) return '';
  
  var now = new Date();
  var target = new Date(date);
  var diff = now - target;
  
  var minute = 60 * 1000;
  var hour = minute * 60;
  var day = hour * 24;
  var month = day * 30;
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  } else if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  } else if (diff < day * 7) {
    return Math.floor(diff / day) + '天前';
  } else if (diff < month) {
    return Math.floor(diff / (day * 7)) + '周前';
  } else {
    return formatTime(date, 'YYYY-MM-DD');
  }
}

/**
 * 防抖函数
 * @param {Function} func 要防抖的函数
 * @param {number} delay 延迟时间
 * @returns {Function} 防抖后的函数
 */
function debounce(func, delay) {
  delay = delay || 300;
  var timer;
  return function() {
    var args = Array.prototype.slice.call(arguments);
    clearTimeout(timer);
    timer = setTimeout(function() {
      func.apply(this, args);
    }.bind(this), delay);
  };
}

/**
 * 节流函数
 * @param {Function} func 要节流的函数
 * @param {number} delay 间隔时间
 * @returns {Function} 节流后的函数
 */
function throttle(func, delay) {
  delay = delay || 300;
  var timer;
  return function() {
    var args = Array.prototype.slice.call(arguments);
    if (timer) return;
    timer = setTimeout(function() {
      func.apply(this, args);
      timer = null;
    }.bind(this), delay);
  };
}

/**
 * 深拷贝对象
 * @param {any} obj 要拷贝的对象
 * @returns {any} 拷贝后的对象
 */
function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj);
  if (obj instanceof Array) return obj.map(function(item) { return deepClone(item); });
  if (typeof obj === 'object') {
  var clonedObj = {};
  for (var key in obj) {
    if (obj.hasOwnProperty(key)) {
      clonedObj[key] = deepClone(obj[key]);
    }
  }
    return clonedObj;
  }
}

/**
 * 获取图片临时URL
 * @param {string} url 图片URL
 * @returns {string} 处理后的URL
 */
function getImageUrl(url) {
  if (!url) return '/images/foods/gongbao-chicken.jpg';
  if (url.startsWith('http') || url.startsWith('/')) return url;
  return '/images/' + url;
}

/**
 * 验证手机号
 * @param {string} phone 手机号
 * @returns {boolean} 是否有效
 */
function validatePhone(phone) {
  var regex = /^1[3-9]\d{9}$/;
  return regex.test(phone);
}

/**
 * 验证邮箱
 * @param {string} email 邮箱
 * @returns {boolean} 是否有效
 */
function validateEmail(email) {
  var regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return regex.test(email);
}

/**
 * 获取文件扩展名
 * @param {string} filename 文件名
 * @returns {string} 扩展名
 */
function getFileExt(filename) {
  return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的大小
 */
function formatFileSize(bytes) {
  if (bytes === 0) return '0 B';
  var k = 1024;
  var sizes = ['B', 'KB', 'MB', 'GB'];
  var i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * 生成随机字符串
 * @param {number} length 长度
 * @returns {string} 随机字符串
 */
function generateRandomString(length) {
  if (length === undefined) length = 8;
  var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var result = '';
  for (var i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

/**
 * 获取系统信息
 * @returns {Object} 系统信息
 */
function getSystemInfo() {
  try {
    return wx.getSystemInfoSync();
  } catch (error) {
    console.error('获取系统信息失败:', error);
    return {};
  }
}

/**
 * 保存数据到本地存储
 * @param {string} key 键名
 * @param {any} data 数据
 * @returns {boolean} 是否成功
 */
function setStorage(key, data) {
  try {
    wx.setStorageSync(key, data);
    return true;
  } catch (error) {
    console.error('保存数据失败:', error);
    return false;
  }
}

/**
 * 从本地存储获取数据
 * @param {string} key 键名
 * @param {any} defaultValue 默认值
 * @returns {any} 数据
 */
function getStorage(key, defaultValue) {
  if (defaultValue === undefined) defaultValue = null;
  try {
    var data = wx.getStorageSync(key);
    return data !== '' ? data : defaultValue;
  } catch (error) {
    console.error('获取数据失败:', error);
    return defaultValue;
  }
}

/**
 * 删除本地存储数据
 * @param {string} key 键名
 * @returns {boolean} 是否成功
 */
function removeStorage(key) {
  try {
    wx.removeStorageSync(key);
    return true;
  } catch (error) {
    console.error('删除数据失败:', error);
    return false;
  }
}

module.exports = {
  formatTime,
  getRelativeTime,
  debounce,
  throttle,
  deepClone,
  getImageUrl,
  validatePhone,
  validateEmail,
  getFileExt,
  formatFileSize,
  generateRandomString,
  getSystemInfo,
  setStorage,
  getStorage,
  removeStorage
};


