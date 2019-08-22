#import <Cordova/CDV.h>
#import "StoneSDK/StoneSDK.h"

@interface StoneSDK : CDVPlugin

- (void) setEnvironment:(CDVInvokedUrlCommand*)command;
- (void) validation:(CDVInvokedUrlCommand*)command;
- (void) device:(CDVInvokedUrlCommand*)command;
- (void) isDeviceConnected:(CDVInvokedUrlCommand*)command;
- (void) deviceDisplay:(CDVInvokedUrlCommand*)command;
- (void) transaction:(CDVInvokedUrlCommand*)command;
- (void) transactionList:(CDVInvokedUrlCommand*)command;
- (void) transactionCancel:(CDVInvokedUrlCommand*)command;

@property (strong, nonatomic) STNTransactionModel *transaction;

@end
