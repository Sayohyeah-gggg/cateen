// 用户认证相关工具函数

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
function isLoggedIn() {
  try {
    return wx.getStorageSync('isLoggedIn') || false;
  } catch (error) {
    console.error('检查登录状态失败:', error);
    return false;
  }
}

/**
 * 获取当前用户信息
 * @returns {Object|null} 用户信息
 */
function getCurrentUser() {
  try {
    if (!isLoggedIn()) {
      return null;
    }
    return wx.getStorageSync('userInfo') || null;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return null;
  }
}

/**
 * 设置用户登录状态和信息
 * @param {Object} userInfo 用户信息
 */
function setUserInfo(userInfo) {
  try {
    wx.setStorageSync('isLoggedIn', true);
    wx.setStorageSync('userInfo', userInfo);
  } catch (error) {
    console.error('保存用户信息失败:', error);
  }
}

/**
 * 清除用户登录状态
 */
function clearUserInfo() {
  try {
    wx.removeStorageSync('isLoggedIn');
    wx.removeStorageSync('userInfo');
  } catch (error) {
    console.error('清除用户信息失败:', error);
  }
}

/**
 * 检查是否需要登录，如果未登录则跳转到个人中心
 * @param {string} action 操作描述
 * @returns {boolean} 是否已登录
 */
function requireLogin(action = '此操作') {
  if (!isLoggedIn()) {
    wx.showModal({
      title: '需要登录',
      content: `${action}需要先登录，是否前往登录？`,
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
  return true;
}

module.exports = {
  isLoggedIn,
  getCurrentUser,
  setUserInfo,
  clearUserInfo,
  requireLogin
};



