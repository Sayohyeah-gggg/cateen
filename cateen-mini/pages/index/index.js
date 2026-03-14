// pages/index/index.js
var api = require('../../utils/api');

var ICON_MAP = {
  all: '/styles/icon/all.svg',
  zhushi: '/styles/icon/mifan-tianchong.svg',
  caiyao: '/styles/icon/caiyaox.svg',
  tanggeng: '/styles/icon/tanggeng.svg',
  xiaochi: '/styles/icon/xiaochi.svg',
  yinpin: '/styles/icon/yinpin.svg'
};

var CATEGORY_BG_MAP = {
  all: 'linear-gradient(135deg, #2f7fff 0%, #1d62f0 100%)',
  zhushi: 'linear-gradient(135deg, #ff8b5a 0%, #ff6b4a 100%)',
  caiyao: 'linear-gradient(135deg, #36cf84 0%, #22b66e 100%)',
  tanggeng: 'linear-gradient(135deg, #4ca6ff 0%, #2d80e8 100%)',
  xiaochi: 'linear-gradient(135deg, #f6bf3c 0%, #e6a91f 100%)',
  yinpin: 'linear-gradient(135deg, #9073ff 0%, #7357e6 100%)'
};

var FALLBACK_CATEGORIES = [
  { id: 'zhushi', name: '主食', code: 'zhushi' },
  { id: 'caiyao', name: '菜肴', code: 'caiyao' },
  { id: 'tanggeng', name: '汤羹', code: 'tanggeng' },
  { id: 'xiaochi', name: '小吃', code: 'xiaochi' },
  { id: 'yinpin', name: '饮品', code: 'yinpin' }
];

var FALLBACK_POPULAR = [
  { id: 'hot_1', name: '宫保鸡丁', category: '菜肴', avgRating: 4.7, ratingCount: 128, images: ['/images/foods/gongbao-chicken.jpg'] },
  { id: 'hot_2', name: '麻婆豆腐', category: '菜肴', avgRating: 4.6, ratingCount: 113, images: ['/images/foods/mapo-tofu.jpg'] },
  { id: 'hot_3', name: '红烧肉', category: '菜肴', avgRating: 4.8, ratingCount: 152, images: ['/images/foods/hongshao-pork.jpg'] },
  { id: 'hot_4', name: '西湖牛肉羹', category: '汤羹', avgRating: 4.5, ratingCount: 84, images: ['/images/foods/xihu-beef-soup.jpg'] }
];

function toArray(value) {
  if (!value) {
    return [];
  }
  return Array.isArray(value) ? value : [value];
}

function ensureImage(food) {
  if (food.image_url) {
    return [food.image_url];
  }
  if (food.imageUrl) {
    return [food.imageUrl];
  }
  if (food.images && food.images.length) {
    return food.images;
  }
  return ['/images/foods/gongbao-chicken.jpg'];
}

function formatFood(food) {
  var avgRating = Number(food.avgRating || food.avg_rating || food.rating || 0) || 0;
  var ratingCount = Number(food.ratingCount || food.rating_count || 0) || 0;

  return {
    id: food.id,
    name: food.name || '未命名菜品',
    category: food.category || food.category_name || '未分类',
    images: ensureImage(food),
    avgRating: avgRating,
    rating: avgRating,
    ratingCount: ratingCount,
    isCollected: !!(food.isCollected || food.is_collected)
  };
}

