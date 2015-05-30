package org.adriarios.memshapp.activities.offline;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.adriarios.memshapp.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordAudioAC extends ActionBarActivity {
    final String STATE_READY_FOR_RECORD = "STATE_READY_FOR_RECORD";
    final String STATE_RECORDING = "STATE_RECORDING";
    final String STATE_RECORDED = "STATE_RECORDED";

    String currentState;

    ImageButton mRecAndStopAudioButton;
    ImageButton mPlayAudioButton;
    ImageView mMicroImage;
    String mFileName = null;
    MediaPlayer mPlayer = null;
    MediaRecorder mRecorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio_ac);

        mPlayAudioButton = (ImageButton) findViewById(R.id.playButton);
        mPlayAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPlayButtonClick();
            }
        });
        mRecAndStopAudioButton = (ImageButton) findViewById(R.id.recButton);
        mRecAndStopAudioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRecAndStopButtonClick();
            }

        });

        mMicroImage = (ImageView)findViewById(R.id.microImageView);

        currentState = STATE_READY_FOR_RECORD;

        initCustomMenu();
        setStateReadyForRecord();
    }

    private void setStateReadyForRecord (){
        currentState = STATE_READY_FOR_RECORD;
        mMicroImage.setAlpha(0.5f);
        Drawable playInactive = getResources().getDrawable(R.drawable.play_off);
        mPlayAudioButton.setImageDrawable(playInactive);
        mPlayAudioButton.setEnabled(false);

        Drawable recActive = getResources().getDrawable(R.drawable.rec);
        mRecAndStopAudioButton.setImageDrawable(recActive);
        mRecAndStopAudioButton.setEnabled(true);

    }

    private void setStateRecording (){
        currentState = STATE_RECORDING;
        mMicroImage.setAlpha(1.0f);
        Drawable playInactive = getResources().getDrawable(R.drawable.play_off);
        mPlayAudioButton.setImageDrawable(playInactive);
        mPlayAudioButton.setEnabled(false);

        Drawable stopActive = getResources().getDrawable(R.drawable.stop);
        mRecAndStopAudioButton.setImageDrawable(stopActive);
        mRecAndStopAudioButton.setEnabled(true);
    }

    private void setStateRecorded (){
        currentState = STATE_RECORDED;
        mMicroImage.setAlpha(0.5f);
        Drawable playActive = getResources().getDrawable(R.drawable.play_on);
        mPlayAudioButton.setImageDrawable(playActive);
        mPlayAudioButton.setEnabled(true);

        Drawable recActive = getResources().getDrawable(R.drawable.rec);
        mRecAndStopAudioButton.setImageDrawable(recActive);
        mRecAndStopAudioButton.setEnabled(true);
    }


    private void onPlayButtonClick() {
        mPlayer = new MediaPlayer();
        mPlayAudioButton.setEnabled(false);
        Drawable recInactive = getResources().getDrawable(R.drawable.rec_off);
        mRecAndStopAudioButton.setImageDrawable(recInactive);
        mRecAndStopAudioButton.setEnabled(false);
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new  MediaPlayer.OnCompletionListener() {
                public  void  onCompletion(MediaPlayer mediaPlayer) {
                    mPlayAudioButton.setEnabled(true);
                    Drawable recActive = getResources().getDrawable(R.drawable.rec);
                    mRecAndStopAudioButton.setImageDrawable(recActive);
                    mRecAndStopAudioButton.setEnabled(true);
                }
            });
        } catch (IOException e) {
            //Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void onRecAndStopButtonClick() {
        switch (currentState) {
            case STATE_READY_FOR_RECORD:
            case STATE_RECORDED:
                setStateRecording();
                startRecording();
                break;
            case STATE_RECORDING:
                setStateRecorded();
                stopRecording();
                break;
        }
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
