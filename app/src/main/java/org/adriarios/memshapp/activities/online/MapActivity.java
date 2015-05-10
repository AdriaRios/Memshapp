package org.adriarios.memshapp.activities.online;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import org.adriarios.memshapp.activities.offline.ShowMemoriesAC;
import org.adriarios.memshapp.valueobjects.MemoryDataVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends ActionBarActivity implements OnMapReadyCallback {
    GoogleMap memoriesMap;
    List<MemoryDataVO> memoryList;
    HashMap<String, MemoryDataVO> markersPerMemoryMap;

    //Visual Elements
    ToggleButton showMyMemories;
    ToggleButton showAllMemories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initCustomMenu();

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

    private void initCustomMenu() {
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_details_memory_inflate, null);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.backToMemoriesListFromDetailsView);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this,
                        ShowMemoriesAC.class);

                startActivity(intent);
            }
        });

        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

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

    private void getRemoteMemories() throws IOException {
        // Create a new Thread to load the address
        memoryList = new ArrayList<MemoryDataVO>();
        markersPerMemoryMap = new HashMap<String, MemoryDataVO>();
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
                        String imagePath = (String)data.get(i).get("image_path");
                        String audioPath = (String)data.get(i).get("audio_path");
                        String videoPath = (String)data.get(i).get("video_path");
                        Double latitude = Double.parseDouble((String)data.get(i).get("latitude"));
                        Double longitude = Double.parseDouble((String)data.get(i).get("longitude"));
                        String date = (String)data.get(i).get("date");

                        memoryList.add(
                                new MemoryDataVO(Integer.valueOf(id),
                                        title,
                                        text,
                                        audioPath,
                                        videoPath,
                                        imagePath,
                                        latitude,
                                        longitude,
                                        date));

                    }


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
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pointer)));

                            markersPerMemoryMap.put(marker.getId(),memoryList.get(i));
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
                MemoryDataVO currentMemory = markersPerMemoryMap.get(marker.getId());
                Intent i = new Intent();
                Intent intent = new Intent(MapActivity.this,
                        DetailsOnLineMemoryAC.class);
                Bundle extras = new Bundle();
                extras.putInt("DETAILS_ID", currentMemory.getId());
                extras.putString("DETAILS_IMAGE_PATH", currentMemory.getImagePath());
                extras.putString("DETAILS_TITLE", currentMemory.getTitle());
                extras.putString("DETAILS_DESCRIPTION", currentMemory.getText());
                extras.putString("DETAILS_AUDIO_PATH", currentMemory.getAudioPath());
                extras.putString("DETAILS_VIDEO_PATH", currentMemory.getVideoPath());
                extras.putDouble("DETAILS_LATITUDE", currentMemory.getLatitude());
                extras.putDouble("DETAILS_LONGITUDE", currentMemory.getLongitude());
                extras.putString("DETAILS_DATE", currentMemory.getDate());
                intent.putExtras(extras);
                startActivity(intent);
            }
        });


        try {
            getRemoteMemories();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
