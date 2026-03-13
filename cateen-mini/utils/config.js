// 统一管理 API 基础地址与环境切换（小程序端）
// 可通过在小程序“本地存储”里设置 API_ENV 来切换：local | lan | prod
// 例如在控制台执行：wx.setStorageSync('API_ENV', 'local')

var ENVIRONMENTS = {
  cloal: 'http://localhost:8080/api/mini',
  lan: 'http://192.168.182.1:8080/api/mini',
  prod: 'https://your-domain.com/api/mini'
};

function getMode() {
  var mode = wx.getStorageSync('API_ENV') || 'local';
  if (!ENVIRONMENTS[mode]) {
    mode = 'lan';
  }
  return mode;
}

function setMode(mode) {
  if (ENVIRONMENTS[mode]) {
    wx.setStorageSync('API_ENV', mode);
    return true;
  }
  return false;
}

var BASE_URL = ENVIRONMENTS[getMode()];

module.exports = {
  BASE_URL: BASE_URL,
  ENVIRONMENTS: ENVIRONMENTS,
  getMode: getMode,
  setMode: setMode
};
