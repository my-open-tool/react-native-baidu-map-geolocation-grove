#import "BMapGeolocationModule.h"
#import <CoreLocation/CoreLocation.h>

@interface BMapGeolocationModule () <CLLocationManagerDelegate>

@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, copy) RCTResponseSenderBlock successCallback;
@property (nonatomic, copy) RCTResponseSenderBlock errorCallback;
@property (nonatomic, assign) BOOL isWatching;

@end

@implementation BMapGeolocationModule

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[@"BMapGetCurrentLocationPosition", @"BMapLocationUpdate", @"BMapLocationError"];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _locationManager = [[CLLocationManager alloc] init];
        _locationManager.delegate = self;
        _locationManager.desiredAccuracy = kCLLocationAccuracyBest;
        _isWatching = NO;
    }
    return self;
}

RCT_EXPORT_METHOD(initPrivacy) {
    // iOS 端不需要特殊的隐私协议初始化
    // 系统会自动处理权限请求
}

RCT_EXPORT_METHOD(getCurrentPosition:(NSString *)coorType) {
    if (![CLLocationManager locationServicesEnabled]) {
        [self sendErrorEvent:@"BMapLocationError" errorMessage:@"Location services are disabled"];
        return;
    }
    
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
        [self sendErrorEvent:@"BMapLocationError" errorMessage:@"Location permission denied"];
        return;
    }
    
    if (status == kCLAuthorizationStatusNotDetermined) {
        [self.locationManager requestWhenInUseAuthorization];
        return;
    }
    
    [self.locationManager requestLocation];
}

RCT_EXPORT_METHOD(startLocating:(NSString *)coorType scanSpan:(NSInteger)scanSpan distanceFilter:(NSInteger)distanceFilter) {
    if (![CLLocationManager locationServicesEnabled]) {
        [self sendErrorEvent:@"BMapLocationError" errorMessage:@"Location services are disabled"];
        return;
    }
    
    CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
    if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
        [self sendErrorEvent:@"BMapLocationError" errorMessage:@"Location permission denied"];
        return;
    }
    
    if (status == kCLAuthorizationStatusNotDetermined) {
        [self.locationManager requestWhenInUseAuthorization];
        return;
    }
    
    self.isWatching = YES;
    self.locationManager.distanceFilter = distanceFilter > 0 ? distanceFilter : kCLDistanceFilterNone;
    [self.locationManager startUpdatingLocation];
}

RCT_EXPORT_METHOD(stopLocating) {
    self.isWatching = NO;
    [self.locationManager stopUpdatingLocation];
}

RCT_EXPORT_METHOD(isStarted:(RCTResponseSenderBlock)callback) {
    callback(@[@(self.isWatching)]);
}

#pragma mark - CLLocationManagerDelegate

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray<CLLocation *> *)locations {
    CLLocation *location = [locations lastObject];
    
    NSDictionary *coords = @{
        @"latitude": @(location.coordinate.latitude),
        @"longitude": @(location.coordinate.longitude),
        @"altitude": @(location.altitude),
        @"accuracy": @(location.horizontalAccuracy),
        @"heading": @(location.course),
        @"speed": @(location.speed)
    };
    
    NSDictionary *params = @{
        @"coords": coords,
        @"timestamp": [NSString stringWithFormat:@"%.0f", location.timestamp.timeIntervalSince1970 * 1000]
    };
    
    if (self.isWatching) {
        [self sendEventWithName:@"BMapLocationUpdate" body:params];
    } else {
        [self sendEventWithName:@"BMapGetCurrentLocationPosition" body:params];
    }
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    NSString *errorMessage = @"Location error occurred";
    
    switch (error.code) {
        case kCLErrorDenied:
            errorMessage = @"Location permission denied";
            break;
        case kCLErrorLocationUnknown:
            errorMessage = @"Location unavailable";
            break;
        case kCLErrorNetwork:
            errorMessage = @"Network error";
            break;
        default:
            errorMessage = error.localizedDescription;
            break;
    }
    
    [self sendErrorEvent:@"BMapLocationError" errorMessage:errorMessage];
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if (status == kCLAuthorizationStatusAuthorizedWhenInUse || status == kCLAuthorizationStatusAuthorizedAlways) {
        // 权限获取成功，可以开始定位
        if (self.isWatching) {
            [self.locationManager startUpdatingLocation];
        }
    } else if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) {
        [self sendErrorEvent:@"BMapLocationError" errorMessage:@"Location permission denied"];
    }
}

#pragma mark - Helper Methods

- (void)sendErrorEvent:(NSString *)eventName errorMessage:(NSString *)errorMessage {
    NSDictionary *params = @{@"error": errorMessage};
    [self sendEventWithName:eventName body:params];
}

@end 