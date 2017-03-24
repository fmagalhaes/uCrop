package com.yalantis.ucrop.model;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created by franciscomagalhaes on 09/03/17.
 */

public class Resolution {
    private final int mWidth;
    private final int mHeight;

    public Resolution(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public Resolution(double width, double height) {
        this((int) width, (int) height);
    }

    public Resolution(Bitmap bitmap) {
        this(bitmap.getWidth(), bitmap.getHeight());
    }

    public Resolution getResolutionWithHeight(int height) {
        return new Resolution(((double) height * getRatio()), height);
    }

    public Resolution getResolutionWithWidth(int width) {
        return new Resolution(width, ((double) width / getRatio()));
    }

    public double getRatio() {
        return (double) mWidth / (double) mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public boolean isValid() {
        return mWidth > 0 && mHeight > 0;
    }

    public boolean isInstagramSquareResolutionSupported() {
        return mWidth % mHeight == 0 && getRatio() <= 10f;
    }

    public void printStackTrace(){
        Log.i(Resolution.class.getSimpleName(), toString());
    }

    @Override
    public String toString() {
        return mWidth + "x" + mHeight + "; ratio: " + getRatio() + "; is: " + isInstagramSquareResolutionSupported();
    }

    public static Resolution withRatioFromHeight(float ratio, int height) {
        return new Resolution(height * ratio, height);
    }

    public static Resolution withRatioFromWidth(float ratio, int width) {
        return new Resolution(width, (int)((float)width / ratio));
    }
}
