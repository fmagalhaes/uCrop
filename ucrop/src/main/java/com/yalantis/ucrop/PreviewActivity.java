package com.yalantis.ucrop;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.muffin.shared.activities.base.BaseActivity;
import com.muffin.shared.models.Resolution;
import com.muffin.shared.utils.AnimationUtils;
import com.muffin.shared.utils.CrossfadeDrawable;
import com.muffin.shared.utils.ImageCropperManager;
import com.rd.PageIndicatorView;
import com.yalantis.ucrop.adapters.PreviewImagesPagerAdapter;

import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by franciscomagalhaes on 11/06/17.
 */

public class PreviewActivity extends BaseActivity {
    public static final String EXTRA_IMAGE_URI = "extra_image_uri";
    public static final String EXTRA_RESULT_ASPECT_RATIO = "extra_result_aspect_ratio";
    public static final String EXTRA_IMAGE_WIDTH = "extra_image_width";
    public static final String EXTRA_IMAGE_HEIGHT = "extra_image_height";

    private @ColorInt int mPanoramaCropColor;

    private FrameLayout mToolbarContainerFrameLayout;
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private PageIndicatorView mPageIndicator;
    private ProgressBar mProgressBar;

    private PreviewImagesPagerAdapter<BitmapDrawable> mImagesPagerAdapter;

    private Uri mImageToPreviewUri;

    private @ColorInt int[] mSwatchesColors;
    private CrossfadeDrawable[] mCrossfadeDrawables;

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
        initAppBar();
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mPageIndicator = (PageIndicatorView) findViewById(R.id.pageIndicatorView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
    }

    @Override
    protected void populateViews() {
        mPanoramaCropColor = getResources().getColor(R.color.colorAccent);

        mPageIndicator.setVisibility(View.INVISIBLE);
        mPageIndicator.setScaleFactor(0.75f);
        mPageIndicator.setPadding(6);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mProgressBar.setIndeterminateTintList(ColorStateList.valueOf(Color.WHITE));
        }
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap imageBitmap = BitmapFactory.decodeFile(mImageToPreviewUri.getPath());
                Resolution imageResolution = new Resolution(imageBitmap);

                int count = ImageCropperManager.getSquareWidthCountForResolution(imageResolution);

                mSwatchesColors = new int[count];
                mCrossfadeDrawables = new CrossfadeDrawable[count - 1];

                final List<BitmapDrawable> bitmapList = ImageCropperManager.splitBitmap(imageBitmap, count, getResources());
                for (int index = 0; index < bitmapList.size(); index++) {
                    final Bitmap bitmap = bitmapList.get(index).getBitmap();

                    if (bitmap != null && !bitmap.isRecycled()) {
                        Palette palette = Palette.from(bitmap).generate();

                        Palette.Swatch darkSwatch = palette.getDarkVibrantSwatch();
                        if (darkSwatch == null) {
                            darkSwatch = palette.getDarkMutedSwatch();
                        }

                        if(darkSwatch != null) {
                            mSwatchesColors[index] = darkSwatch.getRgb();
                        } else {
                            mSwatchesColors[index] = mPanoramaCropColor;
                        }

                        if (index > 0) {
                            if (mCrossfadeDrawables[index - 1] == null) {
                                mCrossfadeDrawables[index - 1] = new CrossfadeDrawable();

                                int baseColor = mSwatchesColors[index - 1];
                                int fadingColor = mSwatchesColors[index];

                                mCrossfadeDrawables[index - 1].setBase(new ColorDrawable(baseColor));
                                mCrossfadeDrawables[index - 1].setFading(new ColorDrawable(fadingColor));
                            }
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mImagesPagerAdapter = new PreviewImagesPagerAdapter<>(bitmapList);
                        mViewPager.setAdapter(mImagesPagerAdapter);
                        mPageIndicator.setCount(bitmapList.size());
                        mPageIndicator.setVisibility(View.VISIBLE);

                        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                                if (position < mCrossfadeDrawables.length) {
                                    mToolbarContainerFrameLayout.setBackground(getCrossfadeDrawable(position, positionOffset));
                                }
                            }

                            @Override
                            public void onPageSelected(int position) {
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {

                            }
                        });

                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private void setResultUriOk() {
        setResultUriFromIntent(RESULT_OK);
    }

    private void setResultUriCanceled() {
        setResultUriFromIntent(RESULT_CANCELED);
    }

    private void setResultUriFromIntent(int resultCode) {
        Intent intent = getIntent();

        if (!intent.hasExtra(EXTRA_RESULT_ASPECT_RATIO) || !intent.hasExtra(EXTRA_IMAGE_WIDTH) || !intent.hasExtra(EXTRA_IMAGE_HEIGHT)) {
            if (Fabric.isInitialized()) {
                Crashlytics.logException(new RuntimeException("intent doesn't have all extras!"));
            }
        }
        float resultAspectRatio = intent.getFloatExtra(EXTRA_RESULT_ASPECT_RATIO, 0);
        int imageWidth = intent.getIntExtra(EXTRA_IMAGE_WIDTH, 0);
        int imageHeight = intent.getIntExtra(EXTRA_IMAGE_HEIGHT, 0);

        setResultUri(resultCode, mImageToPreviewUri, resultAspectRatio, imageWidth, imageHeight);
    }

    protected void setResultUri(int resultCode, Uri uri, float resultAspectRatio, int imageWidth, int imageHeight) {
        setResult(resultCode, new Intent()
                .putExtra(EXTRA_IMAGE_URI, uri)
                .putExtra(EXTRA_RESULT_ASPECT_RATIO, resultAspectRatio)
                .putExtra(EXTRA_IMAGE_WIDTH, imageWidth)
                .putExtra(EXTRA_IMAGE_HEIGHT, imageHeight)
        );
    }


    private CrossfadeDrawable getCrossfadeDrawable(int position, float positionOffset) {
        mCrossfadeDrawables[position].setProgress(positionOffset);
        return mCrossfadeDrawables[position];
    }

    /**
     * Configures and styles both status bar and toolbar.
     */
    private void initAppBar() {
        int toolbarColor = ContextCompat.getColor(this, R.color.colorAccent);
        int toolbarWidgetColor = Color.WHITE;

        mToolbarContainerFrameLayout = (FrameLayout) findViewById(R.id.toolbar_container);
        mToolbarContainerFrameLayout.setPadding(mToolbarContainerFrameLayout.getPaddingLeft(), getStatusBarHeight(), mToolbarContainerFrameLayout.getPaddingRight(), mToolbarContainerFrameLayout.getPaddingBottom());
        //setStatusBarColor(mStatusBarColor);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        // Set all of the Toolbar coloring
        mToolbarContainerFrameLayout.setBackgroundColor(toolbarColor);
        mToolbar.setTitleTextColor(toolbarWidgetColor);

        final TextView toolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setTextColor(toolbarWidgetColor);
        toolbarTitle.setText(getString(R.string.label_preview_photo));

        // Color buttons inside the Toolbar
        mToolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_arrow_back));

        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
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
        AnimationUtils.overridePendingTransitionForFinishActivity(this);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
