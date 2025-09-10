import { useState, useEffect, useCallback, useRef } from 'react'
import BMapGeolocation from './index'

/**
 * 百度地图定位 Hook
 * @returns {Object} 定位相关方法和状态
 */
export const useBMapGeolocation = () => {
  const [isInitialized, setIsInitialized] = useState(false)
  const [isLocating, setIsLocating] = useState(false)
  const [currentPosition, setCurrentPosition] = useState(null)
  const [error, setError] = useState(null)
  const watchIdRef = useRef(null)

  // 初始化
  const init = useCallback(async () => {
    try {
      await BMapGeolocation.init()
      setIsInitialized(true)
      setError(null)
    } catch (err) {
      setError(err)
      console.error('BMapGeolocation init failed:', err)
    }
  }, [])

  // 获取当前位置（Promise 版本）
  const getCurrentPosition = useCallback(async (options = {}) => {
    if (!isInitialized) {
      await init()
    }

    return new Promise((resolve, reject) => {
      BMapGeolocation.getCurrentPosition(
        (position) => {
          setCurrentPosition(position)
          setError(null)
          resolve(position)
        },
        (err) => {
          setError(err)
          reject(err)
        },
        options
      )
    })
  }, [isInitialized, init])

  // 开始持续定位
  const watchPosition = useCallback((success, error, options = {}) => {
    if (!isInitialized) {
      const initError = new Error('BMapGeolocation not initialized')
      if (error) {
        error(initError)
      }
      return null
    }

    setIsLocating(true)
    setError(null)

    const watchId = BMapGeolocation.watchPosition(
      (position) => {
        setCurrentPosition(position)
        if (success) {
          success(position)
        }
      },
      (err) => {
        setError(err)
        setIsLocating(false)
        if (error) {
          error(err)
        }
      },
      options
    )

    watchIdRef.current = watchId
    return watchId
  }, [isInitialized])

  // 停止持续定位
  const clearWatch = useCallback((id = watchIdRef.current) => {
    BMapGeolocation.clearWatch(id)
    setIsLocating(false)
    watchIdRef.current = null
  }, [])

  // 检查是否正在定位
  const isStarted = useCallback(() => {
    return BMapGeolocation.isStarted()
  }, [])

  // 清理函数
  useEffect(() => {
    return () => {
      if (watchIdRef.current) {
        clearWatch(watchIdRef.current)
      }
    }
  }, [clearWatch])

  return {
    // 状态
    isInitialized,
    isLocating,
    currentPosition,
    error,
    
    // 方法
    init,
    getCurrentPosition,
    watchPosition,
    clearWatch,
    isStarted
  }
}

/**
 * 自动定位 Hook
 * @param {Object} options - 配置选项
 * @param {boolean} options.autoStart - 是否自动开始定位
 * @param {number} options.interval - 定位间隔
 * @param {number} options.distanceFilter - 距离过滤
 * @returns {Object} 定位相关方法和状态
 */
export const useAutoLocation = (options = {}) => {
  const { autoStart = false, interval = 10000, distanceFilter = 10 } = options
  const {
    isInitialized,
    isLocating,
    currentPosition,
    error,
    init,
    getCurrentPosition,
    watchPosition,
    clearWatch
  } = useBMapGeolocation()

  // 自动开始定位
  useEffect(() => {
    if (autoStart && isInitialized && !isLocating) {
      watchPosition(
        (position) => {
          console.log('Auto location update:', position)
        },
        (err) => {
          console.error('Auto location error:', err)
        },
        { interval, distanceFilter }
      )
    }
  }, [autoStart, isInitialized, isLocating, watchPosition, interval, distanceFilter])

  return {
    isInitialized,
    isLocating,
    currentPosition,
    error,
    init,
    getCurrentPosition,
    watchPosition,
    clearWatch
  }
} 