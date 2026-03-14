// components/ai-assistant-button/ai-assistant-button.js
Component({
  properties: {
    visible: { type: Boolean, value: true },
    showTip: { type: Boolean, value: true }
  },

  data: {
    isDragging: false
  },

  methods: {
    onTouchStart: function() {
      this.setData({ isDragging: true });
    },
    onTouchMove: function() {},
    onTouchEnd: function() {
      this.setData({ isDragging: false });
    },
    onAssistantTap: function() {
      this.triggerEvent('assistanttap');
    },
    onLongPress: function() {
      this.triggerEvent('hideassistant');
    }
  }
});
