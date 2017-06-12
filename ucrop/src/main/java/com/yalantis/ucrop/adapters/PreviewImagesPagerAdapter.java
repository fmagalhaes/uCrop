package com.yalantis.ucrop.adapters;

import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by franciscomagalhaes on 11/06/17.
 */

public class PreviewImagesPagerAdapter<T extends Drawable> extends PagerAdapter {
    private final List<T> mDrawableList;

    public PreviewImagesPagerAdapter(List<T> drawableList) {
        mDrawableList = new ArrayList<>(drawableList);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(container.getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageDrawable(mDrawableList.get(position));
        container.addView(imageView);
        imageView.setTag(position);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return mDrawableList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
