package org.adriarios.memshapp.activities.offline;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.activities.online.MapActivity;
import org.adriarios.memshapp.adapter.MemoryAdapter;
import org.adriarios.memshapp.contentprovider.MemoriesProvider;
import org.adriarios.memshapp.valueobjects.MemoryDataVO;

import java.util.ArrayList;
import java.util.List;


public class ShowMemoriesAC extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    ContentResolver contentResolver;
    List<MemoryDataVO> memoryList;

    ToggleButton showMyMemories;
    ToggleButton showAllMemories;
    GridView gridview;
    MemoryAdapter memoryAdapter;

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;

    @Override
    protected void onResume() {
        super.onResume();

        showMyMemories.setChecked(true);
        showAllMemories.setChecked(false);

        getMemories();
        memoryAdapter = new MemoryAdapter(this, memoryList);
        // Set the Adapter for the GridView
        gridview.setAdapter(memoryAdapter);

        gridview.setOnItemClickListener(new GridView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(ShowMemoriesAC.this,
                        DetailsMemoryAC.class);
                Bundle extras = new Bundle();
                extras.putInt("DETAILS_ID", memoryList.get(position).getId());
                extras.putString("DETAILS_IMAGE_PATH", memoryList.get(position).getImagePath());
                extras.putString("DETAILS_TITLE", memoryList.get(position).getTitle());
                extras.putString("DETAILS_DESCRIPTION", memoryList.get(position).getText());
                extras.putString("DETAILS_AUDIO_PATH", memoryList.get(position).getAudioPath());
                extras.putString("DETAILS_VIDEO_PATH", memoryList.get(position).getVideoPath());
                extras.putDouble("DETAILS_LATITUDE", memoryList.get(position).getLatitude());
                extras.putDouble("DETAILS_LONGITUDE", memoryList.get(position).getLongitude());
                extras.putString("DETAILS_DATE", memoryList.get(position).getDate());
                extras.putString("DETAILS_CODE", memoryList.get(position).getMemoryCode());
                intent.putExtras(extras);
                startActivity(intent);

            }
        });
    }



    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_memories);
        gridview = (GridView) findViewById(R.id.gridView);
        showMyMemories =(ToggleButton)findViewById(R.id.showMyMemories);
        showAllMemories =(ToggleButton)findViewById(R.id.showAllMemories);
        showAllMemories.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ShowMemoriesAC.this,
                        MapActivity.class);

                startActivity(intent);
            }
        });
        initCustomMenu();
        buildGoogleApiClient();
    }

    private void initCustomMenu() {
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);


        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_show_memories_inflate, null);


        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }

    private void getMemories() {
        this.contentResolver = getContentResolver();

        Cursor coursesListCursor = this.contentResolver.query(
                MemoriesProvider.CONTENT_URI,
                new String[]{MemoriesProvider.MEMORY_ID,
                        MemoriesProvider.MEMORY_TITLE,
                        MemoriesProvider.MEMORY_TEXT,
                        MemoriesProvider.MEMORY_IMAGE,
                        MemoriesProvider.MEMORY_AUDIO,
                        MemoriesProvider.MEMORY_VIDEO,
                        MemoriesProvider.MEMORY_LATITUDE,
                        MemoriesProvider.MEMORY_LONGITUDE,
                        MemoriesProvider.MEMORY_DATE,
                        MemoriesProvider.MEMORY_CODE
                },
                null,
                null,
                null);
        fromCursor(coursesListCursor);
    }

    private void fromCursor(Cursor cursor) {
        memoryList = new ArrayList<MemoryDataVO>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_ID)
                );
                String memoryTitle = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_TITLE)
                );
                String memoryDesc = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_TEXT)
                );
                String imagePath = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_IMAGE)
                );
                String audioPath = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_AUDIO)
                );
                String videoPath = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_VIDEO)
                );
                Double latitude = cursor.getDouble(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_LATITUDE)
                );
                Double longitude = cursor.getDouble(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_LONGITUDE)
                );
                String date = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_DATE)
                );
                String memoryCode = cursor.getString(
                        cursor.getColumnIndex(MemoriesProvider.MEMORY_CODE)
                );


                memoryList.add(
                        new MemoryDataVO(id,
                                memoryTitle,
                                memoryDesc,
                                audioPath,
                                videoPath,
                                imagePath,
                                latitude,
                                longitude,
                                date,
                                memoryCode));

            } while (cursor.moveToNext());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_memories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.addMemory:
                if (mLastLocation != null) {
                    Intent intent = new Intent(ShowMemoriesAC.this,
                            AddMemoryAC.class);
                    Bundle extras = new Bundle();
                    extras.putDouble("EXTRA_LAT", mLastLocation.getLatitude());
                    extras.putDouble("EXTRA_LON", mLastLocation.getLongitude());
                    intent.putExtras(extras);
                    startActivity(intent);
                } else {
                    CharSequence options[] = new CharSequence[]{"Aceptar", "Cancelar"};
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ShowMemoriesAC.this);
                    dialog.setTitle("Debes activar la localización para añadir recuerdos.");
                    dialog.setNegativeButton("Cancelar", null);
                    dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                            //get gps
                        }
                    });

                    dialog.show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * GOOGLE CONNECTION*
     */

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i("GOOGLE_LOCATION", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i("GOOGLE_LOCATION", "Connection suspended");
        mGoogleApiClient.connect();
    }
}
