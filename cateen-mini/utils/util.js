// utils/util.js

function pad2(value) {
  return value < 10 ? '0' + value : '' + value;
}

function formatTime(date, format) {
  var fmt = format || 'YYYY-MM-DD HH:mm:ss';
  if (!date) {
    return '';
  }

  var d = new Date(date);
  return fmt
    .replace('YYYY', d.getFullYear())
    .replace('MM', pad2(d.getMonth() + 1))
    .replace('DD', pad2(d.getDate()))
    .replace('HH', pad2(d.getHours()))
    .replace('mm', pad2(d.getMinutes()))
    .replace('ss', pad2(d.getSeconds()));
}

function getRelativeTime(date) {
  if (!date) {
    return '';
  }

  var now = Date.now();
  var target = new Date(date).getTime();
  var diff = now - target;

  var minute = 60 * 1000;
  var hour = 60 * minute;
  var day = 24 * hour;

  if (diff < minute) {
    return '刚刚';
  }
  if (diff < hour) {
    return Math.floor(diff / minute) + '分钟前';
  }
  if (diff < day) {
    return Math.floor(diff / hour) + '小时前';
  }
  if (diff < day * 7) {
    return Math.floor(diff / day) + '天前';
  }

  return formatTime(date, 'YYYY-MM-DD');
}

function debounce(func, delay) {
  var wait = delay || 300;
  var timer = null;

  return function() {
    var ctx = this;
    var args = arguments;
    clearTimeout(timer);
    timer = setTimeout(function() {
      func.apply(ctx, args);
    }, wait);
  };
}

function throttle(func, delay) {
  var wait = delay || 300;
  var locked = false;

  return function() {
    if (locked) {
      return;
    }

    locked = true;
    var ctx = this;
    var args = arguments;

    setTimeout(function() {
      func.apply(ctx, args);
      locked = false;
    }, wait);
  };
}

function deepClone(obj) {
  if (obj === null || typeof obj !== 'object') {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(function(item) {
      return deepClone(item);
    });
  }

  var next = {};
  for (var key in obj) {
    if (Object.prototype.hasOwnProperty.call(obj, key)) {
      next[key] = deepClone(obj[key]);
    }
  }
  return next;
}

function getImageUrl(url) {
  if (!url) {
    return '/images/foods/gongbao-chicken.jpg';
  }
  if (url.indexOf('http') === 0 || url.indexOf('/') === 0) {
    return url;
  }
  return '/images/' + url;
}

function validatePhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone || '');
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email || '');
}

function getFileExt(filename) {
  var name = filename || '';
  var i = name.lastIndexOf('.');
  if (i === -1) {
    return '';
  }
  return name.slice(i + 1);
}

function formatFileSize(bytes) {
  var value = Number(bytes || 0);
  if (!value) {
    return '0 字节';
  }

  var units = ['字节', 'KB', 'MB', 'GB'];
  var index = Math.floor(Math.log(value) / Math.log(1024));
  var size = value / Math.pow(1024, index);
  return size.toFixed(2) + ' ' + units[index];
}

function generateRandomString(length) {
  var size = length || 8;
  var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var out = '';

  for (var i = 0; i < size; i++) {
    out += chars.charAt(Math.floor(Math.random() * chars.length));
  }

  return out;
}

function getSystemInfo() {
  try {
    return wx.getSystemInfoSync();
  } catch (error) {
    return {};
  }
}

function setStorage(key, data) {
  try {
    wx.setStorageSync(key, data);
    return true;
  } catch (error) {
    return false;
  }
}

function getStorage(key, defaultValue) {
  try {
    var value = wx.getStorageSync(key);
    if (value === '' || value === undefined || value === null) {
      return defaultValue === undefined ? null : defaultValue;
    }
    return value;
  } catch (error) {
    return defaultValue === undefined ? null : defaultValue;
  }
}

function removeStorage(key) {
  try {
    wx.removeStorageSync(key);
    return true;
  } catch (error) {
    return false;
  }
}

module.exports = {
  formatTime: formatTime,
  getRelativeTime: getRelativeTime,
  debounce: debounce,
  throttle: throttle,
  deepClone: deepClone,
  getImageUrl: getImageUrl,
  validatePhone: validatePhone,
  validateEmail: validateEmail,
  getFileExt: getFileExt,
  formatFileSize: formatFileSize,
  generateRandomString: generateRandomString,
  getSystemInfo: getSystemInfo,
  setStorage: setStorage,
  getStorage: getStorage,
  removeStorage: removeStorage
};
