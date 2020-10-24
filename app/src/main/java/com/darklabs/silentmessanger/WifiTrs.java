package com.darklabs.silentmessanger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.List;

public class WifiTrs<WifiNetworkList> {
    public String target = "TARGET";
    public WifiManager mWifiManager;
    public List<ScanResult> mScanResultList = null;
    public List<android.net.wifi.ScanResult> mTrustedWifiNetworks;
    public final File WifiNetworkList = new File(String.valueOf(R.string.WifiTrustedNetworks));

    public class mNetworkMonitorState extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            WifiManager mWfifSwicher = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration mWifiConfiguration = new WifiConfiguration();
            mWifiConfiguration.hiddenSSID = true;
            mWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
            mWiFiConfigurator(mWifiConfiguration);
            mScanResultList = null;
            NetworkInfo liveNetwork = connectivityManager.getActiveNetworkInfo();
            if (liveNetwork.isConnected()) {
                mWfifSwicher.setWifiEnabled(false);
            } else {
                mWfifSwicher.setWifiEnabled(true);
                mWifiManager.startScan();
                mScanResultList = mWifiManager.getScanResults();
                formatter(mScanResultList);
                while (mScanResultList.isEmpty()) {
                    mWfifSwicher.setWifiEnabled(false);
                }
            }
            WiFiCompare(mScanResultList);

        }

        private boolean WiFiCompare(List<android.net.wifi.ScanResult> inner) {
            try {
                FileInputStream fileInputStream = new FileInputStream(WifiNetworkList);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                List<android.net.wifi.ScanResult> current = (List<android.net.wifi.ScanResult>) bufferedReader;
                if (inner != null) {
                    current = inner;
                } else {
                    return false;
                }
                formatter(current);
                formatter(mScanResultList);
                try {
                    Object ones = current.class.getField("SSID");
                    while (mTrustedWifiNetworks.contains(ones)) {
                        Object[] carrentArr = current.toArray();
                        for (int i = 0; i < carrentArr.length; i++) {
                            String s = (String) carrentArr[i];
                            if (s.contentEquals(target)) {

                                return true;
                            }
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return false;
        }

        public void formatter(List<android.net.wifi.ScanResult> list) {
            try {
                list.remove(android.net.wifi.ScanResult.class.getField("Creator"));
                list.remove(android.net.wifi.ScanResult.class.getField("centerFreq0"));
                list.remove(android.net.wifi.ScanResult.class.getField("centerFreq1"));
                list.remove(android.net.wifi.ScanResult.class.getField("channelWidth"));
                list.remove(android.net.wifi.ScanResult.class.getField("frequency"));
                list.remove(android.net.wifi.ScanResult.class.getField("level"));
                list.remove(android.net.wifi.ScanResult.class.getField("operatorFriendlyName"));
                list.remove(android.net.wifi.ScanResult.class.getField("timestamp"));
                list.remove(android.net.wifi.ScanResult.class.getField("venueName"));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }


        }

        private class current {
            public android.net.wifi.ScanResult current;
        }
    }

    public WifiConfiguration mWiFiConfigurator(WifiConfiguration MYWifiConfiguration) {
        MYWifiConfiguration.preSharedKey = null;
        MYWifiConfiguration.hiddenSSID = true;
        MYWifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        MYWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        MYWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.PairwiseCipher.TKIP);
        MYWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        return MYWifiConfiguration;
    }
}
