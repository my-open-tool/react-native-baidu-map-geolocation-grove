# react-native-baidu-map-geolocation-grove
> 重要说明 : 该仓库代码是抄自`react-native-bmap-geolocation`,然后根据目前情况做了微调. 
> 原作者地址:  https://github.com/zkgaojianjian/react-native-bmap-geolocation




React Native 百度地图定位模块，提供高精度的地理位置服务。

## 项目背景

### 为什么需要这个工具包？

在中国大陆地区，Android 手机的原生定位服务（Google Play Services）无法正常工作，导致以下问题：

1. **无法获取位置信息**: 使用 `@react-native-community/geolocation` 等基于原生定位的库无法获取位置
2. **定位精度差**: 即使能定位，精度也很低
3. **定位不稳定**: 经常出现定位失败的情况
4. **依赖Google服务**: 需要Google Play Services，但在国内无法正常使用

### 解决方案

本工具包基于百度地图SDK，提供以下优势：

- ✅ **国内可用**: 不依赖Google服务，在国内网络环境下正常工作
- ✅ **高精度定位**: 结合GPS、基站、WiFi等多种定位方式
- ✅ **稳定可靠**: 百度地图SDK经过多年优化，定位稳定性好
- ✅ **低功耗**: 优化的定位算法，减少电量消耗
- ✅ **后台定位**: 支持应用在后台时继续定位

## 功能特性

- 🎯 高精度定位
- 📍 单次定位和持续定位
- 🔄 自动坐标系转换
- 📱 支持 Android 和 iOS
- ��️ 隐私协议合规
- ⚡ 低功耗优化

## 安装

```bash
npm install react-native-baidu-map-geolocation-grove
# 或者
yarn add react-native-baidu-map-geolocation-grove
```

## 配置

> **重要**: Android 配置比较复杂，请务必查看 [Android 配置详细说明](./ANDROID_SETUP.md) 文档。

### Android 配置

1. 在 `android/app/build.gradle` 中添加依赖：

```gradle
dependencies {
    implementation 'com.baidu.lbsyun:BaiduMapSDK_Location:9.6.5.1'
}
```

2. 在 `android/app/src/main/AndroidManifest.xml` 中添加权限：

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

### 3. ⚠️ 重要：添加百度地图API Key

在 `android/app/src/main/AndroidManifest.xml` 的 `<application>` 标签内添加百度地图API Key：

```xml
<application>
    <!-- 其他配置... -->
    
    <!-- 百度地图API Key - 必需！ -->
    <meta-data
        android:name="com.baidu.lbsapi.API_KEY"
        android:value="您的百度地图API Key" />
        
    <!-- 其他配置... -->
</application>
```

