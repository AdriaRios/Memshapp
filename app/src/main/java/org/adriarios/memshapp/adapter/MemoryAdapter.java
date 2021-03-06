package org.adriarios.memshapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.adriarios.memshapp.R;
import org.adriarios.memshapp.asynctask.LoadImageMiniWorkerTask;
import org.adriarios.memshapp.models.ImagesDataModel;
import org.adriarios.memshapp.valueobjects.MemoryDataVO;

import java.util.List;

/**
 * Created by Adrian on 22/03/2015.
 */
public class MemoryAdapter extends BaseAdapter {

    // Application context
    Context context;

    // List of memories that will be rendered in the GridView
    List<MemoryDataVO> memoriesList;
    private LruCache<String, Bitmap> mMemoryCache;


    // Set the context and user list from the constructor
    public MemoryAdapter(Context context, List<MemoryDataVO> memoriesList) {
        this.context = context;
        this.memoriesList = memoriesList;


    }

    // AdapterViews call this method to know how many objects are to be displayed
    @Override
    public int getCount() {
        return memoriesList.size();
    }

    // AdapterViews call this method to retrieve an object at the specified position
    @Override
    public Object getItem(int position) {
        return memoriesList.get(position);
    }

    // AdapterViews call this method to get the object's ID at the specified position
    @Override
    public long getItemId(int position) {
        return position;
    }

    // AdapterViews call this method to retrieve the row View that they must
    // display for the object at the specified position
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // The View corresponding to the ListView's row layout
        View currentView = null;

        // If the convertView element is not null, the ListView is asking to
        // recycle the existing View, so there is no need to create the View; just update its content
        if (currentView == null) {
            LayoutInflater inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            currentView = inflater.inflate(R.layout.memory_item, parent, false);
        }
        //Get Memory Data
        MemoryDataVO memoryData = memoriesList.get(position);
        //Set Mini Icons Visibility
        ImageView iconMiniImage = (ImageView) currentView.findViewById(R.id.iconMiniImage);
        if (memoryData.getImagePath()==null){
            iconMiniImage.setVisibility(View.GONE);
        }
        ImageView iconMiniVideo = (ImageView) currentView.findViewById(R.id.iconMiniVideo);
        if (memoryData.getVideoPath()==null){
            iconMiniVideo.setVisibility(View.GONE);
        }
        ImageView iconMiniAudio = (ImageView) currentView.findViewById(R.id.iconMiniAudio);
        if (memoryData.getAudioPath()==null){
            iconMiniAudio.setVisibility(View.GONE);
        }
        ImageView iconMiniText = (ImageView) currentView.findViewById(R.id.iconMiniText);
        if (memoryData.getText()==null||memoryData.getText().length()==0){
            iconMiniText.setVisibility(View.GONE);
        }
        //Set Memory Date
        ((TextView) currentView.findViewById(R.id.dateText)).setText(memoryData.getDate());
        //Set Memory Title
        ((TextView) currentView.findViewById(R.id.textViewTest)).setText(memoryData.getTitle());

        //Set Image
        ImageView mImageView = (ImageView) currentView.findViewById(R.id.imageView2);
        if (memoryData.getImagePath() != null) {
            setPic((mImageView), memoryData.getImagePath());
        } else {
            Drawable defaultImage = context.getResources().getDrawable(R.drawable.icon_mem);
            mImageView.setImageDrawable(defaultImage);
        }

        return currentView;
    }

    private void setPic(ImageView mImageView, String mCurrentPhotoPath) {
        if (ImagesDataModel.getInstance().getBitmapFromMemCache(mCurrentPhotoPath) == null) {
            LoadImageMiniWorkerTask task = new LoadImageMiniWorkerTask(mImageView, mCurrentPhotoPath, 200, 200);
            task.execute();
        } else {
            mImageView.setImageBitmap(ImagesDataModel.getInstance().getBitmapFromMemCache(mCurrentPhotoPath));
        }
    }


}
