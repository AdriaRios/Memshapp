package org.adriarios.memshapp.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import org.adriarios.memshapp.contentprovider.MemoriesProvider;
import org.adriarios.memshapp.R;
import org.adriarios.memshapp.adapter.MemoryAdapter;
import org.adriarios.memshapp.valueobjects.MemoryDataVO;

import java.util.ArrayList;
import java.util.List;


public class ShowMemoriesAC extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {
    ContentResolver contentResolver;
    List<MemoryDataVO> memoryList;

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
        getMemories();
        memoryAdapter = new MemoryAdapter(this, memoryList);
        // Set the Adapter for the GridView
        gridview.setAdapter(memoryAdapter);
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
        setContentView(R.layout.activity_memories_list);
        gridview = (GridView) findViewById(R.id.gridView);
        buildGoogleApiClient();
    }

    private void getMemories() {
        this.contentResolver = getContentResolver();

        Cursor coursesListCursor = this.contentResolver.query(
                MemoriesProvider.CONTENT_URI,
                new String[]{MemoriesProvider.MEMORY_TITLE,MemoriesProvider.MEMORY_TEXT, MemoriesProvider.MEMORY_IMAGE, MemoriesProvider.MEMORY_AUDIO, MemoriesProvider.MEMORY_VIDEO},
                null,
                null,
                null);
        fromCursor(coursesListCursor);
    }

    private void fromCursor(Cursor cursor) {
        memoryList = new ArrayList<MemoryDataVO>();
        if (cursor.moveToFirst()) {
            do {

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


                memoryList.add(
                        new MemoryDataVO(
                                memoryTitle,
                                memoryDesc,
                                audioPath,
                                videoPath,
                                imagePath,
                                2.02,
                                43.02));
                Log.i("BBDD","FIN");

            } while (cursor.moveToNext());
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_memories_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.addMemory:
                Intent i = new Intent(ShowMemoriesAC.this,
                        AddMemoryAC.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**GOOGLE CONNECTION**/

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
        if (mLastLocation != null) {
            Log.i("GOOGLE_LOCATION", "Latitude: "+mLastLocation.getLatitude());
            Log.i("GOOGLE_LOCATION", "Longitude: "+mLastLocation.getLongitude());

            Toast.makeText(this, "Location: "+mLastLocation.getLatitude()+" "+mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
           // mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            // mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        } else {
            Log.i("GOOGLE_LOCATION", "No location detected");
            Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
        }
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
