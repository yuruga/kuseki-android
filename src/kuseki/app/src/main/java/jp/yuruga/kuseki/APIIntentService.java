package jp.yuruga.kuseki;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static jp.yuruga.kuseki.utils.Util.*;
/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class APIIntentService extends IntentService {



    private static final String ACTION_CHECK_IN = "jp.yuruga.kuseki.action.CHECK_IN";
    private static final String ACTION_CHECK_OUT = "jp.yuruga.kuseki.action.CHECK_OUT";
    private static final String ACTION_GET_SEAT_AVAILABILITY = "jp.yuruga.kuseki.action.GET_SEAT_AVAILABILITY";
    private static final String EXTRA_JSON_PARAMS = "jp.yuruga.kuseki.extra.JSON_PARAMS";
    private static final String EXTRA_RECEIVER = "jp.yuruga.kuseki.extra.RECEIVER";
    private static final String API_HOST = "http://52.69.120.73:8000";
    private static final String END_POINT_CHECK_IN = "/api/v1/checkin";
    private static final String END_POINT_CHECK_OUT = "/api/v1/checkout";
    private static final String END_POINT_GET_SEAT_AVAILABILITY = "/api/v1/shop";


    //private static final String EXTRA_PARAM2 = "jp.yuruga.kuseki.extra.PARAM2";


    public static void startActionCheckIn(Context context, String params, ResultReceiver receiver) {
        Intent intent = new Intent(context, APIIntentService.class);
        intent.setAction(ACTION_CHECK_IN);
        intent.putExtra(EXTRA_JSON_PARAMS, params);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    public static void startActionCheckOut(Context context, String params, ResultReceiver receiver) {
        Intent intent = new Intent(context, APIIntentService.class);
        intent.setAction(ACTION_CHECK_OUT);
        intent.putExtra(EXTRA_JSON_PARAMS, params);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    public static void startActionGetSeatAvailavility(Context context, String params, ResultReceiver receiver) {
        Intent intent = new Intent(context, APIIntentService.class);
        intent.setAction(ACTION_GET_SEAT_AVAILABILITY);
        intent.putExtra(EXTRA_JSON_PARAMS, params);
        intent.putExtra(EXTRA_RECEIVER, receiver);
        context.startService(intent);
    }

    public APIIntentService() {
        super("APIIntentService");
        /*try{
            ApplicationInfo ai = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            log(ai.toString());
            log(bundle.toString());
            mApiHost = bundle.getString("jp.yuruga.kuseki.API_HOST");
        } catch (PackageManager.NameNotFoundException e)
        {
            log("Failed to load meta-data, NameNotFound: " + e.getMessage());
        }
        catch(NullPointerException e)
        {
            log("Failed to load meta-data, NullPointer: " + e.getMessage());
        }*/
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            final String params = intent.getStringExtra(EXTRA_JSON_PARAMS);
            final ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);
            Bundle b = new Bundle();
            JSONObject result = null;
            if (ACTION_CHECK_IN.equals(action)) {
                result = handleActionCheckIn(params);
            } else if (ACTION_CHECK_OUT.equals(action)) {
                result = handleActionCheckOut(params);
            } else if (ACTION_GET_SEAT_AVAILABILITY.equals(action)){
                result  = handleActionGetSeatAvailavility(params);
            }
            if(result != null){
                b.putString(ApiTask.RESULT, result.toString());
                receiver.send(100, b);
            }else{
                b.putString(ApiTask.RESULT, "{\n" +
                        "  \"head\": {\n" +
                        "    \"status\": 200\n" +
                        "  },\n" +
                        "  \"body\": {\n" +
                        "    \"shop_id\": 1234,\n" +
                        "    \"seat_number\": 5,\n" +
                        "    \"session_id\": 5678\n" +
                        "  }\n" +
                        "}");
                receiver.send(400,b);
            }

        }
    }

    private JSONObject handleActionGetSeatAvailavility(String params) {
        StringBuffer urlString = new StringBuffer();
        urlString.append(API_HOST);
        urlString.append(END_POINT_GET_SEAT_AVAILABILITY);
        try {
            JSONObject jsonParams = new JSONObject(params);


            urlString.append("?");
            Iterator<?> keys = jsonParams.keys();

            while( keys.hasNext() ) {
                String key = (String)keys.next();

                urlString.append(key);
                urlString.append("=");
                urlString.append(jsonParams.get(key).toString());
                urlString.append("&");
            }
            urlString.delete(urlString.length() - 1, urlString.length());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = null;
        try
        {
            url = new URL(urlString.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setDoInput(true);


            urlConnection.connect();

            InputStream inStream = null;
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null)
                response += temp;
            bReader.close();
            inStream.close();
            urlConnection.disconnect();
            object = (JSONObject) new JSONTokener(response).nextValue();
        }
        catch (Exception e)
        {
            log(e.toString());
        }



        return object;
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private JSONObject handleActionCheckIn(String params) {
        //log(params);
        return callPostAPI(API_HOST + END_POINT_CHECK_IN,params);

    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private JSONObject handleActionCheckOut(String params) {

        return callPostAPI(API_HOST + END_POINT_CHECK_OUT, params);

    }


    private JSONObject callPostAPI(String urlString, String params ){
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> p = gson.fromJson(params, type);


        HttpURLConnection urlConnection = null;
        URL url = null;
        JSONObject object = null;

        try
        {
            url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);


            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(p));

            writer.flush();
            writer.close();
            os.close();


            urlConnection.connect();

            InputStream inStream = null;
            inStream = urlConnection.getInputStream();
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));
            String temp, response = "";
            while ((temp = bReader.readLine()) != null)
                response += temp;
            bReader.close();
            inStream.close();
            urlConnection.disconnect();
            object = (JSONObject) new JSONTokener(response).nextValue();
        }
        catch (Exception e)
        {
            log(e.toString());
        }
        return object;




    }

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
