// data/mock.js

var FOODS = [
  {
    id: '1',
    name: '宫保鸡丁',
    category: '菜肴',
    images: ['/images/foods/gongbao-chicken.jpg'],
    description: '经典又下饭的川味炒菜。',
    avgRating: 4.8,
    ratingCount: 256,
    tags: ['经典', '微辣'],
    isCollected: false,
    price: 18
  },
  {
    id: '2',
    name: '麻婆豆腐',
    category: '菜肴',
    images: ['/images/foods/mapo-tofu.jpg'],
    description: '麻辣鲜香，豆腐细嫩。',
    avgRating: 4.7,
    ratingCount: 198,
    tags: ['麻辣'],
    isCollected: false,
    price: 16
  },
  {
    id: '3',
    name: '红烧肉',
    category: '菜肴',
    images: ['/images/foods/hongshao-pork.jpg'],
    description: '入口即化的红烧五花。',
    avgRating: 4.9,
    ratingCount: 312,
    tags: ['招牌'],
    isCollected: false,
    price: 22
  },
  {
    id: '4',
    name: '西湖牛肉羹',
    category: '汤羹',
    images: ['/images/foods/xihu-beef-soup.jpg'],
    description: '清爽暖胃的一碗汤。',
    avgRating: 4.6,
    ratingCount: 146,
    tags: ['清淡'],
    isCollected: false,
    price: 14
  },
  {
    id: '5',
    name: '扬州炒饭',
    category: '主食',
    images: ['/images/foods/yangzhou-rice.jpg'],
    description: '粒粒分明，搭配百搭。',
    avgRating: 4.5,
    ratingCount: 133,
    tags: ['主食'],
    isCollected: false,
    price: 15
  },
  {
    id: '6',
    name: '小笼包',
    category: '小吃',
    images: ['/images/foods/xiaolongbao.jpg'],
    description: '汤汁饱满，一口爆汁。',
    avgRating: 4.7,
    ratingCount: 175,
    tags: ['点心'],
    isCollected: false,
    price: 12
  }
];

var mockData = {
  categories: [
    { id: 'all', name: '全部', code: 'all', icon: '/styles/icon/all.svg', foodCount: FOODS.length },
    { id: 'zhushi', name: '主食', code: 'zhushi', icon: '/styles/icon/mifan-tianchong.svg', foodCount: 1 },
    { id: 'caiyao', name: '菜肴', code: 'caiyao', icon: '/styles/icon/caiyaox.svg', foodCount: 3 },
    { id: 'tanggeng', name: '汤羹', code: 'tanggeng', icon: '/styles/icon/tanggeng.svg', foodCount: 1 },
    { id: 'xiaochi', name: '小吃', code: 'xiaochi', icon: '/styles/icon/xiaochi.svg', foodCount: 1 },
    { id: 'yinpin', name: '饮品', code: 'yinpin', icon: '/styles/icon/yinpin.svg', foodCount: 0 }
  ],
  foods: FOODS,
  luckyFoods: FOODS,
  todayRecommendations: FOODS.slice(0, 3),
  comments: [
    {
      id: 'c1',
      foodId: '1',
      userNickName: '食客A',
      userAvatar: '/images/default-avatar.jpg',
      rating: 5,
      content: '非常下饭，味道赞！',
      images: [],
      likeCount: 12,
      isLiked: false,
      createTime: '2026-03-13 10:20:00'
    }
  ],
  collections: [
    {
      id: 'col1',
      userId: 'u1',
      foodId: '3',
      food: FOODS[2],
      createTime: '2026-03-13'
    }
  ],
  hotKeywords: [
    { keyword: '红烧肉', searchCount: 120 },
    { keyword: '宫保鸡丁', searchCount: 108 },
    { keyword: '麻婆豆腐', searchCount: 102 }
  ]
};

module.exports = mockData;
