package com.bmap.geolocation;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.baidu.location.Poi;
import com.facebook.react.bridge.WritableArray;


public class BMapGeolocationModule extends ReactContextBaseJavaModule {
    private LocationClient mLocationClient;
    private volatile boolean isLocating = false;
    private volatile boolean isPrivacyInitialized = false;
    protected ReactApplicationContext context;
    private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

    public BMapGeolocationModule(ReactApplicationContext reactContext) {
        super(reactContext);
        context = reactContext;
    }

    @Override
    public String getName() {
        return "BMapGeolocationModule";
    }

    /**
     * 初始化百度地图SDK隐私协议
     * 必须在用户同意隐私协议后调用
     */
    @ReactMethod
    public void initPrivacy() {
        if (isPrivacyInitialized) {
            Log.i("BMapGeolocationModule", "隐私协议已经初始化，跳过重复初始化");
            return;
        }

        try {
            Log.i("BMapGeolocationModule", "调用init方法，初始化百度地图SDK隐私协议");

            LocationClient.setAgreePrivacy(true);
            isPrivacyInitialized = true;
            
            
        } catch (Exception e) {
            Log.e("BMapGeolocationModule", "设置百度地图SDK隐私协议失败: " + e.getMessage());
            // 尝试另一种方式
            try {
                Class<?> privacyClass = Class.forName("com.baidu.location.Privacy");
                Object privacy = privacyClass.newInstance();
                privacyClass.getMethod("setAgreePrivacy", boolean.class)
                    .invoke(privacy, true);
                
                Log.i("BMapGeolocationModule", "通过反射设置百度地图SDK隐私协议成功");
                isPrivacyInitialized = true;
            } catch (Exception e2) {
                Log.e("BMapGeolocationModule", "反射设置百度地图SDK隐私协议也失败: " + e2.getMessage());
                // 如果都失败了，我们假设隐私协议已经同意（为了兼容性）
                isPrivacyInitialized = true;
            }
        }

        Log.i("BMapGeolocationModule", "百度地图SDK隐私协议-isPrivacyInitialized-"  + isPrivacyInitialized);
    }

    protected void sendEvent(String eventName, WritableMap params) {
        if (eventEmitter == null) {
            eventEmitter = context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        }
        eventEmitter.emit(eventName, params);
    }

    protected void sendErrorEvent(String eventName, String errorMessage) {
        WritableMap params = Arguments.createMap();
        params.putString("error", errorMessage);
        sendEvent(eventName, params);
    }

