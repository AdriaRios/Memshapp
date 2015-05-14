package org.adriarios.memshapp.activities.offline;

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
import android.util.Log;
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
import org.adriarios.memshapp.customComponents.VideoViewCustom;
import org.adriarios.memshapp.models.ImagesDataModel;

import java.io.File;
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
        mDateBox = (TextView)findViewById(R.id.dateDetails);
        mDescription = (TextView) findViewById(R.id.descDetails);
        mAddress = (TextView) findViewById(R.id.addressDetails);
        mAddress.setVisibility(View.INVISIBLE);
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
        mDate = extras.getString("DETAILS_DATE");


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
        setAddress();
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mScrollView.addInterceptScrollView(mapFragment.getView());
    }

    private void setAddress (){
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
                if ( mLocation != null ) {
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

    public void run() throws Exception {
        MediaType MEDIA_TYPE_JPG=MediaType.parse("image/jpg");
        MediaType MEDIA_TYPE_3GP=MediaType.parse("audio/3gpp");
        MediaType MEDIA_TYPE_MP4=MediaType.parse("video/mp4");
        final OkHttpClient client = new OkHttpClient();

        File file = new File(mImagePath);
        final RequestBody body = RequestBody.create(MEDIA_TYPE_JPG, file);

        File file2 = new File(mAudioPath);
        final RequestBody body2 = RequestBody.create(MEDIA_TYPE_3GP, file2);

        File file3 = new File(mVideoPath);
        final RequestBody body3 = RequestBody.create(MEDIA_TYPE_MP4, file3);


        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    RequestBody requestBody = new MultipartBuilder().type(MultipartBuilder.FORM)
                            .addFormDataPart("test", "Hello!!")
                            .addFormDataPart("uploadedfile1", "uploadedfile1.jpg",body)
                            .addFormDataPart("uploadedfile2", "uploadedfile2.3gp",body2)
                            .addFormDataPart("uploadedfile3", "uploadedfile3.mp4",body3)
                            .build();

                    Request request = new Request.Builder().url("http://52.11.144.116/uploadMemory.php").post(requestBody).build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    Log.d("Response", response.body().string());


                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {



                    }
                });


            }
        }).start(); // Executes the newly created thread



    }

    private void initMultimedia() {
        if (mImagePath == null){
            mImage.setVisibility(View.GONE);
        }else{
            mImage.setImageBitmap(ImagesDataModel.getInstance().getBitmapFromMemCache(mImagePath));

            try {
                run();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
