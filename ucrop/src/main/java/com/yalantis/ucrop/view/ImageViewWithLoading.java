package com.yalantis.ucrop.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.yalantis.ucrop.R;

/**
 * Created by franciscomagalhaes on 11/06/17.
 */

public class ImageViewWithLoading extends FrameLayout {
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public ImageViewWithLoading(@NonNull Context context) {
        super(context);
        init();
    }

    public ImageViewWithLoading(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageViewWithLoading(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageViewWithLoading(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.ucrop_view_preview_image, this);

        mImageView = (ImageView) rootView.findViewById(R.id.image);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressbar);
    }

    public void setImageDrawable(@Nullable Drawable drawable) {
        mImageView.setImageDrawable(drawable);

        boolean isImageVisible = drawable != null;

        mImageView.setVisibility(isImageVisible ? VISIBLE : INVISIBLE);
        mProgressBar.setVisibility(isImageVisible ? INVISIBLE : VISIBLE);
    }
}
