package org.adriarios.memshapp.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.adriarios.memshapp.models.ImagesDataModel;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

/**
 * Created by Adrian on 23/03/2015.
 */
public class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
    private  String mCurrentPhotoPath;
    private final WeakReference<ImageView> imageViewReference;
    private int data = 0;

    public BitmapWorkerTask(ImageView imageView,String mCurrentPhotoPath) {
        this.mCurrentPhotoPath = mCurrentPhotoPath;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        //data = params[0];
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        // Determine how much to scale down the image
        int scaleFactor = 5;

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        bitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        // bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

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
