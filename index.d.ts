declare module 'react-native-baidu-map-geolocation-grove' {
  import { Component } from 'react';

  // 错误码常量
  export const ERROR_CODES: {
    PERMISSION_DENIED: 1;
    POSITION_UNAVAILABLE: 2;
    TIMEOUT: 3;
  };

  // 事件名称常量
  export const EVENT_NAMES: {
    GET_CURRENT_POSITION: 'BMapGetCurrentLocationPosition';
    LOCATION_UPDATE: 'BMapLocationUpdate';
    LOCATION_ERROR: 'BMapLocationError';
  };

  // 位置坐标接口
  export interface LocationCoords {
    latitude: number;
    longitude: number;
    altitude: number;
    accuracy: number;
    heading: number;
    speed: number;
  }

  // 位置信息接口
  export interface LocationPosition {
    coords: LocationCoords;
    timestamp: string;
  }

  // 错误信息接口
  export interface LocationError {
    code: number;
    message: string;
  }

  // 定位选项接口
  export interface LocationOptions {
    timeout?: number;
    enableHighAccuracy?: boolean;
    maximumAge?: number;
    interval?: number;
    distanceFilter?: number;
  }

  // 自动定位选项接口
  export interface AutoLocationOptions {
    autoStart?: boolean;
    interval?: number;
    distanceFilter?: number;
  }

  // Hook 返回值接口
  export interface UseBMapGeolocationReturn {
    isInitialized: boolean;
    isLocating: boolean;
    currentPosition: LocationPosition | null;
    error: LocationError | null;
    init: () => Promise<void>;
    getCurrentPosition: (options?: LocationOptions) => Promise<LocationPosition>;
    watchPosition: (success: (position: LocationPosition) => void, error: (error: LocationError) => void, options?: LocationOptions) => number | null;
    clearWatch: (id?: number) => void;
    isStarted: () => boolean;
  }

  // 主类接口
  export interface BMapGeolocationInterface {
    init(): Promise<void>;
    getCurrentPosition(success: (position: LocationPosition) => void, error: (error: LocationError) => void, options?: LocationOptions): void;
    watchPosition(success: (position: LocationPosition) => void, error: (error: LocationError) => void, options?: LocationOptions): number | null;
    clearWatch(watchId?: number): void;
    isStarted(): boolean;
  }

  // 主类
  export class BMapGeolocation implements BMapGeolocationInterface {
    init(): Promise<void>;
    getCurrentPosition(success: (position: LocationPosition) => void, error: (error: LocationError) => void, options?: LocationOptions): void;
    watchPosition(success: (position: LocationPosition) => void, error: (error: LocationError) => void, options?: LocationOptions): number | null;
    clearWatch(watchId?: number): void;
    isStarted(): boolean;
  }

  // Hook 函数
  export function useBMapGeolocation(): UseBMapGeolocationReturn;
  export function useAutoLocation(options?: AutoLocationOptions): UseBMapGeolocationReturn;

  // 默认导出
  const BMapGeolocation: BMapGeolocationInterface;
  export default BMapGeolocation;
} 