**如何获取百度地图API Key：**
1. 访问 [百度地图开放平台](https://lbsyun.baidu.com/)
2. 注册并登录账号
3. 创建应用，选择"Android平台"
4. 填写应用包名（如：com.yourapp）
5. 获取API Key

### 4. ⚠️ 重要：添加百度地图定位服务组件（仅持续定位需要）

**如果您的应用需要使用持续定位功能**，请在 `android/app/src/main/AndroidManifest.xml` 的 `<application>` 标签内添加：

```xml
<application>
    <!-- 其他配置... -->
    
    <!-- 声明百度地图定位服务组件 - 仅持续定位需要！ -->
    <service 
        android:name="com.baidu.location.f" 
        android:enabled="true" 
        android:process=":remote" />
        
    <!-- 其他配置... -->
</application>
```

**如果您的应用只使用单次定位功能，则不需要添加此服务组件。**

> **注意**: 单次定位功能不需要添加此服务组件，只有持续定位功能才需要。

4. 在 `android/app/src/main/java/com/yourpackage/MainApplication.java` 中注册模块：

```java
import com.bmap.geolocation.BMapGeolocationPackage;

@Override
protected List<ReactPackage> getPackages() {
    List<ReactPackage> packages = new PackageList(this).getPackages();
    packages.add(new BMapGeolocationPackage());
    return packages;
}
```

### iOS 配置

1. 在 `ios/Podfile` 中添加：

```ruby
pod 'react-native-baidu-map-geolocation-grove', :path => '../node_modules/react-native-baidu-map-geolocation-grove'
```

2. 运行 `cd ios && pod install`

3. 在 `ios/YourApp/Info.plist` 中添加权限描述：

```xml
<key>NSLocationWhenInUseUsageDescription</key>
<string>需要定位权限来获取您的位置信息</string>
<key>NSLocationAlwaysAndWhenInUseUsageDescription</key>
<string>需要定位权限来获取您的位置信息</string>
```

## 使用方法

### 基本用法

```javascript
import BMapGeolocation from 'react-native-baidu-map-geolocation-grove';

// 初始化
await BMapGeolocation.init();

// 单次定位
BMapGeolocation.getCurrentPosition(
  (position) => {
    console.log('当前位置:', position);
  },
  (error) => {
    console.error('定位失败:', error);
  },
  {
    timeout: 10000,
    enableHighAccuracy: true,
    maximumAge: 0
  }
);

// 持续定位
const watchId = BMapGeolocation.watchPosition(
  (position) => {
    console.log('位置更新:', position);
  },
  (error) => {
    console.error('定位错误:', error);
  },
  {
    interval: 10000,
    distanceFilter: 10,
    enableHighAccuracy: true
  }
);

// 停止持续定位
BMapGeolocation.clearWatch(watchId);
```

### 使用 Hook

```javascript
import { useBMapGeolocation } from 'react-native-baidu-map-geolocation-grove';

const MyComponent = () => {
  const { 
    getCurrentPosition, 
    watchPosition, 
    clearWatch,
    isInitialized 
  } = useBMapGeolocation();

  const handleGetLocation = async () => {
    try {
      const position = await getCurrentPosition();
      console.log('位置:', position);
    } catch (error) {
      console.error('定位失败:', error);
    }
  };

  return (
    <Button title="获取位置" onPress={handleGetLocation} />
  );
};
```

## API 参考

### BMapGeolocation

#### 方法

- `init()`: 初始化模块
- `getCurrentPosition(success, error, options)`: 获取当前位置
- `watchPosition(success, error, options)`: 开始持续定位
- `clearWatch(watchId)`: 停止持续定位
- `isStarted()`: 检查是否正在定位

#### 参数

##### options 对象

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| timeout | number | 10000 | 超时时间（毫秒） |
| enableHighAccuracy | boolean | true | 是否启用高精度定位 |
| maximumAge | number | 0 | 位置缓存时间（毫秒） |
| interval | number | 10000 | 持续定位间隔（毫秒） |
| distanceFilter | number | 0 | 距离过滤（米） |

##### position 对象

| 属性 | 类型 | 说明 |
|------|------|------|
| latitude | number | 纬度 |
| longitude | number | 经度 |
| altitude | number | 海拔 |
| accuracy | number | 精度 |
| heading | number | 方向 |
| speed | number | 速度 |
| timestamp | string | 时间戳 |

##### error 对象

| 属性 | 类型 | 说明 |
|------|------|------|
| code | number | 错误码 |
| message | string | 错误信息 |

### useBMapGeolocation Hook

返回一个对象，包含以下方法：

- `getCurrentPosition(options)`: 返回 Promise 的单次定位方法
- `watchPosition(success, error, options)`: 持续定位方法
- `clearWatch(watchId)`: 停止持续定位
- `isInitialized`: 是否已初始化

## 错误码

| 错误码 | 说明 |
|--------|------|
| 1 | 权限被拒绝 |
| 2 | 位置不可用 |
| 3 | 超时 |

## 注意事项

1. **重要**: 使用前必须先调用 `init()` 方法初始化
2. **重要**: Android 端必须在 `AndroidManifest.xml` 中添加百度地图API Key
3. **重要**: 如果使用持续定位功能，Android 端必须在 `AndroidManifest.xml` 中声明百度地图定位服务组件
4. **重要**: 单次定位功能不需要添加服务组件声明
5. Android 需要确保已添加百度地图 SDK 依赖
6. iOS 需要配置相应的权限描述
7. 建议在应用启动时初始化模块

## 故障排除

### Android 常见问题

1. **API Key认证失败**
   - 检查是否在 `AndroidManifest.xml` 中添加了百度地图API Key
   - 确认API Key是否正确
   - 确认应用包名是否与百度地图开放平台注册的一致

2. **持续定位服务无法启动**
   - 检查是否在 `AndroidManifest.xml` 中添加了百度地图定位服务组件声明
   - 确认已添加所有必要的权限

3. **单次定位无法获取位置**
   - 检查应用是否有定位权限
   - 确认已添加百度地图SDK依赖
   - 确认已调用 `init()` 方法初始化
   - 确认API Key配置正确

4. **权限被拒绝**
   - 检查应用是否有定位权限
   - 确认在运行时请求了权限

### iOS 常见问题

1. **定位权限被拒绝**
   - 检查 `Info.plist` 中的权限描述是否正确
   - 确认在运行时请求了权限

## 许可证

MIT

## 贡献

欢迎提交 Issue 和 Pull Request！ 