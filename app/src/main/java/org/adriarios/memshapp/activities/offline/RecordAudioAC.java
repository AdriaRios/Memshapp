package org.adriarios.memshapp.activities.offline;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.adriarios.memshapp.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordAudioAC extends ActionBarActivity {
    Button mRecordAudioButton;
    Button mStopAudioButton;
    Button mPlayAudioButton;
    String mFileName = null;
    MediaPlayer mPlayer = null;
    MediaRecorder mRecorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio_ac);

        initCustomMenu();
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
    }

    private void initCustomMenu(){
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_record_audio_inflate, null);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.backToAddMemory);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                resetAudioPanel();
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }


    //Audio
    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        mFileName += "/"+timeStamp+"_audiorecord.3gp";
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_audio_ac, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.confirmAudioButton){
            resetAudioPanel();
            Intent intent = new Intent();
            if (mFileName !=null && mFileName !="") {
                intent.putExtra("AUDIO_PATH", mFileName);
                setResult(RESULT_OK, intent);
            }else{
                setResult(RESULT_CANCELED, intent);
            }
            finish();

        }
        return super.onOptionsItemSelected(item);
    }

    private void resetAudioPanel (){
        if (mRecorder!=null){
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null){
            mPlayer.stop();
            mPlayer = null;
        }
    }

}
