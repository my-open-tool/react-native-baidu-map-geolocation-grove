# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-01-XX

### Added
- 初始版本发布
- 支持 Android 和 iOS 平台
- 提供单次定位和持续定位功能
- 支持高精度定位
- 自动坐标系转换（GCJ-02）
- 隐私协议合规处理
- React Hook 支持
- 完整的错误处理机制
- 详细的 API 文档和使用示例

### Features
- `getCurrentPosition()` - 获取当前位置
- `watchPosition()` - 持续监听位置变化
- `clearWatch()` - 停止持续定位
- `init()` - 初始化模块
- `isStarted()` - 检查定位状态
- `useBMapGeolocation()` - React Hook
- `useAutoLocation()` - 自动定位 Hook

### Technical Details
- Android: 基于百度地图 SDK 9.6.5.1
- iOS: 基于系统 CoreLocation 框架
- 支持 TypeScript
- 完整的错误码定义
- 事件驱动的架构设计 