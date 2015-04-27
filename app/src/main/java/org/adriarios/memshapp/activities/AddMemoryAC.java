package org.adriarios.memshapp.activities;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.ImageButton;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.contentprovider.MemoriesProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AddMemoryAC extends ActionBarActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;
    static final int REQUEST_AUDIO_CAPTURE = 3;
    static final int REQUEST_IMAGE_GALLERY_CAPTURE = 4;
    //Controls
    ImageButton mImageButton;

    ImageButton mVideoButton;

    ImageButton mRecordAudio;


    EditText mTitleET;
    EditText mDescriptionET;


    ContentResolver contentResolver;
    String mCurrentAudioPath = null;
    String mCurrentPhotoPath;
    String mCurrentVideoPath;
    String mTime;
    Double mLatitude;
    Double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memories);

        initCustomMenu();

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mLatitude = extras.getDouble("EXTRA_LAT");
        mLongitude = extras.getDouble("EXTRA_LON");

        //Parte del botón para capturar audio
        //Test añadir comentario
        mDescriptionET = (EditText) findViewById(R.id.descriptionET);
        mTitleET = (EditText) findViewById(R.id.titleET);


        //Parte del botón para capturar video
        mVideoButton = (ImageButton) findViewById(R.id.videoView);
        mVideoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakeVideoIntent();
            }

        });


        //Parte de la imagen
        mImageButton = (ImageButton) findViewById(R.id.imageView);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CharSequence colors[] = new CharSequence[]{"Cámara", "Galería"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddMemoryAC.this);
                builder.setTitle("Escoge una opción");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                        switch (option) {
                            case 0:
                                dispatchTakePictureIntent();
                                break;
                            case 1:
                                dispatchTakeGalleryPictureIntent();
                                break;
                            default:
                        }
                        // the user clicked on colors[which]
                    }
                });
                builder.show();

            }

        });

        //Parte del audio

        mRecordAudio = (ImageButton) findViewById(R.id.recordView);
        mRecordAudio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(AddMemoryAC.this, RecordAudioAC.class);
                startActivityForResult(intent, REQUEST_AUDIO_CAPTURE);
            }

        });

    }

    private void initCustomMenu() {
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_add_memories_inflate, null);

        ImageButton imageButton = (ImageButton) mCustomView
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
        mTime = getTime();
        // Open the database for writing
        ContentResolver contentResolver = getContentResolver();

        // Create the ContentValues for the data to be saved
        ContentValues values = new ContentValues();
        values.put(MemoriesProvider.MEMORY_TITLE, mTitleET.getText().toString());
        values.put(MemoriesProvider.MEMORY_TEXT, mDescriptionET.getText().toString());
        values.put(MemoriesProvider.MEMORY_AUDIO, mCurrentAudioPath);
        values.put(MemoriesProvider.MEMORY_VIDEO, mCurrentVideoPath);
        values.put(MemoriesProvider.MEMORY_IMAGE, mCurrentPhotoPath);
        values.put(MemoriesProvider.MEMORY_LATITUDE, mLatitude);
        values.put(MemoriesProvider.MEMORY_LONGITUDE, mLongitude);
        values.put(MemoriesProvider.MEMORY_DATE, mTime);

        // Save the data through the ContentProvider
        contentResolver.insert(MemoriesProvider.CONTENT_URI, values);
    }

    private String getTime() {

        Calendar c = Calendar.getInstance();

        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH)+1;
        int year = c.get(Calendar.YEAR);

        String date = paddingZero(day) + "/" + paddingZero(month) + "/" + year;
        return  date;
    }

    private String paddingZero(int number) {
        String output = String.valueOf(number);
        if (number < 10){
            output = "0"+output;
        }
        return output;
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

    //IMAGE FROM GALLERY
    private void dispatchTakeGalleryPictureIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY_CAPTURE);


    }

    //IMAGE FROM CAMERA
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
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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
        }

        if (requestCode == REQUEST_IMAGE_GALLERY_CAPTURE && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mCurrentPhotoPath = cursor.getString(columnIndex);
            }
            cursor.close();
            setPic();
        }

        if (requestCode == REQUEST_AUDIO_CAPTURE && resultCode == RESULT_OK) {
            mCurrentAudioPath = data.getStringExtra("AUDIO_PATH");
            Drawable recordCheckIcon = getResources().getDrawable(R.drawable.upload_record_icon_check);
            mRecordAudio.setImageDrawable(recordCheckIcon);
        }

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Drawable videoCheckIcon = getResources().getDrawable(R.drawable.upload_video_icon_check);
            mVideoButton.setImageDrawable(videoCheckIcon);
        }


    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageButton.getWidth();
        int targetH = mImageButton.getHeight();

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
        mImageButton.setImageBitmap(bitmap);
    }

    //Métodos por defecto

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_memories, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.addMemoryButton) {
            startMemoryOnBBDD();
            Intent intent = new Intent(AddMemoryAC.this,
                    ShowMemoriesAC.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }
}
