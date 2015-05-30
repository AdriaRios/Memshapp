package org.adriarios.memshapp.activities.online;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.activities.offline.PlayMemoryVideo;
import org.adriarios.memshapp.customComponents.ScrollViewCustom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

public class DetailsOnLineMemoryAC extends ActionBarActivity implements OnMapReadyCallback, MediaController.MediaPlayerControl {
    //Base Path
    final String BASE_PATH = "http://52.11.144.116/uploads/";

    //counter
    int loadedElements = 0;
    int elementsToLoad = 0;

    //Data
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
    RelativeLayout mVideoLayout;
    RelativeLayout mAudioLayout;
    ScrollViewCustom mScrollView;
    ImageView mImage;
    TextView mTitle;
    TextView mDateBox;
    TextView mDescription;
    TextView mAddress;
    TextView mPlayAudioText;
    ImageView mAudioButton;
    ProgressBar progressBar;
    ImageView mImageVideoThumbnail;

    MediaController mediaController;
    MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_on_line_memory_ac);
        //Init custom menu
        mediaController = new MediaController(this);
        initCustomMenu();

        //Init properties
        mAudioLayout = (RelativeLayout) findViewById(R.id.audioLayoutOL);
        mAudioLayout.setVisibility(View.GONE);
        mVideoLayout = (RelativeLayout) findViewById(R.id.videoLayoutOL);
        mVideoLayout.setVisibility(View.GONE);

        mScrollView = (ScrollViewCustom) findViewById(R.id.scrollViewDetailsOL);
        mImage = (ImageView) findViewById(R.id.imageDetailsOL);
        mTitle = (TextView) findViewById(R.id.titleDetailsOL);
        mDateBox = (TextView) findViewById(R.id.dateDetailsOL);
        mDescription = (TextView) findViewById(R.id.descDetailsOL);
        mPlayAudioText = (TextView) findViewById(R.id.playAudioTextDetailsOL);
        mAddress = (TextView) findViewById(R.id.addressDetailsOL);
        mAddress.setVisibility(View.INVISIBLE);

        mAudioButton = (ImageView) findViewById(R.id.playAudioButtonDetailsOL);
        mAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playAudioRecorded();
            }

        });

        mImageVideoThumbnail = (ImageView) findViewById(R.id.videoThumbnailOL);
        mImageVideoThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsOnLineMemoryAC.this,
                        PlayMemoryVideo.class);
                Bundle extras = new Bundle();
                extras.putString("DETAILS_VIDEO_PATH", mVideoFilePath);
                intent.putExtras(extras);
                startActivity(intent);
                stopAudio();
            }
        });

        this.contentResolver = getContentResolver();


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
                stopAudio();
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
            elementsToLoad++;
            Picasso.with(this)
                    .load(BASE_PATH + mImageRemotePath).into(mImage, new Callback() {
                @Override
                public void onSuccess() {
                    checkHideProgressBar();
                }

                @Override
                public void onError() {
                    checkHideProgressBar();
                }
            });
        }


        if (mVideoRemotePath == null || mVideoRemotePath.isEmpty()) {
            mVideoLayout.setVisibility(View.GONE);
        } else {
            elementsToLoad++;
            downloadVideo();
        }

        if (mAudioRemotePath == null || mAudioRemotePath.isEmpty()) {
            mAudioLayout.setVisibility(View.GONE);
        } else {
            elementsToLoad++;
            downloadAudio();
        }

        if (elementsToLoad > 0) {
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
                            mVideoLayout.setVisibility(View.VISIBLE);
                            final Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mVideoFilePath,
                                    MediaStore.Images.Thumbnails.MINI_KIND);
                            mImageVideoThumbnail.setImageBitmap(thumb);

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
                            mAudioLayout.setVisibility(View.VISIBLE);
                            mPlayer = new MediaPlayer();
                            try {
                                mPlayer.setDataSource(mAudioFilePath);
                                mPlayer.prepare();
                                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mediaController.setMediaPlayer(DetailsOnLineMemoryAC.this);
                                        mediaController.setEnabled(true);
                                        mediaController.setAnchorView(mAudioLayout);
                                    }
                                });
                                mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    public void onCompletion(MediaPlayer mediaPlayer) {
                                        Drawable playAudioOn = getResources().getDrawable(R.drawable.play_audio_on);
                                        mAudioButton.setImageDrawable(playAudioOn);
                                        mPlayAudioText.setText("REPRODUCIR AUDIO");
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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

    private void checkHideProgressBar() {
        loadedElements++;
        if (loadedElements == elementsToLoad) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void playAudioRecorded() {
        if (!mPlayer.isPlaying()) {
            mPlayAudioText.setText("REPRODUCIENDO...");
            mPlayer.start();
        } else {
            mPlayAudioText.setText("PAUSE");
            mPlayer.pause();
        }
        Drawable playAudioOff = getResources().getDrawable(R.drawable.play_audio_off);
        mAudioButton.setImageDrawable(playAudioOff);
        mediaController.show();
    }

    @Override
    public void onBackPressed() {
        stopAudio();
        super.onBackPressed();
    }

    private void stopAudio() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
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

    @Override
    public void start() {
        mPlayAudioText.setText("REPRODUCIENDO...");
        Drawable playAudioOff = getResources().getDrawable(R.drawable.play_audio_off);
        mAudioButton.setImageDrawable(playAudioOff);
        mPlayer.start();
    }

    @Override
    public void pause() {
        mPlayAudioText.setText("PAUSE");
        Drawable playAudioOff = getResources().getDrawable(R.drawable.play_audio_off);
        mAudioButton.setImageDrawable(playAudioOff);
        mPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

}
