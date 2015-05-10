package org.adriarios.memshapp.activities.online;

import android.content.ContentResolver;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.customComponents.ScrollViewCustom;
import org.adriarios.memshapp.customComponents.VideoViewCustom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class DetailsOnLineMemoryAC extends ActionBarActivity implements OnMapReadyCallback {
    //Base Path
    final String BASE_PATH = "http://52.11.144.116/memshapp/";

    //counter
    int loadedElements = 0;
    int elementsToLoad = 0;

    //Data
    String mImageFilePath;
    String mAudioFilePath;
    String mVideoFilePath;
    String mImageRemotePath;
    String mAudioRemotePath;
    String mVideoRemotePath;
    String mTitleData;
    String mDescriptionData;
    String mLocation;
    String mDate;
    Double mLatitude;
    Double mLongitude;
    int mID;

    //Content resolver reference
    ContentResolver contentResolver;

    //View objetcs
    ScrollViewCustom mScrollView;
    ImageView mImage;
    TextView mTitle;
    TextView mDateBox;
    TextView mDescription;
    TextView mAddress;
    VideoViewCustom mVideoView;
    Button mAudioButton;
    ProgressBar progressBar;

    MediaController mediaController;
    MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_on_line_memory_ac);
        //Init custom menu
        initCustomMenu();

        //Init properties
        mScrollView = (ScrollViewCustom) findViewById(R.id.scrollViewDetailsOL);
        mImage = (ImageView) findViewById(R.id.imageDetailsOL);
        mTitle = (TextView) findViewById(R.id.titleDetailsOL);
        mDateBox = (TextView) findViewById(R.id.dateDetailsOL);
        mDescription = (TextView) findViewById(R.id.descDetailsOL);
        mAddress = (TextView) findViewById(R.id.addressDetailsOL);
        mAddress.setVisibility(View.INVISIBLE);
        mVideoView = (VideoViewCustom) findViewById(R.id.videoDetailsOL);
        mVideoView.setVisibility(View.GONE);
        mAudioButton = (Button) findViewById(R.id.playAudioButtonDetailsOL);
        mAudioButton.setVisibility(View.GONE);
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
        mImageRemotePath = extras.getString("DETAILS_IMAGE_PATH");
        mTitleData = extras.getString("DETAILS_TITLE");
        mAudioRemotePath = extras.getString("DETAILS_AUDIO_PATH");
        mVideoRemotePath = extras.getString("DETAILS_VIDEO_PATH");
        mDescriptionData = extras.getString("DETAILS_DESCRIPTION");
        mLatitude = extras.getDouble("DETAILS_LATITUDE");
        mLongitude = extras.getDouble("DETAILS_LONGITUDE");
        mDate = extras.getString("DETAILS_DATE");


        mTitle.setText(mTitleData);
        mDateBox.setText(mDate);
        mDescription.setText(mDescriptionData);
        try {
            initMultimedia();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initMap();
    }


    private void initCustomMenu() {
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_details_memory_online_inflate, null);

        progressBar = (ProgressBar) mCustomView.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.MULTIPLY);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.backToMemoriesListFromDetailsView);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailsOnLineMemoryAC.this,
                        MapActivity.class);

                startActivity(intent);
            }
        });

        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }

    private void initMap() {
        setAddress();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapOL);
        mapFragment.getMapAsync(this);
        mScrollView.addInterceptScrollView(mapFragment.getView());
    }

    private void setAddress() {
        // Create a new Thread to load the address
        new Thread(new Runnable() {

            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {
                    List<Address> listAddresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
                    if (null != listAddresses && listAddresses.size() > 0) {
                        mLocation = listAddresses.get(0).getAddressLine(0);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mLocation != null) {
                    // The Activity.runOnUiThred() method runs in the UIThread
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mAddress.setVisibility(View.VISIBLE);
                            mAddress.setText(mLocation);
                        }
                    });
                }


            }
        }).start(); // Executes the newly created thread


    }


    @Override
    public void onMapReady(GoogleMap map) {
        // Some buildings have indoor maps. Center the camera over
        // the building, and a floor picker will automatically appear.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLatitude, mLongitude), 16));
    }

    private void initMultimedia() throws IOException {
        if (mImageRemotePath == null || mImageRemotePath.isEmpty()) {
            mImage.setVisibility(View.GONE);
        } else {
            Picasso.with(this).load(BASE_PATH + mImageRemotePath).into(mImage);
        }


        if (mVideoRemotePath == null || mVideoRemotePath.isEmpty()) {
            mVideoView.setVisibility(View.GONE);
        } else {
            elementsToLoad++;
            downloadVideo();
        }

        if (mAudioRemotePath == null || mAudioRemotePath.isEmpty()) {
            mAudioButton.setVisibility(View.GONE);
        } else {
            elementsToLoad++;
            downloadAudio();
        }

        if (elementsToLoad > 0){
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void downloadVideo() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count;
                    File file = null;
                    File outputDir = DetailsOnLineMemoryAC.this.getCacheDir(); // context being the Activity pointer
                    file = File.createTempFile("test", ".mp4", outputDir);
                    mVideoFilePath = file.getAbsolutePath();

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(BASE_PATH + mVideoRemotePath).build();
                    Response response = null;
                    response = client.newCall(request).execute();
                    InputStream is = null;
                    is = response.body().byteStream();
                    BufferedInputStream input = new BufferedInputStream(is);
                    OutputStream output = new FileOutputStream(file);

                    byte[] data = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkHideProgressBar();
                            mVideoView.setVisibility(View.VISIBLE);
                            mVideoView.setVideoURI(Uri.parse(mVideoFilePath));
                            mediaController = new MediaController(DetailsOnLineMemoryAC.this);
                            mediaController.setAnchorView(mVideoView);
                            mVideoView.setMediaController(mediaController);
                            mVideoView.seekTo(1);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error de conexión",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }

            }
        }).start(); // Executes the newly created thread
    }

    private void downloadAudio() throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count;
                    File file = null;
                    File outputDir = DetailsOnLineMemoryAC.this.getCacheDir(); // context being the Activity pointer
                    file = File.createTempFile("test", ".3gp", outputDir);
                    mAudioFilePath = file.getAbsolutePath();

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(BASE_PATH + mAudioRemotePath).build();
                    Response response = null;
                    response = client.newCall(request).execute();
                    InputStream is = null;
                    is = response.body().byteStream();
                    BufferedInputStream input = new BufferedInputStream(is);
                    OutputStream output = new FileOutputStream(file);

                    byte[] data = new byte[1024];
                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkHideProgressBar();
                            mAudioButton.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Error de conexión",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                }


            }
        }).start(); // Executes the newly created thread
    }

    private void checkHideProgressBar () {
        loadedElements++;
        if (loadedElements == elementsToLoad){
            progressBar.setVisibility(View.GONE);
        }
    }

    private void playAudioRecorded() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mAudioFilePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_on_line_memory_ac, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
