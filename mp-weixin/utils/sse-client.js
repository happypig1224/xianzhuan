const BASE_URL = 'https://localhost:8080'

class SseClient {
  constructor(options) {
    this.url = options.url || ''
    this.data = options.data || {}
    this.header = options.header || {}
    this.onChunk = options.onChunk || function() {}
    this.onDone = options.onDone || function() {}
    this.onError = options.onError || function() {}
    this.requestTask = null
    this.reconnectCount = 0
    this.maxReconnect = 5
    this.reconnectDelay = 1000
    this._buffer = ''
  }

  connect() {
    const token = wx.getStorageSync('token') || ''
    const fullUrl = this.url.startsWith('http') ? this.url : BASE_URL + this.url

    this.requestTask = wx.request({
      url: fullUrl,
      method: 'POST',
      data: this.data,
      header: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Authorization': token ? 'Bearer ' + token : '',
        ...this.header
      },
      enableChunked: true,
      success: () => {
        this.reconnectCount = 0
      },
      fail: (err) => {
        this._tryReconnect()
      }
    })

    if (this.requestTask && this.requestTask.onChunkReceived) {
      this.requestTask.onChunkReceived((res) => {
        const text = this._decodeChunk(res.data)
        this._parseSseEvents(text)
      })
    }
  }

  _decodeChunk(buffer) {
    const uint8Array = new Uint8Array(buffer)
    let text = ''
    for (let i = 0; i < uint8Array.length; i++) {
      text += String.fromCharCode(uint8Array[i])
    }
    try {
      return decodeURIComponent(escape(text))
    } catch (e) {
      return text
    }
  }

  _parseSseEvents(chunkText) {
    this._buffer += chunkText
    const lines = this._buffer.split('\n')
    this._buffer = lines.pop() || ''

    let eventData = ''
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim()
      if (line.startsWith('data:')) {
        eventData += line.substring(5).trimStart()
      } else if (line === '' && eventData) {
        try {
          const parsed = JSON.parse(eventData)
          if (parsed.type === 'done' || parsed.type === 'complete') {
            this.onDone(parsed)
          } else if (parsed.type === 'error') {
            this.onError(parsed)
          } else {
            this.onChunk(parsed)
          }
        } catch (e) {
          if (eventData.trim() === '[DONE]') {
            this.onDone({})
          } else {
            this.onChunk({ content: eventData })
          }
        }
        eventData = ''
      }
    }
  }

  _tryReconnect() {
    if (this.reconnectCount >= this.maxReconnect) {
      this.onError({ message: '连接失败，请检查网络后重试' })
      return
    }
    this.reconnectCount++
    const delay = Math.min(this.reconnectDelay * Math.pow(2, this.reconnectCount - 1), 30000)
    setTimeout(() => {
      this.connect()
    }, delay)
  }

  abort() {
    if (this.requestTask) {
      this.requestTask.abort()
      this.requestTask = null
    }
    this._buffer = ''
  }
}

module.exports = { SseClient, BASE_URL }
