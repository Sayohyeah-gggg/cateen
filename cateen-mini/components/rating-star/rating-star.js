// components/rating-star/rating-star.js
Component({
  properties: {
    rating: {
      type: Number,
      value: 0
    },
    size: {
      type: String,
      value: 'normal'
    },
    editable: {
      type: Boolean,
      value: false
    }
  },

  data: {
    currentRating: 0
  },

  observers: {
    rating: function(newVal) {
      var numeric = Number(newVal) || 0;
      this.setData({
        currentRating: Math.max(0, Math.min(5, Math.round(numeric)))
      });
    }
  },

  lifetimes: {
    attached: function() {
      var numeric = Number(this.properties.rating) || 0;
      this.setData({
        currentRating: Math.max(0, Math.min(5, Math.round(numeric)))
      });
    }
  },

  methods: {
    onStarTap: function(e) {
      if (!this.properties.editable) {
        return;
      }

      var index = Number(e.currentTarget.dataset.index) || 0;
      var newRating = index + 1;

      this.setData({
        currentRating: newRating
      });

      this.triggerEvent('ratingchange', {
        rating: newRating
      });
    }
  }
});
