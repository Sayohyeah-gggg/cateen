// utils/auth.js

function isLoggedIn() {
  try {
    return !!wx.getStorageSync('isLoggedIn');
  } catch (error) {
    console.error('read login state failed:', error);
    return false;
  }
}

function getCurrentUser() {
  try {
    if (!isLoggedIn()) {
      return null;
    }
    return wx.getStorageSync('userInfo') || null;
  } catch (error) {
    console.error('read user failed:', error);
    return null;
  }
}

function setUserInfo(userInfo) {
  try {
    wx.setStorageSync('isLoggedIn', true);
    wx.setStorageSync('userInfo', userInfo || null);
  } catch (error) {
    console.error('save user failed:', error);
  }
}

function clearUserInfo() {
  try {
    wx.removeStorageSync('isLoggedIn');
    wx.removeStorageSync('userInfo');
  } catch (error) {
    console.error('clear user failed:', error);
  }
}

function requireLogin(action) {
  var actionText = action || '完成操作';
  if (isLoggedIn()) {
    return true;
  }

  wx.showModal({
    title: '需要登录',
    content: actionText + ' 需要登录，是否前往“我的”页面？',
    success: function(res) {
      if (res.confirm) {
        wx.switchTab({
          url: '/pages/profile/profile'
        });
      }
    }
  });

  return false;
}

module.exports = {
  isLoggedIn: isLoggedIn,
  getCurrentUser: getCurrentUser,
  setUserInfo: setUserInfo,
  clearUserInfo: clearUserInfo,
  requireLogin: requireLogin
};
