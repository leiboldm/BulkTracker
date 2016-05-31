package com.mattleibold.bulktracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Matt on 5/27/2016.
 */
public class ImageLoaderTask extends AsyncTask<Integer, Void, Bitmap> {
    private final WeakReference<ImageView> mImageViewReference;
    private int data = 0;
    private String mFilepath;
    private boolean mSquare;

    public ImageLoaderTask(ImageView imageView, String filepath, boolean square) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        mImageViewReference = new WeakReference<ImageView>(imageView);
        mFilepath = filepath;
        mSquare = square;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Integer... params) {
        ImageView iv = mImageViewReference.get();
        int width = (iv == null) ? 0 : iv.getWidth();
        int height = (iv == null) ? 0 : iv.getHeight();
        return decodeSampledBitmapFromFilepath(mFilepath, width, height, mSquare);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mImageViewReference != null) {
            final ImageView imageView = mImageViewReference.get();
            if (imageView != null) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    imageView.setImageResource(R.drawable.notification_icon);
                }
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromFilepath(String filepath,
                                                         int reqWidth, int reqHeight,
                                                         boolean square) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filepath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap image = null;
        boolean imageDecoded = false;
        int decodeAttemptCount = 0;
        while (!imageDecoded) {
            try {
                image = BitmapFactory.decodeFile(filepath, options);
                imageDecoded = true;
            } catch (OutOfMemoryError error) {
                Log.d("BTLOG", error.getMessage());
                options.inSampleSize *= 4;
                decodeAttemptCount += 1;
                if (decodeAttemptCount > 2) return null;
            }
        }

        float rotation = 0f;
        try {
            ExifInterface exifData = new ExifInterface(filepath);
            int exifRotation = exifData.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (exifRotation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180f;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270f;
                    break;
            }
        } catch (IOException exception) {
            Log.d("BTLOG", "IOException in ExifInterface: " + exception.getMessage());
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        // the width and height of the output image
        int outWidth = image.getWidth();
        int outHeight = image.getHeight();
        // the x and y coordinates of where cropping will start from
        int xStart = 0;
        int yStart = 0;
        if (square) {
            // set xStart, yStart, outWidth, and outHeight so that the image is cropped to a square
            outWidth = Math.min(outWidth, outHeight);
            outHeight = outWidth;
            xStart = (image.getWidth() - outWidth) / 2;
            yStart = (image.getHeight() - outHeight) / 2;
        }

        try {
            image = Bitmap.createBitmap(image, xStart, yStart, outWidth, outHeight,
                    matrix, true);
        } catch (OutOfMemoryError exception) {
            Log.d("BTLOG", "Out of memory error: " + exception.getMessage());
            return null;
        }
        return image;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (reqWidth == 0 || reqHeight == 0) {
            // scale the image to 1080p if we don't know how big the imageView is going to be
            inSampleSize = Math.min(height/1920, width/1920);

        } else if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
