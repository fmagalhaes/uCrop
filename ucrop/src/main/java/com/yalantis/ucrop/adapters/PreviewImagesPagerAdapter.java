package com.yalantis.ucrop.adapters;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.yalantis.ucrop.view.ImageViewWithLoading;

/**
 * Created by franciscomagalhaes on 11/06/17.
 */

public class PreviewImagesPagerAdapter extends PagerAdapter {
    private final Drawable[] mDrawableArray;

    public PreviewImagesPagerAdapter(int count) {
        mDrawableArray = new Drawable[count];
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageViewWithLoading imageViewWithLoading = new ImageViewWithLoading(container.getContext());
        imageViewWithLoading.setImageDrawable(mDrawableArray[position]);

        container.addView(imageViewWithLoading);
        imageViewWithLoading.setTag(position);
        return imageViewWithLoading;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mDrawableArray.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public void updateDrawable(int index, Drawable drawable) {
        mDrawableArray[index] = drawable;
    }

    public void recycleAll() {
        for (Drawable drawable : mDrawableArray) {
            if(drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

                if(bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
    }
}
