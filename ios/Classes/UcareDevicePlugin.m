#import "UcareDevicePlugin.h"
#import <ucare_device_plugin/ucare_device_plugin-Swift.h>

@implementation UcareDevicePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftUcareDevicePlugin registerWithRegistrar:registrar];
}
@end
