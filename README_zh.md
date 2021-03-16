#设备备用网络

[中文版](README_zh.md) |[English](README.md)


备用网络就是将wifi和密码保存到设备端。当设备当前连接wifi没有网络时候，设备可以自己取连接已保存的wifi。也可以通过app进行主动切换。这里提供设备备用网络案例。

##功能介绍
-获取设备当前网络
-切换设备网络
-获取、设置备用网络

###获取设备当前网络


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

**参数说明**

参数	 | 说明
---|---
ssid | WiFi ssid
signal | 信号强度
network | 区分有线网络和WiFi，0表示WiFi，1表示有线
version | version
hash | 网络数据hash值，用于区分网络

###获取当前设备中已有的备用wifi列表

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
**参数说明**
参数	 | 说明
---|---
maxNum | 可设置的最大备用网络数
backupList | 设备的备用网络

###生成WiFi信息的hash数据

用于和备用网络的hash比较，判断是否已经添加过该网络


```
val hash = SHA256Util.getBase64Hash(dev?.getLocalKey() + ssid + pwd)
```

###新增备用网络并切换

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

###切换到备用网络

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

###设置备用网络

注意添加或者移除备用网络都可以通过该方法

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
**参数说明**
参数	 | 说明
---|---
List<BackupWifiBean> backupWifiList | 备用网络列表
ITuyaDataCallback<BackupWifiResultBean> dataCallback | 设置备用网络回调

