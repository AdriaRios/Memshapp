package org.adriarios.memshapp.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.adriarios.memshapp.models.ImagesDataModel;

import java.lang.ref.WeakReference;

/**
 * Created by Adrian on 23/03/2015.
 */
public class LoadImageMiniWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private Integer imageW;
    private Integer imageH;
    private  String mCurrentPhotoPath;
    private final WeakReference<ImageView> imageViewReference;

    public LoadImageMiniWorkerTask(ImageView imageView, String mCurrentPhotoPath, Integer imageW, Integer imageH) {
        this.imageH = imageH;
        this.imageW = imageW;
        this.mCurrentPhotoPath = mCurrentPhotoPath;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        // Get the dimensions of the View

        int targetW = imageW;
        int targetH = imageH;
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.max(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        return bitmap;
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                ImagesDataModel.getInstance().addBitmapToMemoryCache(mCurrentPhotoPath, bitmap);
            }
        }
    }
}
