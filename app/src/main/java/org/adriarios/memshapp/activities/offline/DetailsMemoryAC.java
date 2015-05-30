package org.adriarios.memshapp.activities.offline;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
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
import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.asynctask.LoadImageWorkerTask;
import org.adriarios.memshapp.contentprovider.MemoriesProvider;
import org.adriarios.memshapp.customComponents.ScrollViewCustom;
import org.adriarios.memshapp.models.ImagesDataModel;
import org.adriarios.memshapp.valueobjects.MemoryDataOnLineVO;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;

public class DetailsMemoryAC extends ActionBarActivity implements OnMapReadyCallback, MediaController.MediaPlayerControl {
    //Data
    String mImagePath;
    String mAudioPath;
    String mVideoPath;
    String mTitleData;
    String mDescriptionData;
    String mLocation;
    String mDate;
    String mCode;
    Double mLatitude;
    Double mLongitude;
    ProgressBar progressBar;
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
    MenuItem mSynchronizeMemory;
    ImageView mAudioButton;
    ImageView mImageVideoThumbnail;

    MediaController mediaController;
    MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_memory);

        mediaController = new MediaController(this);

        //Init custom menu
        initCustomMenu();

        //Init properties
        mAudioLayout = (RelativeLayout) findViewById(R.id.audioLayout);
        mVideoLayout = (RelativeLayout) findViewById(R.id.videoLayout);

        mScrollView = (ScrollViewCustom) findViewById(R.id.scrollViewDetails);
        mImage = (ImageView) findViewById(R.id.imageDetails);
        mTitle = (TextView) findViewById(R.id.titleDetails);
        mDateBox = (TextView) findViewById(R.id.dateDetails);
        mDescription = (TextView) findViewById(R.id.descDetails);
        mPlayAudioText = (TextView) findViewById(R.id.playAudioTextDetails);
        mAddress = (TextView) findViewById(R.id.addressDetails);
        mAddress.setVisibility(View.INVISIBLE);
        //mVideoView = (VideoViewCustom) findViewById(R.id.videoDetails);
        mAudioButton = (ImageView) findViewById(R.id.playAudioButtonDetails);
        mImageVideoThumbnail = (ImageView) findViewById(R.id.videoThumbnail);
        mImageVideoThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsMemoryAC.this,
                        PlayMemoryVideo.class);
                Bundle extras = new Bundle();
                extras.putString("DETAILS_VIDEO_PATH", mVideoPath);
                intent.putExtras(extras);
                startActivity(intent);
                stopAudio();
            }
        });

        mAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playAudioRecorded();
            }

        });

        this.contentResolver = getContentResolver();


        //mVideoView.setDimensions(900, 900);
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
        mDate = extras.getString("DETAILS_DATE");
        mCode = extras.getString("DETAILS_CODE");


        mTitle.setText(mTitleData);
        mDateBox.setText(mDate);
        mDescription.setText(mDescriptionData);

        initMultimedia();

        LoadImageWorkerTask task = new LoadImageWorkerTask(mImage, mImagePath);
        task.execute();

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
                Intent intent = new Intent(DetailsMemoryAC.this,
                        ShowMemoriesAC.class);

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
                .findFragmentById(R.id.map);
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


    private void initMultimedia() {
        if (mImagePath == null) {
            mImage.setVisibility(View.GONE);
        } else {
            mImage.setImageBitmap(ImagesDataModel.getInstance().getBitmapFromMemCache(mImagePath));
        }
        if (mVideoPath == null) {
            mVideoLayout.setVisibility(View.GONE);
        } else {

            new Thread(new Runnable() {

                @Override
                public void run() {
                    final Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mVideoPath,
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageVideoThumbnail.setImageBitmap(thumb);
                        }
                    });
                }
            }).start();


        }

        if (mAudioPath == null) {
            mAudioLayout.setVisibility(View.GONE);
        } else {
            mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(mAudioPath);
                mPlayer.prepare();
                mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaController.setMediaPlayer(DetailsMemoryAC.this);
                        mediaController.setEnabled(true);
                        mediaController.setAnchorView(mAudioLayout);
                    }
                });
                mPlayer.setOnCompletionListener(new  MediaPlayer.OnCompletionListener() {
                    public  void  onCompletion(MediaPlayer mediaPlayer) {
                        Drawable playAudioOn = getResources().getDrawable(R.drawable.play_audio_on);
                        mAudioButton.setImageDrawable(playAudioOn);
                        mPlayAudioText.setText("REPRODUCIR AUDIO");
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void playAudioRecorded() {
        if (!mPlayer.isPlaying()) {
            mPlayAudioText.setText("REPRODUCIENDO...");
            mPlayer.start();
        }else{
            mPlayAudioText.setText("PAUSE");
            mPlayer.pause();
        }
        Drawable playAudioOff = getResources().getDrawable(R.drawable.play_audio_off);
        mAudioButton.setImageDrawable(playAudioOff);
        mediaController.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_memory, menu);
        mSynchronizeMemory = menu.findItem(R.id.synchronizeMemory);

        if (mCode != null) {
            mSynchronizeMemory.setVisible(false);
        }
        return true;
    }

    private void confirmRemoveMemory() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsMemoryAC.this);
        dialog.setTitle("Estás seguro que quieres eliminar este recuerdo?");
        dialog.setNegativeButton("Cancelar", null);
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                removeMemory();
            }
        });

        dialog.show();
    }

    private void removeMemory() {
        if (mCode != null) {
            removeMemoryInRemoteBBDD();
        } else {
            removeMemoryFromLocalBBDDandNotify();
        }

    }

    private void removeMemoryInRemoteBBDD() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request = new Request.Builder()
                            .url("http://52.11.144.116/services.php?deleteMemory&id=" + mCode)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                removeMemoryFromLocalBBDDandNotify();
                            }
                        });
                    }

                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(DetailsMemoryAC.this, "Error al eliminar el recuerdo", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });
                    e.printStackTrace();
                }
            }
        }).start(); // Executes the newly created thread


    }

    private void removeMemoryFromLocalBBDDandNotify() {
        CharSequence toastText;
        if (contentResolver.delete(MemoriesProvider.CONTENT_URI, MemoriesProvider.MEMORY_ID + "=" + mID, null) > 0) {
            toastText = "Recuerdo eliminado correctamente";
        } else {
            toastText = "Se ha producido un error al eliminar el recuerdo";
        }
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toastText, duration);
        toast.show();
        returnToShowMemories();
    }

    private void returnToShowMemories() {
        Intent intent = new Intent(DetailsMemoryAC.this,
                ShowMemoriesAC.class);
        startActivity(intent);
        stopAudio();
    }

    private void stopAudio() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.removeMemory) {
            confirmRemoveMemory();
        } else if (id == R.id.synchronizeMemory) {
            confirmSynchronize();
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmSynchronize() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(DetailsMemoryAC.this);
        dialog.setTitle("Estás seguro que quieres sincronizar este recuerdo?");
        dialog.setNegativeButton("Cancelar", null);
        dialog.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                try {
                    synchronizeMemory();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
    }

    public void synchronizeMemory() throws Exception {
        final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpg");
        final MediaType MEDIA_TYPE_3GP = MediaType.parse("audio/3gpp");
        final MediaType MEDIA_TYPE_MP4 = MediaType.parse("video/mp4");
        final OkHttpClient client = new OkHttpClient();

        mSynchronizeMemory.setVisible(false);
        progressBar.setVisibility(View.VISIBLE);

        SecureRandom random = new SecureRandom();
        final long memoryCode = Math.abs(random.nextLong());


        final MemoryDataOnLineVO currentMemory = new MemoryDataOnLineVO(mID, mTitleData, mDescriptionData, mAudioPath,
                mVideoPath, mImagePath, mLatitude, mLongitude, mDate, "", String.valueOf(memoryCode));

        Gson gson = new Gson();

        // convert java object to JSON format,
        // and returned as JSON formatted string
        final String currentMemoryJSON = gson.toJson(currentMemory);


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    RequestBody body = null;
                    RequestBody body2 = null;
                    RequestBody body3 = null;

                    MultipartBuilder multipartBuilder = new MultipartBuilder();
                    multipartBuilder.type(MultipartBuilder.FORM);
                    multipartBuilder.addFormDataPart("currentMemory", currentMemoryJSON);

                    if (mImagePath != null) {
                        File file = new File(mImagePath);
                        body = RequestBody.create(MEDIA_TYPE_JPG, file);
                        multipartBuilder.addFormDataPart("uploadedfile1", memoryCode + "image.jpg", body);
                    }

                    if (mAudioPath != null) {
                        File file2 = new File(mAudioPath);
                        body2 = RequestBody.create(MEDIA_TYPE_3GP, file2);
                        multipartBuilder.addFormDataPart("uploadedfile2", memoryCode + "audio.3gp", body2);
                    }

                    if (mVideoPath != null) {
                        File file3 = new File(mVideoPath);
                        body3 = RequestBody.create(MEDIA_TYPE_MP4, file3);
                        multipartBuilder.addFormDataPart("uploadedfile3", memoryCode + "video.mp4", body3);
                    }


                    RequestBody requestBody = multipartBuilder.build();

                    Request request = new Request.Builder().url("http://52.11.144.116/uploadMemory.php").post(requestBody).build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(DetailsMemoryAC.this, "Error al sincronizar", Toast.LENGTH_SHORT);
                                toast.show();
                                mSynchronizeMemory.setVisible(true);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                        throw new IOException("Unexpected code " + response);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(DetailsMemoryAC.this, "Recuerdo sincronizado correctamente", Toast.LENGTH_SHORT);
                                toast.show();
                                ContentValues args = new ContentValues();
                                args.put(MemoriesProvider.MEMORY_CODE, String.valueOf(memoryCode));
                                contentResolver.update(MemoriesProvider.CONTENT_URI, args, MemoriesProvider.MEMORY_ID + "=" + mID, null);
                                mSynchronizeMemory.setVisible(false);
                                progressBar.setVisibility(View.INVISIBLE);
                                mCode = String.valueOf(memoryCode);
                            }
                        });
                    }
                    Log.d("Response", response.body().string());


                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(DetailsMemoryAC.this, "Error al sincronizar", Toast.LENGTH_SHORT);
                            toast.show();
                            mSynchronizeMemory.setVisible(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }


            }
        }).start(); // Executes the newly created thread


    }

    /*Media Player Controls*/

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