function guessCategoryType(category) {
  var fallbackOrder = ['zhushi', 'caiyao', 'tanggeng', 'xiaochi', 'yinpin'];
  var code = String(category.code || category.id || '').toLowerCase().replace(/[_\-\s]/g, '');
  var name = String(category.name || '').toLowerCase().replace(/[_\-\s]/g, '');
  var token = code + ' ' + name;

  if (token.indexOf('all') > -1 || token.indexOf('quanbu') > -1 || category.name === '全部') {
    return 'all';
  }
  if (token.indexOf('zhushi') > -1 || token.indexOf('mifan') > -1 || token.indexOf('staple') > -1 || token.indexOf('main') > -1 || category.name === '主食') {
    return 'zhushi';
  }
  if (token.indexOf('caiyao') > -1 || token.indexOf('caiyaox') > -1 || token.indexOf('dish') > -1 || category.name === '菜肴') {
    return 'caiyao';
  }
  if (token.indexOf('tanggeng') > -1 || token.indexOf('soup') > -1 || token.indexOf('tang') > -1 || category.name === '汤羹') {
    return 'tanggeng';
  }
  if (token.indexOf('xiaochi') > -1 || token.indexOf('snack') > -1 || category.name === '小吃') {
    return 'xiaochi';
  }
  if (token.indexOf('yinpin') > -1 || token.indexOf('drink') > -1 || token.indexOf('beverage') > -1 || category.name === '饮品') {
    return 'yinpin';
  }

  var numberLike = parseInt(code, 10);
  if (!isNaN(numberLike) && numberLike > 0) {
    return fallbackOrder[(numberLike - 1) % fallbackOrder.length];
  }

  var hashSeed = (code + name).length;
  return fallbackOrder[hashSeed % fallbackOrder.length];
}

