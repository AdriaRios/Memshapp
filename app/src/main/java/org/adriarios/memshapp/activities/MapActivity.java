package org.adriarios.memshapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.valueobjects.MemoryDataVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {
    GoogleMap memoriesMap;
    List<MemoryDataVO> memoryList;

    //Visual Elements
    ToggleButton showMyMemories;
    ToggleButton showAllMemories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        showMyMemories =(ToggleButton)findViewById(R.id.showMyMemoriesMap);
        showMyMemories.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MapActivity.this,
                        ShowMemoriesAC.class);

                startActivity(intent);
            }
        });
        showAllMemories =(ToggleButton)findViewById(R.id.showAllMemoriesMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showMyMemories.setChecked(false);
        showAllMemories.setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


        return true;
    }

    private void testWebservice() throws IOException {
        // Create a new Thread to load the address
        memoryList = new ArrayList<MemoryDataVO>();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url("http://52.11.144.116/services.php?memoriesList")
                            .build();

                    Response response = client.newCall(request).execute();
                    String test=response.body().string();
                    Gson gson = new Gson();

                    LinkedTreeMap responseObject = gson.fromJson(test, LinkedTreeMap.class);
                    ArrayList<LinkedTreeMap> data = (ArrayList<LinkedTreeMap>) responseObject.get("data");

                    for (int i=0; i < data.size(); i++ ){
                        String id = (String)data.get(i).get("_id");
                        String title = (String)data.get(i).get("title");
                        String text = (String)data.get(i).get("text");
                        Double latitude = Double.parseDouble((String)data.get(i).get("latitude"));
                        Double longitude = Double.parseDouble((String)data.get(i).get("longitude"));
                        String date = (String)data.get(i).get("date");

                        memoryList.add(
                                new MemoryDataVO(Integer.valueOf(id),
                                        title,
                                        text,
                                        "",
                                        "",
                                        "",
                                        latitude,
                                        longitude,
                                        date));

                    }

                    Log.i("WS", test);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        for (int i = 0; i < memoryList.size(); i++){
                            MemoryDataVO item = memoryList.get(i);
                            Marker marker = memoriesMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(item.getLatitude(), item.getLongitude()))
                                    .title(item.getTitle())
                                    .snippet(item.getDate())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_m)));
                        }


                    }
                });


            }
        }).start(); // Executes the newly created thread


    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Some buildings have indoor maps. Center the camera over
        // the building, and a floor picker will automatically appear.
        memoriesMap = map;
        memoriesMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(41.38,2.17), 2));

        memoriesMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String id = marker.getId();
                Toast.makeText(MapActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });


        try {
            testWebservice();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
