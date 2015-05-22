package bom.mitac.bist.burnin.util;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnect {
	WifiManager wifiManager;

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }

    public WifiConnect(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    private boolean OpenWifi() {
        boolean bRet = true;
        if (!wifiManager.isWifiEnabled()) {
            bRet = wifiManager.setWifiEnabled(true);
        }
        return bRet;
    }

    public boolean Connect(String SSID, String Password, WifiCipherType Type) {
        if (!this.OpenWifi()) {
            return false;
        }
        while (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            try {
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
        }
 
        WifiConfiguration wifiConfig = this
                .CreateWifiInfo(SSID, Password, Type);
        if (wifiConfig == null) {
            return false;
        }

        WifiConfiguration tempConfig = this.IsExsits(SSID);
 
        if (tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
 
        int netID = wifiManager.addNetwork(wifiConfig);
        boolean bRet = wifiManager.enableNetwork(netID, true);
        return bRet;
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }
 
    private WifiConfiguration CreateWifiInfo(String SSID, String Password,
            WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            config.wepKeys[0] = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }else if (Type == WifiCipherType.WIFICIPHER_WPA) {
        	config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            //config.allowedAuthAlgorithms
            //        .set(WifiConfiguration.AuthAlgorithm.OPEN);
            //config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //config.allowedPairwiseCiphers
            //        .set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            //config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }
 
}
