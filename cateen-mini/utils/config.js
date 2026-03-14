// utils/config.js

var ENVIRONMENTS = {
  local: 'http://localhost:8080/api/mini',
  lan: 'http://192.168.10.4:8080/api/mini',
  prod: 'https://your-domain.com/api/mini'
};

function getMode() {
  var mode = wx.getStorageSync('API_ENV') || 'lan';
  if (!ENVIRONMENTS[mode]) {
    mode = 'lan';
  }
  return mode;
}

function setMode(mode) {
  if (!ENVIRONMENTS[mode]) {
    return false;
  }

  wx.setStorageSync('API_ENV', mode);
  return true;
}

function getBaseUrl() {
  return ENVIRONMENTS[getMode()];
}

module.exports = {
  BASE_URL: getBaseUrl(),
  ENVIRONMENTS: ENVIRONMENTS,
  getMode: getMode,
  setMode: setMode,
  getBaseUrl: getBaseUrl
};
