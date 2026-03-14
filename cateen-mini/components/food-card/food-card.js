// components/food-card/food-card.js
Component({
  properties: {
    foodData: {
      type: Object,
      value: {}
    },
    theme: {
      type: String,
      value: 'light'
    }
  },

  methods: {
    onCardTap: function() {
      this.triggerEvent('cardtap', {
        foodId: this.properties.foodData.id,
        foodData: this.properties.foodData
      });
    },

    onCollectTap: function() {
      this.triggerEvent('collecttap', {
        foodId: this.properties.foodData.id,
        isCollected: !!this.properties.foodData.isCollected
      });
    },

    stopPropagation: function() {}
  }
});
