package jp.yuruga.kuseki;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static jp.yuruga.kuseki.utils.Util.log;

public class MapsActivity extends FragmentActivity implements ApiTask.APICallbackInterface{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private double latitude = 0D;
    private double longitude = 0D;
    private Integer mPredictionHour = 0;
    private Handler mHandler;
    private SeekBar mSeekbar;
    private ArrayList<Marker> mMarkers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        mHandler = new Handler();
        mSeekbar = (SeekBar)findViewById(R.id.seekBar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float val = seekBar.getProgress();
                mPredictionHour = (int)val;
                updateStatus();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        //システムサービスのLOCATION_SERVICEからLocationManager objectを取得
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //retrieve providerへcriteria objectを生成
        Criteria criteria = new Criteria();

        //Best providerの名前を取得
        String provider = locationManager.getBestProvider(criteria, true);

        //現在位置を取得
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null)
        {
            //現在位置の緯度を取得
            latitude = location.getLatitude();

            //現在位置の経度を取得
            longitude = location.getLongitude();
        }else{

            latitude = 35.666696D;
            longitude = 139.7490902D;
        }


        //現在位置からLatLng objectを生成
        LatLng latLng = new LatLng(latitude, longitude);

        //Google Mapに現在地を表示
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //Google Mapの Zoom値を指定
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


        //
        JSONObject json = new JSONObject();
        try {
            json.put("lat", Double.toString(latitude));
            json.put("lng",Double.toString(longitude));
            json.put("rad", 1200);
            json.put("ler",mPredictionHour);

            getShopsData(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateStatus(){
        //
        JSONObject json = new JSONObject();
        try {
            json.put("lat", Double.toString(latitude));
            json.put("lng",Double.toString(longitude));
            json.put("rad", 1200);
            json.put("ler",mPredictionHour);

            getShopsData(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void getShopsData(String params){
        ApiTask task = new ApiTask();
        task.setReceiver(mHandler);
        new ApiTask().request(this, ApiTask.API_GET_SEAT_AVAILABILITY, this, params);
    }

    @Override
    public void onAPIResult(final String result) {




        JSONArray shopsJson = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mMarkers.size(); i++) {
                    mMarkers.get(i).remove();
                }
                mMarkers.clear();
                try {
                    JSONArray shops= new JSONObject(result).getJSONArray("body");
                    Integer l = shops.length();
                    for(int i = 0; i<l; i++){
                        JSONObject shop = shops.getJSONObject(i);

                        LatLng loc = new LatLng(Double.parseDouble(shop.getString("lng")), Double.parseDouble(shop.getString("lat")));
                        float count = (float)(shop.getInt("count"));
                        float capacity = (float)(shop.getInt("capacity"));

                        addMarkerAt(loc, count, capacity );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private Marker addMarkerAt(LatLng position, float count, float capacity){
        if(capacity > 0 && count >= 0) {
            Float hue = Math.min(359f, 360 * (count / capacity));

            LevelListDrawable icon = (LevelListDrawable) this.getResources().getDrawable(R.drawable.coffee);
            icon.setLevel((int)Math.floor(hue));
            BitmapDrawable bmp = (BitmapDrawable)icon.getCurrent();
            Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(position)
                    //.icon(BitmapDescriptorFactory.defaultMarker(hue))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp.getBitmap()))
                            .title((int) Math.min(count, Math.max(1, capacity)) + "/" + (int) Math.max(1, capacity)+"席"));
            //marker.showInfoWindow();
            mMarkers.add(marker);
            return marker;
        }else{
            return null;
        }
    }
}
