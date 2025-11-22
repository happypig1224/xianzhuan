Component({
  methods: {
    onTap() {
      const token = wx.getStorageSync('token')
      if (!token) {
        wx.showToast({ title: '请先登录', icon: 'none' })
        return
      }
      wx.navigateTo({ url: '/pages/ai-chat/index' })
    }
  }
})
