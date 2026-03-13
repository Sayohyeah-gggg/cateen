// components/rating-star/rating-star.js
Component({
  properties: {
    rating: {
      type: Number,
      value: 0,
      observer: function(newVal) {
        // 确保rating始终是数字类型，并正确处理小数
        var numericValue = Number(newVal) || 0;
        console.log('评分星星组件 - 接收到的评分:', newVal, '转换后:', numericValue);
        this.setData({
          currentRating: Math.round(numericValue)
        });
        console.log('评分星星组件 - 最终显示的星星数:', Math.round(numericValue));
      }
    },
    size: {
      type: String,
      value: 'normal' // mini, small, normal, large
    },
    editable: {
      type: Boolean,
      value: false
    }
  },

  data: {
    currentRating: 0
  },


  lifetimes: {
    attached: function() {
      // 初始化时触发observer处理
      var rating = this.properties.rating;
      var numericValue = Number(rating) || 0;
      this.setData({
        currentRating: Math.round(numericValue)
      });
    }
  },

  methods: {
    // 点击星星
    onStarTap: function(e) {
      if (!this.properties.editable) return;
      
      var index = e.currentTarget.dataset.index;
      var newRating = index + 1;
      
      this.setData({
        currentRating: newRating
      });

      // 触发自定义事件
      this.triggerEvent('ratingchange', {
        rating: newRating
      });
    }
  }
});

