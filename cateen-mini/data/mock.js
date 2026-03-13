// 模拟数据
var mockData = {
  // 美食分类数据
  categories: [
    {
      id: '1',
      name: '全部',
      code: 'all',
      icon: '🍽️',
      foodCount: 0
    },
    {
      id: '2',
      name: '主食类',
      code: 'staple',
      icon: '🍚',
      foodCount: 25
    },
    {
      id: '3',
      name: '菜肴类',
      code: 'dish',
      icon: '🥘',
      foodCount: 35
    },
    {
      id: '4',
      name: '汤羹类',
      code: 'soup',
      icon: '🍲',
      foodCount: 15
    }
  ],

  // 美食列表数据
  foods: [
    {
      id: '1',
      name: '扬州炒饭',
      category: '主食类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '经典扬州炒饭，粒粒分明，配菜丰富，口感层次丰富，是一道经典的中式炒饭。选用优质大米，搭配火腿、鸡蛋、青豆等配菜，色香味俱全。',
      avgRating: 4.8,
      ratingCount: 256,
      tags: ['经典', '美味', '营养', '家常'],
      isCollected: false,
      topComment: {
        id: '1',
        content: '这道炒饭真的很香，配菜搭配很棒！',
        rating: 5,
        likeCount: 25,
        userNickName: '美食达人'
      },
      createTime: '2024-01-10',
      updateTime: '2024-01-15'
    },
    {
      id: '2',
      name: '宫保鸡丁',
      category: '菜肴类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '经典川菜，麻辣鲜香，口感丰富。选用嫩鸡丁配以花生米、干辣椒炒制而成，是川菜中的代表性菜品。',
      avgRating: 4.7,
      ratingCount: 189,
      tags: ['川菜', '麻辣', '经典'],
      isCollected: true,
      topComment: {
        id: '2',
        content: '正宗的川菜味道，麻辣适中',
        rating: 5,
        likeCount: 18,
        userNickName: '川菜爱好者'
      },
      createTime: '2024-01-08',
      updateTime: '2024-01-14'
    },
    {
      id: '3',
      name: '西湖牛肉羹',
      category: '汤羹类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '杭州名菜，汤鲜味美，营养丰富。以牛肉丝、蛋花为主要原料，口感滑嫩，是杭帮菜的经典代表。',
      avgRating: 4.6,
      ratingCount: 167,
      tags: ['杭州菜', '营养', '清淡'],
      isCollected: false,
      topComment: {
        id: '3',
        content: '汤很鲜美，口感很好',
        rating: 4,
        likeCount: 12,
        userNickName: '汤品爱好者'
      },
      createTime: '2024-01-05',
      updateTime: '2024-01-13'
    },
    {
      id: '4',
      name: '红烧肉',
      category: '菜肴类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '传统家常菜，肥而不腻，入口即化。选用五花肉经过红烧工艺制作，色泽红亮，口感香甜。',
      avgRating: 4.5,
      ratingCount: 234,
      tags: ['家常菜', '传统', '香甜'],
      isCollected: false,
      topComment: {
        id: '4',
        content: '肥而不腻，非常好吃',
        rating: 5,
        likeCount: 20,
        userNickName: '红烧肉控'
      },
      createTime: '2024-01-03',
      updateTime: '2024-01-12'
    },
    {
      id: '5',
      name: '小笼包',
      category: '主食类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '上海特色小吃，皮薄汁多，鲜美可口。传统手工制作，每个包子都饱含浓郁的汤汁。',
      avgRating: 4.4,
      ratingCount: 198,
      tags: ['上海菜', '特色', '手工'],
      isCollected: true,
      topComment: {
        id: '5',
        content: '皮薄汁多，非常正宗',
        rating: 5,
        likeCount: 15,
        userNickName: '包子达人'
      },
      createTime: '2024-01-01',
      updateTime: '2024-01-11'
    },
    {
      id: '6',
      name: '麻婆豆腐',
      category: '菜肴类',
      images: ['/images/foods/gongbao-chicken.jpg'],
      description: '经典川菜，麻辣鲜嫩，豆腐滑嫩，配以肉末炒制，是素食者也喜爱的经典菜品。',
      avgRating: 4.3,
      ratingCount: 145,
      tags: ['川菜', '麻辣', '豆腐'],
      isCollected: false,
      topComment: {
        id: '6',
        content: '豆腐很嫩，味道很正',
        rating: 4,
        likeCount: 8,
        userNickName: '豆腐爱好者'
      },
      createTime: '2023-12-28',
      updateTime: '2024-01-10'
    }
  ],

  // 热门搜索词
  hotKeywords: [
    { keyword: '炒饭', searchCount: 156 },
    { keyword: '川菜', searchCount: 134 },
    { keyword: '汤品', searchCount: 98 },
    { keyword: '家常菜', searchCount: 87 },
    { keyword: '小笼包', searchCount: 76 },
    { keyword: '红烧肉', searchCount: 65 }
  ],

  // 评论数据
  comments: [
    {
      id: '1',
      foodId: '1',
      userId: 'user1',
      userNickName: '美食达人',
      userAvatar: '/images/default-avatar.jpg',
      rating: 5,
      content: '这道炒饭真的很香，配菜搭配很棒！颗粒分明，口感很好。',
      images: ['/images/foods/gongbao-chicken.jpg'],
      likeCount: 25,
      isLiked: false,
      createTime: '2024-01-15 14:30:00'
    },
    {
      id: '2',
      foodId: '1',
      userId: 'user2',
      userNickName: '吃货小王',
      userAvatar: '/images/default-avatar.jpg',
      rating: 4,
      content: '口感不错，就是稍微有点咸，整体还是很满意的。',
      images: [],
      likeCount: 12,
      isLiked: true,
      createTime: '2024-01-14 19:20:00'
    },
    {
      id: '3',
      foodId: '2',
      userId: 'user3',
      userNickName: '川菜爱好者',
      userAvatar: '/images/default-avatar.jpg',
      rating: 5,
      content: '正宗的川菜味道，麻辣适中，鸡肉很嫩。',
      images: [],
      likeCount: 18,
      isLiked: false,
      createTime: '2024-01-13 12:15:00'
    }
  ],

  // 用户收藏数据
  collections: [
    {
      id: '1',
      userId: 'currentUser',
      foodId: '2',
      food: {
        id: '2',
        name: '宫保鸡丁',
        category: '菜肴类',
        images: ['/images/foods/gongbao-chicken.jpg'],
        avgRating: 4.7,
        ratingCount: 189
      },
      createTime: '2024-01-14'
    },
    {
      id: '2',
      userId: 'currentUser',
      foodId: '5',
      food: {
        id: '5',
        name: '小笼包',
        category: '主食类',
        images: ['/images/foods/gongbao-chicken.jpg'],
        avgRating: 4.4,
        ratingCount: 198
      },
      createTime: '2024-01-12'
    }
  ],

  // 用户评论历史
  userComments: [
    {
      id: '10',
      foodId: '1',
      food: {
        id: '1',
        name: '扬州炒饭',
        images: ['/images/placeholder.jpg']
      },
      rating: 5,
      content: '非常好吃的炒饭，会再来的！',
      images: [],
      likeCount: 3,
      createTime: '2024-01-15'
    },
    {
      id: '11',
      foodId: '4',
      food: {
        id: '4',
        name: '红烧肉',
        images: ['/images/placeholder.jpg']
      },
      rating: 4,
      content: '味道不错，就是稍微有点甜',
      images: [],
      likeCount: 1,
      createTime: '2024-01-13'
    }
  ],

  // 排行榜数据
  ranking: {
    overall: [
      {
        rank: 1,
        food: {
          id: '1',
          name: '扬州炒饭',
          category: '主食类',
          images: ['/images/foods/gongbao-chicken.jpg'],
          avgRating: 4.8,
          ratingCount: 256,
          description: '经典扬州炒饭，粒粒分明，配菜丰富'
        },
        trend: 'up',
        trendValue: 2
      },
      {
        rank: 2,
        food: {
          id: '2',
          name: '宫保鸡丁',
          category: '菜肴类',
          images: ['/images/foods/gongbao-chicken.jpg'],
          avgRating: 4.7,
          ratingCount: 189,
          description: '经典川菜，麻辣鲜香，口感丰富'
        },
        trend: 'same',
        trendValue: 0
      },
      {
        rank: 3,
        food: {
          id: '3',
          name: '西湖牛肉羹',
          category: '汤羹类',
          images: ['/images/foods/gongbao-chicken.jpg'],
          avgRating: 4.6,
          ratingCount: 167,
          description: '杭州名菜，汤鲜味美，营养丰富'
        },
        trend: 'down',
        trendValue: 1
      }
    ]
  },

  // 幸运转盘美食
  luckyFoods: [
    {
      id: 'lucky1',
      name: '扬州炒饭',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '主食类',
      avgRating: 4.8,
      description: '经典扬州炒饭，粒粒分明'
    },
    {
      id: 'lucky2',
      name: '宫保鸡丁',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.7,
      description: '经典川菜，麻辣鲜香'
    },
    {
      id: 'lucky3',
      name: '西湖牛肉羹',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '汤羹类',
      avgRating: 4.6,
      description: '杭州名菜，汤鲜味美'
    },
    {
      id: 'lucky4',
      name: '红烧肉',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.5,
      description: '传统家常菜，肥而不腻'
    },
    {
      id: 'lucky5',
      name: '小笼包',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '主食类',
      avgRating: 4.4,
      description: '上海特色小吃，皮薄汁多'
    },
    {
      id: 'lucky6',
      name: '麻婆豆腐',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.3,
      description: '经典川菜，麻辣鲜嫩'
    },
    {
      id: 'lucky7',
      name: '糖醋里脊',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.6,
      description: '酸甜可口，外酥内嫩'
    },
    {
      id: 'lucky8',
      name: '蒸蛋羹',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '汤羹类',
      avgRating: 4.5,
      description: '嫩滑细腻，营养丰富'
    },
    {
      id: 'lucky9',
      name: '鱼香肉丝',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.4,
      description: '川菜经典，鱼香味浓'
    },
    {
      id: 'lucky10',
      name: '白切鸡',
      images: ['/images/foods/gongbao-chicken.jpg'],
      category: '菜肴类',
      avgRating: 4.7,
      description: '粤菜代表，清淡鲜美'
    }
  ],

  // 今日推荐
  todayRecommendations: [
    {
      id: '101',
      name: '糖醋里脊',
      images: ['/images/foods/gongbao-chicken.jpg'],
      avgRating: 4.6
    },
    {
      id: '102',
      name: '蒸蛋羹',
      images: ['/images/foods/gongbao-chicken.jpg'],
      avgRating: 4.5
    },
    {
      id: '103',
      name: '鱼香肉丝',
      images: ['/images/foods/gongbao-chicken.jpg'],
      avgRating: 4.4
    }
  ],

  // 应用配置
  appConfig: {
    version: '1.0.0',
    aboutUs: '美食展示评分小程序是一个专注于美食展示、评分和互动的平台。我们致力于为用户提供最优质的美食推荐和交流体验，让每一次用餐都成为美好的回忆。',
    privacyPolicy: '我们非常重视您的隐私保护。在使用我们的服务时，我们可能会收集和使用您的相关信息。我们希望通过本《隐私政策》向您说明，在使用我们的服务时，我们如何收集、使用、储存和分享这些信息，以及我们为您提供的访问、更新、控制和保护这些信息的方式。',
    userAgreement: '欢迎您使用美食展示评分小程序！在您使用我们的服务前，请您仔细阅读并充分理解本《用户协议》中的各项条款。您的使用行为将被视为对本协议的接受，并同意接受本协议各项条款的约束。',
    contact: {
      email: 'cateen@example.com',
      phone: '400-123-4567'
    }
  }
};

module.exports = mockData;
