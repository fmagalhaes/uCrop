package com.yalantis.ucrop.util;

import android.app.Activity;

import com.yalantis.ucrop.R;

/**
 * Created by franciscomagalhaes on 27/03/17.
 */

public class AnimationUtils {
    public static void overridePendingTransitionForOnStartActivity(Activity activity) {
        activity.overridePendingTransition(R.anim.ucrop_alpha_0_from_100, R.anim.ucrop_hold);
    }

    public static void overridePendingTransitionForFinishActivity(Activity activity) {
        activity.overridePendingTransition(R.anim.ucrop_hold, R.anim.ucrop_alpha_100_from_0);
    }
}
