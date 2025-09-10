import { NativeModules, DeviceEventEmitter, Platform } from 'react-native'

const { BMapGeolocationModule } = NativeModules

// 默认配置
const DEFAULT_OPTIONS = {
  timeout: 10000,
  maximumAge: 0,
  enableHighAccuracy: true,
  distanceFilter: 0,
  interval: 10000
}

// 错误码常量
const ERROR_CODES = {
  PERMISSION_DENIED: 1,
  POSITION_UNAVAILABLE: 2,
  TIMEOUT: 3
}

// 事件名称
const EVENT_NAMES = {
  GET_CURRENT_POSITION: 'BMapGetCurrentLocationPosition',
  LOCATION_UPDATE: 'BMapLocationUpdate',
  LOCATION_ERROR: 'BMapLocationError'
}

/**
 * 百度地图定位模块
 * 提供高精度的地理位置服务
 */
class BMapGeolocation {
  constructor() {
    this.isInitialized = false
    this.watchId = null
    this.listeners = new Map()
  }

  /**
   * 初始化模块
   * @returns {Promise<void>}
   */
  async init() {
    if (this.isInitialized) {
      return
    }

    try {
      if (Platform.OS === 'android') {
        await BMapGeolocationModule.initPrivacy()
      }
      this.isInitialized = true
      console.log('BMapGeolocation initialized successfully')
    } catch (error) {
      console.error('BMapGeolocation init failed:', error)
      throw error
    }
  }

  /**
   * 获取当前位置（单次定位）
   * @param {Function} success - 成功回调
   * @param {Function} error - 错误回调
   * @param {Object} options - 配置选项
   */
  getCurrentPosition(success, error, options = {}) {
    if (!this.isInitialized) {
      const initError = new Error('BMapGeolocation not initialized. Please call init() first.')
      if (error) {
        error({ code: ERROR_CODES.POSITION_UNAVAILABLE, message: initError.message })
      }
      return
    }

    const config = { ...DEFAULT_OPTIONS, ...options }
    const coorType = 'gcj02'

    console.log('📍 BMap 单次定位开始')

    // 超时处理
    const timeoutId = setTimeout(() => {
      if (error) {
        error({
          code: ERROR_CODES.TIMEOUT,
          message: 'Location request timed out'
        })
      }
    }, config.timeout)

    try {
      BMapGeolocationModule.getCurrentPosition(coorType)
    } catch (e) {
      clearTimeout(timeoutId)
      if (error) {
        error({
          code: ERROR_CODES.POSITION_UNAVAILABLE,
          message: e.message || 'Failed to get current position'
        })
      }
      return
    }

    // 监听结果
    const successListener = DeviceEventEmitter.addListener(EVENT_NAMES.GET_CURRENT_POSITION, resp => {
      clearTimeout(timeoutId)
      successListener.remove()
      errorListener.remove()
      
      if (resp.error) {
        if (error) {
          error({
            code: ERROR_CODES.POSITION_UNAVAILABLE,
            message: resp.error
          })
        }
        return
      }

      if (success) {
        success(resp)
      }
    })

    const errorListener = DeviceEventEmitter.addListener(EVENT_NAMES.LOCATION_ERROR, resp => {
      clearTimeout(timeoutId)
      successListener.remove()
      errorListener.remove()
      
      if (error) {
        error({
          code: ERROR_CODES.POSITION_UNAVAILABLE,
          message: resp.error || 'Location error occurred'
        })
      }
    })
  }

  /**
   * 持续监听位置变化
   * @param {Function} success - 成功回调
   * @param {Function} error - 错误回调
   * @param {Object} options - 配置选项
   * @returns {number} watchId - 监听ID
   */
  watchPosition(success, error, options = {}) {
    if (!this.isInitialized) {
      const initError = new Error('BMapGeolocation not initialized. Please call init() first.')
      if (error) {
        error({ code: ERROR_CODES.POSITION_UNAVAILABLE, message: initError.message })
      }
      return null
    }

    const config = { ...DEFAULT_OPTIONS, ...options }
    const coorType = 'gcj02'

    console.log('📍 BMap 持续定位开始')

    // 生成唯一的监听ID
    const watchId = Date.now() + Math.random()

    try {
      BMapGeolocationModule.startLocating(coorType, config.interval, config.distanceFilter)
    } catch (e) {
      if (error) {
        error({
          code: ERROR_CODES.POSITION_UNAVAILABLE,
          message: e.message || 'Failed to start location watching'
        })
      }
      return null
    }

    // 监听位置更新
    const updateListener = DeviceEventEmitter.addListener(EVENT_NAMES.LOCATION_UPDATE, resp => {
      if (success) {
        success(resp)
      }
    })

    // 监听错误
    const errorListener = DeviceEventEmitter.addListener(EVENT_NAMES.LOCATION_ERROR, resp => {
      if (error) {
        error({
          code: ERROR_CODES.POSITION_UNAVAILABLE,
          message: resp.error || 'Location watching error occurred'
        })
      }
    })

    // 保存监听器引用
    this.listeners.set(watchId, {
      updateListener,
      errorListener
    })

    this.watchId = watchId
    return watchId
  }

  /**
   * 停止持续定位
   * @param {number} watchId - 监听ID
   */
  clearWatch(watchId = this.watchId) {
    if (!watchId) {
      return
    }

    console.log('🛑 BMap 停止持续定位')

    // 移除监听器
    const listeners = this.listeners.get(watchId)
    if (listeners) {
      listeners.updateListener.remove()
      listeners.errorListener.remove()
      this.listeners.delete(watchId)
    }

    // 停止定位服务
    try {
      BMapGeolocationModule.stopLocating()
    } catch (e) {
      console.error('Failed to stop location watching:', e)
    }

    if (watchId === this.watchId) {
      this.watchId = null
    }
  }

  /**
   * 检查是否正在定位
   * @returns {boolean}
   */
  isStarted() {
    if (Platform.OS === 'android') {
      return BMapGeolocationModule.isStarted()
    }
    return this.watchId !== null
  }
}

// 创建单例实例
const bMapGeolocation = new BMapGeolocation()

// 导出实例和类
export default bMapGeolocation
export { BMapGeolocation }

// 导出常量
export { ERROR_CODES, EVENT_NAMES }

// 导出 Hook
export { useBMapGeolocation, useAutoLocation } from './hooks' 