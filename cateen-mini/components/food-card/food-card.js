// components/food-card/food-card.js
Component({
  properties: {
    foodData: {
      type: Object,
      value: {},
      observer: function(newVal, oldVal) {
        // 确保avgRating是数字类型
        if (newVal && newVal.avgRating !== undefined) {
          newVal.avgRating = Number(newVal.avgRating) || 0;
        }
      }
    }
  },

  methods: {
    // 点击卡片
    onCardTap: function() {
      this.triggerEvent('cardtap', {
        foodId: this.properties.foodData.id,
        foodData: this.properties.foodData
      });
    },

    // 收藏按钮点击
    onCollectTap: function() {
      this.triggerEvent('collecttap', {
        foodId: this.properties.foodData.id,
        isCollected: this.properties.foodData.isCollected
      });
    },

    // 阻止事件冒泡
    stopPropagation: function() {
      // 阻止收藏按钮点击时触发卡片点击事件
    }
  }
});

