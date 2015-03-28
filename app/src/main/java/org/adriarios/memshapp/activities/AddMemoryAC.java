package org.adriarios.memshapp.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.contentprovider.MemoriesProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddMemoryAC extends ActionBarActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    static final int REQUEST_TAKE_PHOTO = 1;
    //Controls
    ImageView mImageView;
    VideoView mVideoView;

    Button mVideoButton;
    Button mRecordAudioButton;
    Button mStopAudioButton;
    Button mPlayAudioButton;
    Button mSaveButton;

    EditText mTitleET;
    EditText mDescriptionET;

    MediaRecorder mRecorder = null;
    MediaPlayer mPlayer = null;
    String mFileName = null;

    ContentResolver contentResolver;

    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    Double mLatitude;
    Double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCustomMenu();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mLatitude = extras.getDouble("EXTRA_LAT");
        mLongitude= extras.getDouble("EXTRA_LON");

        //Parte del botón para capturar audio
        //Test añadir comentario
        mDescriptionET = (EditText) findViewById(R.id.descriptionET);
        mTitleET = (EditText) findViewById(R.id.titleET);


        mFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
        mRecordAudioButton = (Button) findViewById(R.id.startAudioButton);
        mRecordAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startRecording();
            }
        });

        mStopAudioButton = (Button) findViewById(R.id.stopAudioButton);
        mStopAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecording();
            }

        });

        mPlayAudioButton = (Button) findViewById(R.id.playAudioButton);
        mPlayAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playAudioRecorded();
            }

        });
        //Parte del botón para capturar video
        mVideoButton = (Button) findViewById(R.id.videoButton);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }

        });


        //Parte de la imagen
        mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }

        });

        //Parte del video
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mVideoView.setVisibility(View.INVISIBLE);

        //Parte guardar en BBDD
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startMemoryOnBBDD();
            }
        });

    }

    private void initCustomMenu(){
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.add_memory_custom_actionbar, null);

        ImageView imageButton = (ImageView) mCustomView
                .findViewById(R.id.backToMemoriesList);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddMemoryAC.this,
                        ShowMemoriesAC.class);

                startActivity(intent);
            }
        });

        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }



    private void startMemoryOnBBDD() {
        // Open the database for writing
        ContentResolver contentResolver = getContentResolver();

        // Create the ContentValues for the data to be saved
        ContentValues values = new ContentValues();
        values.put(MemoriesProvider.MEMORY_TITLE, mTitleET.getText().toString());
        values.put(MemoriesProvider.MEMORY_TEXT, mDescriptionET.getText().toString());
        values.put(MemoriesProvider.MEMORY_AUDIO, mFileName);
        values.put(MemoriesProvider.MEMORY_VIDEO, mCurrentVideoPath);
        values.put(MemoriesProvider.MEMORY_IMAGE, mCurrentPhotoPath);
        values.put(MemoriesProvider.MEMORY_LATITUDE, mLatitude);
        values.put(MemoriesProvider.MEMORY_LONGITUDE, mLongitude);

        // Save the data through the ContentProvider
        contentResolver.insert(MemoriesProvider.CONTENT_URI, values);
    }


    //Audio
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            // Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void playAudioRecorded() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File videoFile = null;
            try {
                videoFile = createImageFile(REQUEST_VIDEO_CAPTURE);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR_FILE", ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (videoFile != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(videoFile));
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    //IMAGE
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile(REQUEST_IMAGE_CAPTURE);
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.e("ERROR_FILE", ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile(int mediaType) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mediaFileName;
        File storageDir;
        File file = null;

        if (mediaType == REQUEST_IMAGE_CAPTURE) {
            mediaFileName = "JPEG_" + timeStamp + "_";
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            file = File.createTempFile(
                    mediaFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            mCurrentPhotoPath = file.getAbsolutePath();
        } else if (mediaType == REQUEST_VIDEO_CAPTURE) {
            mediaFileName = "MP4_" + timeStamp + "_";
            storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);
            file = File.createTempFile(
                    mediaFileName,  /* prefix */
                    ".mp4",         /* suffix */
                    storageDir      /* directory */
            );
            mCurrentVideoPath = file.getAbsolutePath();
        }
        // Save a file: path for use with ACTION_VIEW intents
        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic();
            galleryAddPic();
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            //Uri videoUri = data.getData();
            mVideoView.setVideoURI(Uri.parse(mCurrentVideoPath));
            mVideoView.start();
            mVideoView.setVisibility(View.VISIBLE);
            mVideoButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    //Métodos por defecto

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
