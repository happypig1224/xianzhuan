const { BASE_URL } = require('../../utils/sse-client.js')

Page({
  data: {
    sessions: [],
    loading: true
  },

  onLoad() {
    this._loadSessions()
  },

  onPullDownRefresh() {
    this._loadSessions()
    wx.stopPullDownRefresh()
  },

  _loadSessions() {
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    this.setData({ loading: true })
    wx.request({
      url: BASE_URL + '/user/ai/sessions',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.code === 1 && res.data.data) {
          this.setData({ sessions: res.data.data, loading: false })
        } else {
          this.setData({ sessions: [], loading: false })
        }
      },
      fail: () => {
        this.setData({ sessions: [], loading: false })
        wx.showToast({ title: '加载失败', icon: 'none' })
      }
    })
  },

  onSessionTap(e) {
    const sessionId = e.currentTarget.dataset.sessionId
    wx.navigateTo({
      url: '/pages/ai-chat/index?sessionId=' + sessionId
    })
  },

  onSessionLongPress(e) {
    const sessionId = e.currentTarget.dataset.sessionId
    const index = e.currentTarget.dataset.index
    wx.showModal({
      title: '删除会话',
      content: '确定删除该对话记录吗？',
      confirmColor: '#FF6B35',
      success: (res) => {
        if (res.confirm) {
          this._deleteSession(sessionId, index)
        }
      }
    })
  },

  _deleteSession(sessionId, index) {
    const token = wx.getStorageSync('token')
    wx.request({
      url: BASE_URL + '/user/ai/sessions/' + sessionId,
      method: 'DELETE',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data && res.data.code === 1) {
          const sessions = this.data.sessions
          sessions.splice(index, 1)
          this.setData({ sessions: sessions })
          wx.showToast({ title: '已删除', icon: 'success' })
        }
      }
    })
  },

  onNewSession() {
    wx.navigateTo({
      url: '/pages/ai-chat/index'
    })
  },

  onBack() {
    wx.navigateBack()
  }
})
