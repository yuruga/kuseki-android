package jp.yuruga.kuseki;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by maedanaohito on 2015/06/27.
 */
public class ApiTask {
    public final static String RESULT = "result";

    public static final String API_GET_SEAT_AVAILABILITY = "getAvailability";
    public static final String API_CHECK_IN = "checkIn";
    public static final String API_CHECK_OUT = "checkOut";


    private Intent mIntent;
    private APIServiceTaskReceiver mResultReceiver = null;
    private APICallbackInterface mCallbackInterface;

    interface APICallbackInterface {
        void onAPIResult(String result);
    }
    public void setReceiver(Handler handler){
        mResultReceiver = new APIServiceTaskReceiver(handler);
    }
    public void request(Context context, String apiName, APICallbackInterface callbackInterface, String params){
        if(mResultReceiver == null){
            mResultReceiver = new APIServiceTaskReceiver(null);
        };
        mCallbackInterface = callbackInterface;
        //mIntent = new Intent(context, APIIntentService.class);
        /*mIntent.putExtra(RECEIVER, mResultReceiver);
        mIntent.putExtra(PARAMS, params);
        mIntent.setAction(action);
        context.startService(mIntent);*/
        if(API_CHECK_IN.equals(apiName)){
            APIIntentService.startActionCheckIn(context, params, mResultReceiver);
        }else if(API_CHECK_OUT.equals(apiName))
        {
            APIIntentService.startActionCheckOut(context, params, mResultReceiver);
        }else if(API_GET_SEAT_AVAILABILITY.equals(apiName))
        {
            APIIntentService.startActionGetSeatAvailavility(context, params, mResultReceiver);
        }


    }

    class APIServiceTaskReceiver extends ResultReceiver {
        public APIServiceTaskReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String result = resultData.getString(RESULT);
            mCallbackInterface.onAPIResult(result);
        }
    }
}
