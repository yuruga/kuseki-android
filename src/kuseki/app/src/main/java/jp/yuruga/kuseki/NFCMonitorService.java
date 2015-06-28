package jp.yuruga.kuseki;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Handler;
import android.os.IBinder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;

import static jp.yuruga.kuseki.utils.Util.log;

public class NFCMonitorService extends Service {
    public static final String ACTION_MONITOR_START = "jp.yuruga.kuseki.action.monitor_start";
    public  static final String EXTRA_PARAMS = "jp.yuruga.kuseki.extra.params";
    public static final String EXTRA_TAG = "jp.yuruga.kuseki.extra.tag";
    private Tag mTag;
    private Handler mHandler;
    private Runnable checkConnectivity;
    private Ndef mNdef;
    private Integer mSessionId;
    private int mShopId;

    public NFCMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler = new Handler();
        String action = intent.getAction();
        if (action.equals(ACTION_MONITOR_START))
        {
            String params = intent.getStringExtra(EXTRA_PARAMS);
            mTag = intent.getParcelableExtra(EXTRA_TAG);
            mNdef = Ndef.get(mTag);
            try {
                mShopId = new JSONObject(params).getInt("shop_id");
                mSessionId = readSessionId(mShopId);

                mNdef.connect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(mSessionId != -1){
                try {
                    params = new JSONObject(params).put("session_id", mSessionId).toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            checkIn(params);
            //startMonitoringTagConnectivity();
        }
        return Service.START_STICKY_COMPATIBILITY;
    }

    private Integer readSessionId(int mShopId) {
        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        return pref.getInt("shop_"+mShopId, -1);
    }

    private void checkIn(String params){
        new ApiTask().request(this, ApiTask.API_CHECK_IN, new ApiTask.APICallbackInterface() {
            @Override
            public void onAPIResult(String result) {
                try {
                    mSessionId = new JSONObject(result).getJSONObject("body").getInt("session_id");
                    saveSessionId(mSessionId);
                    startMonitoringTagConnectivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, params);
    }

    private void startMonitoringTagConnectivity(){


        checkConnectivity = new Runnable() {
            @Override
            public void run() {

                if(mNdef != null){
                    if(!mNdef.isConnected()){

                        mHandler.removeCallbacksAndMessages(null);
                       if(mSessionId != null){
                           checkOut("{session_id:"+mSessionId+"}");
                       }
                    }else{
                        mHandler.postDelayed(checkConnectivity, 3000);
                    }

                }

            }
        };
        mHandler.postDelayed(checkConnectivity, 3000);
    }




    private void saveSessionId(Integer sessionId) {

        SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        
        editor.putInt("shop_" + mShopId, sessionId);
        
        editor.commit();
    }

    private void checkOut(String params){
        //TODO
        /*new ApiTask().request(this, ApiTask.API_CHECK_OUT, new ApiTask.APICallbackInterface() {
            @Override
            public void onAPIResult(String result) {
                try {
                    mSessionId = new JSONObject(result).getJSONObject("body").getInt("session_id");
                    saveSessionId(mSessionId);
                    startMonitoringTagConnectivity();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, params);*/
    }




}
