package org.adriarios.memshapp.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.asynctask.BitmapWorkerTask;
import org.adriarios.memshapp.contentprovider.MemoriesProvider;
import org.adriarios.memshapp.customComponents.ScrollViewCustom;
import org.adriarios.memshapp.customComponents.VideoViewCustom;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailsMemoryAC extends ActionBarActivity implements OnMapReadyCallback {
    //Data
    String mImagePath;
    String mAudioPath;
    String mVideoPath;
    String mTitleData;
    String mDescriptionData;
    Double mLatitude;
    Double mLongitude;
    int mID;

    //Content resolver reference
    ContentResolver contentResolver;

    //View objetcs
    ScrollViewCustom mScrollView;
    ImageView mImage;
    TextView mTitle;
    TextView mDescription;
    TextView mAddress;
    VideoViewCustom mVideoView;
    Button mAudioButton;

    MediaController mediaController;
    MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_memory);
        //Init custom menu
        initCustomMenu();

        //Init properties
        mScrollView = (ScrollViewCustom) findViewById(R.id.scrollViewDetails);
        mImage = (ImageView) findViewById(R.id.imageDetails);
        mTitle = (TextView) findViewById(R.id.titleDetails);
        mDescription = (TextView) findViewById(R.id.descDetails);
        mAddress = (TextView) findViewById(R.id.addressDetails);
        mVideoView = (VideoViewCustom) findViewById(R.id.videoDetails);
        mAudioButton = (Button) findViewById(R.id.playAudioButtonDetails);
        mAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playAudioRecorded();
            }

        });

        this.contentResolver = getContentResolver();



        mVideoView.setDimensions(900, 900);
        //Get Memory Info
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        mID = extras.getInt("DETAILS_ID");
        mImagePath = extras.getString("DETAILS_IMAGE_PATH");
        mTitleData = extras.getString("DETAILS_TITLE");
        mAudioPath = extras.getString("DETAILS_AUDIO_PATH");
        mVideoPath = extras.getString("DETAILS_VIDEO_PATH");
        mDescriptionData = extras.getString("DETAILS_DESCRIPTION");
        mLatitude = extras.getDouble("DETAILS_LATITUDE");
        mLongitude = extras.getDouble("DETAILS_LONGITUDE");


        mTitle.setText(mTitleData);
        mDescription.setText(mDescriptionData);

        initMultimedia();

        BitmapWorkerTask task = new BitmapWorkerTask(mImage, mImagePath, 400, 150);
        task.execute();

        initMap();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if (null != listAddresses && listAddresses.size() > 0) {
                String _Location = listAddresses.get(0).getAddressLine(0);
                mAddress.setText(_Location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                Intent intent = new Intent(DetailsMemoryAC.this,
                        ShowMemoriesAC.class);

                startActivity(intent);
            }
        });

        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mScrollView.addInterceptScrollView(mapFragment.getView());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Some buildings have indoor maps. Center the camera over
        // the building, and a floor picker will automatically appear.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLatitude, mLongitude), 18));
    }

    private void initMultimedia() {
        if (mImagePath == null){
            mImage.setVisibility(View.GONE);
        }
        if (mVideoPath == null) {
            mVideoView.setVisibility(View.GONE);
        } else {
            mVideoView.setVideoURI(Uri.parse(mVideoPath));
            mediaController = new MediaController(this);
            mediaController.setAnchorView(mVideoView);
            mVideoView.setMediaController(mediaController);
            mVideoView.seekTo(1);


        }

        if (mAudioPath == null) {
            mAudioButton.setVisibility(View.GONE);
        }
    }

    private void playAudioRecorded() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mAudioPath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_memory, menu);
        return true;
    }

    private void removeMemory(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsMemoryAC.this);
        dialog.setTitle("Estás seguro que quieres eliminar este recuerdo?");
        dialog.setNegativeButton("Cancelar", null);
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                CharSequence toastText;
                if (contentResolver.delete(MemoriesProvider.CONTENT_URI, MemoriesProvider.MEMORY_ID + "=" + mID, null) > 0){
                    toastText="Recuerdo eliminado correctamente";
                }else{
                    toastText="Se ha producido un error al eliminar el recuerdo";
                }
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, toastText, duration);
                toast.show();
                returnToShowMemories();
            }
        });

        dialog.show();
    }

    private void returnToShowMemories() {
        Intent intent = new Intent(DetailsMemoryAC.this,
                ShowMemoriesAC.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.removeMemory) {
           removeMemory();

        }

        return super.onOptionsItemSelected(item);
    }

}