Page({
  data: {
    theme: 'light',
    themeClass: 'theme-light',

    categories: [],
    currentCategory: 'all',

    searchKeyword: '',

    popularFoods: [],
    foodList: [],

    loading: true,
    refreshing: false,
    loadingMore: false,
    noMore: false,

    page: 1,
    pageSize: 10
  },

  onLoad: function() {
    this.syncTheme();
    this.ensureLoginAndLoadData();
  },

  onShow: function() {
    this.syncTheme();
    this.syncCustomTabBar();
  },

  onUnload: function() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
      this.searchTimer = null;
    }
  },

  syncTheme: function() {
    var app = getApp();
    var theme = app.getCurrentTheme ? app.getCurrentTheme() : 'light';

    this.setData({
      theme: theme,
      themeClass: 'theme-' + theme
    });
  },

  syncCustomTabBar: function() {
    if (typeof this.getTabBar !== 'function') {
      return;
    }

    var tabBar = this.getTabBar();
    if (tabBar && tabBar.updateSelected) {
      tabBar.updateSelected();
    }
  },

  ensureLoginAndLoadData: function() {
    var self = this;
    var app = getApp();

    if (!app.globalData.isLoggedIn && app.wxLogin) {
      app.wxLogin().finally(function() {
        self.bootstrapPage();
      });
      return;
    }

    this.bootstrapPage();
  },

  bootstrapPage: function() {
    this.loadCategories();
    this.loadPopularFoods();
    this.loadFoodList(true);
  },

  normalizeCategories: function(source) {
    var seen = {};
    var result = [{ id: 'all', name: '全部', code: 'all', icon: ICON_MAP.all, bgColor: CATEGORY_BG_MAP.all }];
    var list = source && source.length ? source : FALLBACK_CATEGORIES;

    list.forEach(function(category) {
      var queryCode = category.code || category.id;
      if (!queryCode) {
        return;
      }

      var code = String(queryCode);
      if (seen[code]) {
        return;
      }

      seen[code] = true;
      var type = guessCategoryType(category);

      result.push({
        id: category.id || code,
        name: category.name || '分类',
        code: code,
        icon: ICON_MAP[type] || ICON_MAP.caiyao,
        bgColor: CATEGORY_BG_MAP[type] || CATEGORY_BG_MAP.caiyao
      });
    });

    return result;
  },

  loadCategories: function() {
    var self = this;

    api.foods.getCategories().then(function(list) {
      self.setData({ categories: self.normalizeCategories(list || []) });
    }).catch(function() {
      self.setData({ categories: self.normalizeCategories([]) });
    });
  },

  loadPopularFoods: function() {
    var self = this;

    api.foods.getList({ page: 1, pageSize: 8 }).then(function(result) {
      var list = toArray(result.list).map(formatFood);
      if (!list.length) {
        self.setData({ popularFoods: FALLBACK_POPULAR });
        return;
      }

      list.sort(function(a, b) {
        return (b.avgRating || 0) - (a.avgRating || 0);
      });

      self.setData({ popularFoods: list.slice(0, 5) });
    }).catch(function() {
      self.setData({ popularFoods: FALLBACK_POPULAR });
    });
  },

  buildFoodParams: function() {
    var params = {
      page: this.data.page,
      pageSize: this.data.pageSize
    };

    if (this.data.currentCategory !== 'all') {
      var aliasMap = {
        zhushi: 'staple',
        caiyao: 'dish',
        tanggeng: 'soup',
        xiaochi: 'snack',
        yinpin: 'drink'
      };
      params.category = aliasMap[this.data.currentCategory] || this.data.currentCategory;
    }

    if (this.data.searchKeyword) {
      params.keyword = this.data.searchKeyword;
    }

    return params;
  },

  loadFoodList: function(refresh) {
    var self = this;

    if (!refresh && (this.data.loadingMore || this.data.noMore)) {
      return Promise.resolve();
    }

    if (refresh) {
      this.setData({ page: 1, noMore: false, refreshing: true, loading: true });
    } else {
      this.setData({ loadingMore: true });
    }

    return api.foods.getList(this.buildFoodParams()).then(function(result) {
      var list = toArray(result.list).map(formatFood);
      var merged = refresh ? list : self.data.foodList.concat(list);

      self.setData({
        foodList: merged,
        noMore: !result.hasMore,
        page: refresh ? 2 : self.data.page + 1,
        loading: false,
        refreshing: false,
        loadingMore: false
      });

      if (!self.data.popularFoods.length && merged.length) {
        self.setData({ popularFoods: merged.slice(0, 5) });
      }
    }).catch(function(error) {
      console.error('load food list failed:', error);
      self.setData({ loading: false, refreshing: false, loadingMore: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  handleSearchInput: function(e) {
    var self = this;
    var keyword = (e.detail.value || '').trim();
    this.setData({ searchKeyword: keyword });

    if (this.searchTimer) {
      clearTimeout(this.searchTimer);
    }

    this.searchTimer = setTimeout(function() {
      self.loadFoodList(true);
    }, 350);
  },

  switchCategory: function(e) {
    var code = e.currentTarget.dataset.code;
    if (code === this.data.currentCategory) {
      return;
    }

    this.setData({ currentCategory: code, page: 1, noMore: false });
    this.loadFoodList(true);
  },

  onBannerTap: function(e) {
    var foodId = e.currentTarget.dataset.id;
    if (!foodId) {
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + foodId });
  },

  goToDetail: function(e) {
    var foodId = e.detail.foodId;
    if (!foodId) {
      return;
    }

    wx.navigateTo({ url: '/pages/detail/detail?id=' + foodId });
  },

  toggleCollect: function(e) {
    var self = this;
    var foodId = e.detail.foodId;
    var isCollected = e.detail.isCollected;
    var req = isCollected ? api.user.removeCollection(foodId) : api.user.addCollection(foodId);

    req.then(function() {
      self.updateLocalCollectionData(foodId, isCollected);
    }).catch(function() {
      self.updateLocalCollectionData(foodId, isCollected);
    });
  },

  updateLocalCollectionData: function(foodId, isCollected) {
    var list = this.data.foodList.map(function(food) {
      if (food.id !== foodId) {
        return food;
      }

      var next = Object.assign({}, food);
      next.isCollected = !isCollected;
      return next;
    });

    this.setData({ foodList: list });
    wx.showToast({ title: isCollected ? '已取消收藏' : '已收藏', icon: 'success' });
  },

  onRefresh: function() {
    var self = this;
    Promise.all([this.loadFoodList(true), this.loadPopularFoods()]).finally(function() {
      self.setData({ refreshing: false });
    });
  },

  loadMore: function() {
    if (this.data.loadingMore || this.data.noMore) {
      return;
    }

    this.loadFoodList(false);
  },

  onShareAppMessage: function() {
    return {
      title: '食堂优选',
      path: '/pages/index/index',
      imageUrl: '/images/foods/gongbao-chicken.jpg'
    };
  }
});
