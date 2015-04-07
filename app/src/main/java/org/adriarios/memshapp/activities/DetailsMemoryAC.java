package org.adriarios.memshapp.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.asynctask.BitmapWorkerTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailsMemoryAC extends ActionBarActivity {
    //Data
    String mImagePath;
    String mTitleData;
    String mDescriptionData;
    Double mLatitude;
    Double mLongitude;

    //View objetcs
    ImageView mImage;
    TextView mTitle;
    TextView mDescription;
    TextView mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_memory);
        //Init properties
        mImage = (ImageView) findViewById(R.id.imageDetails);
        mTitle = (TextView) findViewById(R.id.titleDetails);
        mDescription = (TextView) findViewById(R.id.descDetails);
        mAddress = (TextView)findViewById(R.id.addressDetails);
        //Get Memory Info
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        mImagePath = extras.getString("DETAILS_IMAGE_PATH");
        mTitleData= extras.getString("DETAILS_TITLE");
        mDescriptionData= extras.getString("DETAILS_DESCRIPTION");
        mLatitude= extras.getDouble("DETAILS_LATITUDE");
        mLongitude= extras.getDouble("DETAILS_LONGITUDE");

        mTitle.setText(mTitleData);
        mDescription.setText(mDescriptionData);

        BitmapWorkerTask task = new BitmapWorkerTask(mImage,mImagePath,400,150);
        task.execute();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(mLatitude, mLongitude, 1);
            if(null!=listAddresses&&listAddresses.size()>0){
                String _Location = listAddresses.get(0).getAddressLine(0);
                mAddress.setText(_Location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_details_memory, menu);
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
