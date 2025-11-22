Component({
  properties: {
    role: {
      type: String,
      value: 'assistant'
    },
    content: {
      type: String,
      value: ''
    },
    timestamp: {
      type: String,
      value: ''
    },
    isStreaming: {
      type: Boolean,
      value: false
    }
  }
})
