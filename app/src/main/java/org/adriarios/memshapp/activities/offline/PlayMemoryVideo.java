package org.adriarios.memshapp.activities.offline;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import org.adriarios.memshapp.R;

public class PlayMemoryVideo extends ActionBarActivity {

    VideoView mVideoView;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomMenu();
        setContentView(R.layout.activity_play_memory_video);

        mVideoView = (VideoView) findViewById(R.id.memoryVideo);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        mVideoView.setVideoURI(Uri.parse(extras.getString("DETAILS_VIDEO_PATH")));
        mediaController = new MediaController(this);
        mediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediaController);
        mVideoView.seekTo(1);
        mVideoView.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_play_memory_video, menu);
        return true;
    }


    private void initCustomMenu(){
        android.support.v7.app.ActionBar myActionVarSupport = getSupportActionBar();
        myActionVarSupport.setDisplayShowHomeEnabled(false);
        myActionVarSupport.setDisplayShowTitleEnabled(false);
        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.menu_play_memory_video_inflate, null);

        ImageButton imageButton = (ImageButton) mCustomView
                .findViewById(R.id.backToLastActivity);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                PlayMemoryVideo.this.onBackPressed();
            }
        });
        myActionVarSupport.setCustomView(mCustomView);
        myActionVarSupport.setDisplayShowCustomEnabled(true);

    }
}