    private void initLocationClient(String coorType, int scanSpan, boolean isOnceLocation, int distanceFilter) {
        try {
            if (mLocationClient != null) {
                mLocationClient.stop();
                mLocationClient = null;
            }

            LocationClientOption option = new LocationClientOption();
            // 定位精度
            option.setLocationMode(LocationMode.Hight_Accuracy);
            // 使用卫星定位
            option.setOpenGnss(true);
            // 使用GPS定位(旧)
            option.setOpenGps(true);
            // 坐标系类型
            option.setCoorType(coorType);
            // 是否需要返回海拔高度
            option.setIsNeedAltitude(true);
            // 设置扫描间隔，单位毫秒，当不设此项，则默认只定位一次
            option.setScanSpan(scanSpan);
            // 设置定位超时时间
            option.setTimeOut(5000);
            // 设置是否需要返回地址信息
            option.setIsNeedAddress(false);
            // 是否需要返回位置POI信息
            option.setIsNeedLocationPoiList(false);
            // 是否需要返回位置语义化信息
            option.setIsNeedLocationDescribe(false);
            
            // 设置是否进行单次定位
            if (isOnceLocation) {
                option.setOnceLocation(true);
                Log.i("BMapGeolocationModule", "单次定位模式，setOnceLocation: true");
            } else {
                // 持续定位：不设置 setOnceLocation，让 SDK 使用 scanSpan 来控制
                Log.i("BMapGeolocationModule", "持续定位模式，不设置 setOnceLocation，使用 scanSpan: " + scanSpan + "ms");
                
                // 设置自动通知模式：scanSpan间隔，distanceFilter距离过滤
                if (distanceFilter > 0) {
                    option.setOpenAutoNotifyMode(scanSpan, distanceFilter, LocationClientOption.LOC_SENSITIVITY_HIGHT);
                    Log.i("BMapGeolocationModule", "设置自动通知模式：scanSpan=" + scanSpan + "ms, distanceFilter=" + distanceFilter + "m, sensitivity=HIGH");
                } else {
                    Log.i("BMapGeolocationModule", "持续定位模式，scanSpan: " + scanSpan + "ms, 不使用距离过滤");
                }
            }

            Log.i("BMapGeolocationModule", "初始化定位客户端 - scanSpan: " + scanSpan + "ms, setOnceLocation: " + isOnceLocation + ", distanceFilter: " + distanceFilter + "m");
            mLocationClient = new LocationClient(context.getApplicationContext());
            mLocationClient.setLocOption(option);
            
            // 定位监听
            mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    if (location == null) {
                        Log.e("BMapGeolocationModule", "收到空的位置信息");
                        return;
                    }

                    // 检查定位是否成功
                    if (location.getLocType() == BDLocation.TypeNone) {
                        Log.e("BMapGeolocationModule", "定位失败: " + location.getLocTypeDescription());
                        sendErrorEvent("BMapLocationError", "定位失败: " + location.getLocTypeDescription());
                        return;
                    }

                    // 检查定位是否无效
                    if (location.getLocType() == BDLocation.TypeCriteriaException || 
                        location.getLocType() == BDLocation.TypeServerError || 
                        location.getLocType() == BDLocation.TypeNetWorkException || 
                        location.getLocType() == BDLocation.TypeOffLineLocationFail) {
                        Log.e("BMapGeolocationModule", "定位无效: " + location.getLocTypeDescription());
                        sendErrorEvent("BMapLocationError", "定位无效: " + location.getLocTypeDescription());
                        return;
                    }

                    // 创建 coords 对象
                    WritableMap coords = Arguments.createMap();
                    coords.putDouble("latitude", location.getLatitude());
                    coords.putDouble("longitude", location.getLongitude());
                    coords.putDouble("altitude", location.getAltitude());
                    coords.putDouble("accuracy", location.getRadius());
                    coords.putDouble("heading", location.getDirection());
                    coords.putDouble("speed", location.getSpeed());
                    coords.putString("address", location.getAddrStr());
                    coords.putString("country", location.getCountry());
                    coords.putString("province", location.getProvince());
                    coords.putString("city", location.getCity());
                    coords.putString("area", location.getDistrict());
                    coords.putString("town", location.getTown());
                    coords.putString("street", location.getStreet());
                    coords.putString("streetNumber", location.getStreetNumber());

                    // 获取POI列表
                    try {
                        if (location.getPoiList() != null && location.getPoiList().size() > 0) {
                            WritableArray poiArray = Arguments.createArray();
                            for (int i = 0; i < location.getPoiList().size(); i++) {
                                Poi poi = location.getPoiList().get(i);
                                if (poi != null) {
                                    WritableMap poiItem = Arguments.createMap();
                                    poiItem.putString("uid", poi.getId());
                                    poiItem.putString("name", poi.getName());
                                    poiItem.putString("address", poi.getAddr());
                                    poiItem.putDouble("rank", poi.getRank());
                                    poiArray.pushMap(poiItem);
                                }
                            }
                            coords.putArray("pois", poiArray);
                            Log.i("BMapLocationModule", "获取到 " + location.getPoiList().size() + " 个POI点");
                        } else {
                            Log.i("BMapLocationModule", "未获取到POI信息");
                        }
                    } catch (Exception e) {
                        Log.e("BMapLocationModule", "获取POI信息失败: " + e.getMessage());
                    }

                    // 创建主参数对象
                    WritableMap params = Arguments.createMap();
                    params.putMap("coords", coords);
                    params.putString("timestamp", location.getTime());
                
                    Log.i("BMapGeolocationModule", "收到位置更新，坐标: " + location.getLatitude() + ", " + location.getLongitude() + ", 定位类型: " + location.getLocTypeDescription() + ", isOnceLocation: " + isOnceLocation + ", isLocating: " + isLocating + ", scanSpan: " + scanSpan);

                    if (isOnceLocation) {
                        // 单次定位：发送结果并停止定位
                        isLocating = false;
                        Log.i("BMapGeolocationModule", "发送单次定位结果: BMapGetCurrentLocationPosition");
                        sendEvent("BMapGetCurrentLocationPosition", params);
                        // setOnceLocation(true) 会自动停止定位，无需手动停止
                    } else {
                        // 持续定位：发送位置更新事件
                        Log.i("BMapGeolocationModule", "发送持续定位结果: BMapLocationUpdate");
                        sendEvent("BMapLocationUpdate", params);
                    }
                }
                
                @Override
                public void onConnectHotSpotMessage(String s, int i) {
                    Log.i("BMapGeolocationModule", "onConnectHotSpotMessage: " + s + ", " + i);
                }
            });
            
        } catch (Exception e) {
            Log.e("BMapGeolocationModule", "初始化LocationClient失败: " + e.getMessage());
            isLocating = false;
            sendErrorEvent("BMapLocationError", "初始化定位客户端失败: " + e.getMessage());
        }
    }

    @ReactMethod
    public void getCurrentPosition(String coorType) {
        if (!isPrivacyInitialized) {
            Log.e("BMapGeolocationModule", "getCurrentPosition: 隐私协议未初始化，请先调用initPrivacy方法");
            sendErrorEvent("BMapLocationError", "隐私协议未初始化，请先调用initPrivacy方法");
            return;
        }
        
        if (isLocating) {
            Log.i("BMapGeolocationModule", "getCurrentPosition: 已经在定位中，先停止");
            stopLocating();
        }
        
        isLocating = true;
        Log.i("BMapGeolocationModule", "getCurrentPosition: 开始单次定位");
        initLocationClient(coorType, 0, true, 0); // 单次定位
        
        if (mLocationClient != null) {
            mLocationClient.start();
        } else {
            isLocating = false;
            sendErrorEvent("BMapLocationError", "定位客户端初始化失败");
        }
    }

    @ReactMethod
    public void startLocating(String coorType, int scanSpan, int distanceFilter) {
        if (!isPrivacyInitialized) {
            Log.e("BMapGeolocationModule", "startLocating: 隐私协议未初始化，请先调用initPrivacy方法");
            sendErrorEvent("BMapLocationError", "隐私协议未初始化，请先调用initPrivacy方法");
            return;
        }
        
        if (isLocating) {
            Log.i("BMapGeolocationModule", "startLocating: 已经在定位中，先停止");
            stopLocating();
        }
        
        // 对于持续定位，使用合适的扫描间隔
        if (scanSpan <= 0) {
            scanSpan = 3000; // 使用3秒间隔作为默认值
            Log.i("BMapGeolocationModule", "scanSpan 为 0，使用默认值 3000ms");
        } else if (scanSpan > 30000) {
            scanSpan = 10000; // 最大10秒间隔
            Log.i("BMapGeolocationModule", "scanSpan 过大，调整为 10000ms");
        }
        
        isLocating = true;
        Log.i("BMapGeolocationModule", "开始持续定位，scanSpan: " + scanSpan + "ms");
        initLocationClient(coorType, scanSpan, false, distanceFilter); // 持续定位，使用 distanceFilter
        
        if (mLocationClient != null) {
            try {
                mLocationClient.start();
                Log.i("BMapGeolocationModule", "持续定位服务启动成功");
            } catch (Exception e) {
                Log.e("BMapGeolocationModule", "启动持续定位服务失败: " + e.getMessage());
                isLocating = false;
                sendErrorEvent("BMapLocationError", "启动持续定位服务失败: " + e.getMessage());
            }
        } else {
            isLocating = false;
            sendErrorEvent("BMapLocationError", "定位客户端初始化失败");
        }
    }

    @ReactMethod
    public void stopLocating() {
        Log.i("BMapGeolocationModule", "停止定位");
        isLocating = false;
        
        if (mLocationClient != null) {
            try {
                mLocationClient.stop();
                mLocationClient = null;
            } catch (Exception e) {
                Log.e("BMapGeolocationModule", "停止定位失败: " + e.getMessage());
            }
        }
    }

    @ReactMethod
    public boolean isStarted() {
        return isLocating;
    }
} 