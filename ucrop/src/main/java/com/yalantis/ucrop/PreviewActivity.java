package com.yalantis.ucrop;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.muffin.shared.activities.base.BaseActivity;
import com.muffin.shared.models.Resolution;
import com.muffin.shared.utils.AnimationUtils;
import com.muffin.shared.utils.ImageCropperManager;
import com.yalantis.ucrop.adapters.PreviewImagesPagerAdapter;
import com.yalantis.ucrop.view.ImageViewWithLoading;

import io.fabric.sdk.android.Fabric;

/**
 * Created by franciscomagalhaes on 11/06/17.
 */

public class PreviewActivity extends BaseActivity {
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_RESULT_ASPECT_RATIO = "extra_result_aspect_ratio";
    public static final String EXTRA_IMAGE_WIDTH = "extra_image_width";
    public static final String EXTRA_IMAGE_HEIGHT = "extra_image_height";

    private Toolbar mToolbar;
    private ViewPager mViewPager;

    private PreviewImagesPagerAdapter mImagesPagerAdapter;

    private Uri mImageToPreviewUri;

    public static void launchActivityForResult(Activity activity, Uri imageToPreview, float resultAspectRatio, int imageWidth, int imageHeight, int requestCode) {
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(EXTRA_IMAGE_URI, imageToPreview);
        intent.putExtra(EXTRA_RESULT_ASPECT_RATIO, resultAspectRatio);
        intent.putExtra(EXTRA_IMAGE_WIDTH, imageWidth);
        intent.putExtra(EXTRA_IMAGE_HEIGHT, imageHeight);

        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int layoutId() {
        return R.layout.ucrop_activity_preview;
    }

    @Override
    protected void loadIntentExtras() {
        super.loadIntentExtras();

        mImageToPreviewUri = getIntent().getParcelableExtra(EXTRA_IMAGE_URI);
    }

    @Override
    protected void initViews() {
        setupAppBar();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
    }

    @Override
    protected void populateViews() {
        Bitmap bitmap = BitmapFactory.decodeFile(mImageToPreviewUri.getPath());
        Resolution imageResolution = new Resolution(bitmap);

        int count = ImageCropperManager.getSquareWidthCountForResolution(imageResolution);

        mImagesPagerAdapter = new PreviewImagesPagerAdapter(count);
        mViewPager.setAdapter(mImagesPagerAdapter);

        ImageCropperManager.splitBitmap(bitmap, count, getResources(), new ImageCropperManager.OnDrawableCreatedListener() {
            @Override
            public void onDrawableCreated(Drawable drawable, int index) {
                mImagesPagerAdapter.updateDrawable(index, drawable);

                ImageViewWithLoading imageViewWithLoading = (ImageViewWithLoading) mViewPager.findViewWithTag(index);

                if (imageViewWithLoading != null) {
                    imageViewWithLoading.setImageDrawable(drawable);
                }
            }
        });
    }

    private void setResultUriOk() {
        setResultUriFromIntent(RESULT_OK);
    }

    private void setResultUriCanceled() {
        setResultUriFromIntent(RESULT_CANCELED);
    }

    private void setResultUriFromIntent(int resultCode) {
        Intent intent = getIntent();

        if(!intent.hasExtra(EXTRA_RESULT_ASPECT_RATIO) || !intent.hasExtra(EXTRA_IMAGE_WIDTH) || !intent.hasExtra(EXTRA_IMAGE_HEIGHT)) {
            if(Fabric.isInitialized()) {
                Crashlytics.logException(new RuntimeException("intent doesn't have all extras!"));
            }
        }
        float resultAspectRatio = intent.getFloatExtra(EXTRA_RESULT_ASPECT_RATIO, 0);
        int imageWidth = intent.getIntExtra(EXTRA_IMAGE_WIDTH, 0);
        int imageHeight = intent.getIntExtra(EXTRA_IMAGE_HEIGHT, 0);

        setResultUri(resultCode, mImageToPreviewUri, resultAspectRatio, imageWidth, imageHeight);
    }

    protected void setResultUri(int resultCode, Uri uri, float resultAspectRatio, int imageWidth, int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_RESULT_ASPECT_RATIO, resultAspectRatio)
                .putExtra(EXTRA_IMAGE_WIDTH, imageWidth)
                .putExtra(EXTRA_IMAGE_HEIGHT, imageHeight)
        );
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void setupAppBar() {
        int statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        int toolbarColor = ContextCompat.getColor(this, R.color.colorAccent);
        int toolbarWidgetColor = ContextCompat.getColor(this, android.R.color.white);

        setStatusBarColor(statusBarColor);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        // Set all of the Toolbar coloring
        mToolbar.setBackgroundColor(toolbarColor);
        mToolbar.setTitleTextColor(toolbarWidgetColor);

        final TextView toolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(toolbarWidgetColor);
        toolbarTitle.setText(getString(R.string.label_preview_photo));

        // Color buttons inside the Toolbar
        Drawable stateButtonDrawable = ContextCompat.getDrawable(this, R.drawable.ucrop_ic_cross).mutate();
        stateButtonDrawable.setColorFilter(toolbarWidgetColor, PorterDuff.Mode.SRC_ATOP);
        mToolbar.setNavigationIcon(stateButtonDrawable);

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    /**
     * Sets status-bar color for L devices.
     *
     * @param color - status-bar color
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final Window window = getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.ucrop_menu_activity, menu);

        // Change crop & loader menu icons color to match the rest of the UI colors

        MenuItem menuItemLoader = menu.findItem(R.id.menu_loader);
        Drawable menuItemLoaderIcon = menuItemLoader.getIcon();
        if (menuItemLoaderIcon != null) {
            try {
                menuItemLoaderIcon.mutate();
                menuItemLoaderIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
                menuItemLoader.setIcon(menuItemLoaderIcon);
            } catch (IllegalStateException e) {
                Log.i(PreviewActivity.class.getSimpleName(), String.format("%s - %s", e.getMessage(), getString(R.string.ucrop_mutate_exception_hint)));
            }
            ((Animatable) menuItemLoader.getIcon()).start();
        }

        MenuItem menuItemCrop = menu.findItem(R.id.menu_crop);
        Drawable menuItemCropIcon = ContextCompat.getDrawable(this, R.drawable.ucrop_ic_done);
        if (menuItemCropIcon != null) {
            menuItemCropIcon.mutate();
            menuItemCropIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.white), PorterDuff.Mode.SRC_ATOP);
            menuItemCrop.setIcon(menuItemCropIcon);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_crop).setVisible(true);
        menu.findItem(R.id.menu_loader).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_crop) {
            setResultUriOk();
            finish();
            AnimationUtils.overridePendingTransitionForFinishActivity(this);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResultUriCanceled();
        super.onBackPressed();
    }
}
