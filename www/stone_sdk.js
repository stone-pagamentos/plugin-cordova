module.exports = {
  setEnvironment: function (environment) {
    cordova.exec(null, null, "StoneSDK", "setEnvironment", [environment]);
  },
  setAppName: function (name) {
    cordova.exec(null, null, "StoneSDK", "setAppName", [name]);
  },
  device: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "device", []);
  },
  deviceSelected: function (arrayList, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "deviceSelected", [arrayList]);
  },
  isDeviceConnected: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "isDeviceConnected", []);
  },
  deviceDisplay: function (arrayList, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "deviceDisplay", [arrayList]);
  },
  transaction: function (amount, method, instalments, success_message, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "transaction", [amount, method, instalments, success_message]);
  },
  transactionCancel: function (transactionCode, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "transactionCancel", [transactionCode]);
  },
  transactionList: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "transactionList", []);
  },
  validation: function (stoneCodeList, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "StoneSDK", "validation", [stoneCodeList]);
  },
  tablesDownload: function (successCallback, errorCallback) {
    console.warn('The method tablesDownload is deprecated and was removed from the sdk. It is a noop and will be removed in the future');
    return successCallback();
  },
  tablesUpdate: function (successCallback, errorCallback) {
    console.warn('The method tablesDownload is deprecated and was removed from the sdk. It is a noop and will be removed in the future');
    return successCallback();
  }
};
