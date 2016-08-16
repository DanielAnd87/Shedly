package com.glenn.hatter.Shedly.model;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by hatter on 2016-08-05.
 */

public class CollapseView {

    private View mView;
    private boolean mCollapsed;
    private boolean mLocked;
    public CollapseView(View view) {
        mView = view;
        mCollapsed = false;
        mLocked = false;
    }

    public void setLocked(boolean locked) {
        mLocked = locked;
    }

    public void expand() {
        if (!mLocked) {
       // mView.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int targetHeight = getTargetHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        mView.getLayoutParams().height = 1;
        mView.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mView.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                mView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / mView.getContext().getResources().getDisplayMetrics().density));
        mView.startAnimation(a);
        setCollapsed(false);
        }
    }

    private int getTargetHeight() {
        return mView.getMeasuredHeight();
    }

    public void collapse() {
        final int initialHeight = getTargetHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    mView.setVisibility(View.GONE);
                }else{
                    mView.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    mView.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / mView.getContext().getResources().getDisplayMetrics().density));
        mView.startAnimation(a);
        setCollapsed(true);
    }

    public boolean isCollapsed() {
        return mCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        mCollapsed = collapsed;
    }
}
