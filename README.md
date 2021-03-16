# Device standby network

[中文版](README_zh.md) |[English](README.md)


The standby network is to save the WiFi and password to the device. When the device is currently connected to WIFI and there is no network, the device can fetch the saved WIFI by itself. You can also actively switch through the app. The device standby network case is provided here.

## Features
-Gets the current network of the device
-Switching device network
-Get and set up the standby network

### Gets the current network of the device


```
wifiBackup = TuyaHomeSdk.getWifiBackupManager(deviceId)

wifiBackup?.getCurrentWifiInfo(object : ITuyaDataCallback<CurrentWifiInfoBean> {
            override fun onSuccess(result: CurrentWifiInfoBean) {
            }

            override fun onError(errorCode: String, errorMessage: String) {
            }
        })
//Be careful to destroy wifiBackup
wifiBackup?.onDestroy()


```

**Declaration**

Parameters	 | Description
---|---
ssid | WiFi ssid
signal | Signal strength
network | Distinguish between wired network and WiFi, with 0 for WiFi and 1 for Wired
version | version
hash | Network data hash value, used to distinguish networks

### Gets a list of standby WiFi existing on the current device

```
wifiBackupManager = TuyaHomeSdk.getWifiBackupManager(devId)
wifiBackupManager?.getBackupWifiList(object : ITuyaDataCallback<BackupWifiListInfo> {
    override fun onSuccess(result: BackupWifiListInfo) {
    }

    override fun onError(errorCode: String, errorMessage: String) {
    }
})
//Be careful to destroy wifiBackupManager
wifiBackupManager?.onDestroy()
```
**Declaration**

Parameters	 | Description
---|---
maxNum | The maximum number of alternate networks that can be set up
backupList | Backup network of device

### Generate the hash data of WiFi information

Used to compare the hash of the standby network to determine whether the network has been added

```
val hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
```

### Add backup network and switch

```
wifiSwitchManager?.switchToNewWifi(ssid, pwd, object : ITuyaDataCallback<SwitchWifiResultBean> {
            override fun onSuccess(result: SwitchWifiResultBean?) {
            }

            override fun onError(errorCode: String, errorMessage: String) {
            }
        }
)
wifiBackupManager?.onDestroy()
```

### Switch to the standby network

```
wifiSwitchManager?.switchToBackupWifi(hash, object : ITuyaDataCallback<SwitchWifiResultBean> {
            override fun onSuccess(result: SwitchWifiResultBean?) {
            }

            override fun onError(errorCode: String, errorMessage: String) {
            }
        }
)
wifiBackupManager?.onDestroy()
```

### Set up standby network

Note that alternate networks can be added or removed using this method

```
wifiBackup = TuyaHomeSdk.getWifiBackupManager(devId)
wifiBackup?.setBackupWifiList(backupWifiBeans, object : ITuyaDataCallback<BackupWifiResultBean> {
    override fun onSuccess(result: BackupWifiResultBean) {
    }

    override fun onError(errorCode: String, errorMessage: String) {
    }
})
wifiBackup?.onDestroy()

```

**Declaration**

Parameters	 | Description
---|---
List<BackupWifiBean> backupWifiList | Alternate network list
ITuyaDataCallback<BackupWifiResultBean> dataCallback | Set up the alternate network callback

