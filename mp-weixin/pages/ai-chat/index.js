const { SseClient, BASE_URL } = require('../../utils/sse-client.js')

Page({
  data: {
    sessionId: '',
    messages: [],
    inputText: '',
    isAiTyping: false,
    scrollTarget: '',
    sseClient: null,
    sendLock: false
  },

  onLoad(options) {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => {
        wx.switchTab({ url: '/pages/my/my' })
      }, 1500)
      return
    }

    if (options.sessionId) {
      this.setData({ sessionId: options.sessionId })
      this._loadHistoryMessages(options.sessionId)
    } else {
      this.setData({ sessionId: this._generateSessionId() })
    }
  },

  onUnload() {
    if (this.data.sseClient) {
      this.data.sseClient.abort()
    }
  },

  _generateSessionId() {
    return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
  },

  _loadHistoryMessages(sessionId) {
    const token = wx.getStorageSync('token')
    wx.request({
      url: BASE_URL + '/user/ai/sessions/' + sessionId + '/messages',
      method: 'GET',
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: (res) => {
        if (res.data && res.data.code === 1 && res.data.data) {
          const msgs = res.data.data.map(m => ({
            role: m.role,
            content: m.content,
            isStreaming: false
          }))
          this.setData({ messages: msgs })
          this._scrollToBottom()
        }
      }
    })
  },

  onInputChange(e) {
    this.setData({ inputText: e.detail.value })
  },

  onQuickAction(e) {
    const action = e.currentTarget.dataset.action
    this.setData({ inputText: action })
    this.onSend()
  },

  onSend() {
    if (this.data.sendLock || this.data.isAiTyping) return
    const text = this.data.inputText.trim()
    if (!text) return

    this.setData({ sendLock: true })
    setTimeout(() => { this.setData({ sendLock: false }) }, 1000)

    const userMsg = { role: 'user', content: text, isStreaming: false }
    const newMessages = this.data.messages.concat([userMsg])
    this.setData({
      messages: newMessages,
      inputText: '',
      isAiTyping: true
    })
    this._scrollToBottom()

    this._connectSSE(text)
  },

  _connectSSE(message) {
    const token = wx.getStorageSync('token')
    const sseClient = new SseClient({
      url: BASE_URL + '/user/ai/chat',
      data: {
        message: message,
        sessionId: this.data.sessionId
      },
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      onChunk: (data) => {
        this._appendChunkContent(data.content || data.text || '')
      },
      onDone: () => {
        this._finishAiMessage()
      },
      onError: (err) => {
        this._finishAiMessage()
        const errMsg = (err && err.message) || 'AI回复异常，请重试'
        const lastMsg = this.data.messages[this.data.messages.length - 1]
        if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.content) {
          const msgs = this.data.messages.slice(0, -1)
          this.setData({ messages: msgs })
        }
        wx.showToast({ title: errMsg, icon: 'none' })
      }
    })

    this.setData({ sseClient: sseClient })
    sseClient.connect()
  },

  _appendChunkContent(chunk) {
    const msgs = this.data.messages
    const lastIdx = msgs.length - 1
    if (lastIdx >= 0 && msgs[lastIdx].role === 'assistant') {
      msgs[lastIdx].content += chunk
      this.setData({ messages: msgs })
    } else {
      const aiMsg = { role: 'assistant', content: chunk, isStreaming: true }
      this.setData({ messages: msgs.concat([aiMsg]) })
    }
    this._scrollToBottom()
  },

  _finishAiMessage() {
    const msgs = this.data.messages
    const lastIdx = msgs.length - 1
    if (lastIdx >= 0 && msgs[lastIdx].role === 'assistant') {
      msgs[lastIdx].isStreaming = false
      this.setData({ messages: msgs, isAiTyping: false })
    } else {
      this.setData({ isAiTyping: false })
    }
  },

  _scrollToBottom() {
    this.setData({ scrollTarget: 'scroll-bottom' })
  }
})
