package com.slideview.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by yongfeng on 2016/12/23.
 * Email:2499522170@qq.com
 */
public class SlideView extends TextView {
    private static final long DURATION_SHORT = 100;
    private static final long DURATION_MIDDLE = 260;
    private static final long DURATION_LONG = 300;

    private ViewGroup mParentView;
    private float mDownX;
    private float mMinParentY;
    private float mMaxParentY;
    private float mMiddleParentY;
    private float mStatusBarHeight;
    private ValueAnimator mValueAnimator;

    public SlideView(Context context) {
        this(context, null);
    }

    public SlideView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mValueAnimator = ValueAnimator.ofFloat(mMinParentY, mMaxParentY);
        mValueAnimator.setDuration(DURATION_SHORT);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Object animatedValue = valueAnimator.getAnimatedValue();
                if (animatedValue instanceof Float && mParentView != null) {
                    moveParentView((Float) animatedValue);
                }
            }
        });
    }

    private void moveParentView(float moveY) {
        if (moveY < mMinParentY) moveY = mMinParentY;
        if (moveY > mMaxParentY) moveY = mMaxParentY;
        mParentView.setY(moveY);

        if (mSlideListener != null) mSlideListener.onDrag(mMinParentY, moveY, mMaxParentY);
        if (moveY == mMinParentY) {//open
        } else if (moveY == mMaxParentY) {//close
        } else {//drag
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (mValueAnimator.isRunning()) super.onTouchEvent(motionEvent);
        if (mParentView == null) {
            Rect windowRect = getWindowRect((Activity) getContext());
            mStatusBarHeight = windowRect.top;
            mParentView = (ViewGroup) getParent();
            mMinParentY = windowRect.bottom - mStatusBarHeight - mParentView.getHeight();
            mMaxParentY = windowRect.bottom - mStatusBarHeight - this.getHeight();
            mMiddleParentY = (mMaxParentY - mMinParentY) / 2 + mMinParentY;
        }
        boolean touchEvent = mGestureDetector.onTouchEvent(motionEvent);
        if (touchEvent) return true;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = motionEvent.getY();
                return true;
            case MotionEvent.ACTION_MOVE:
                float moveY = getRealY(motionEvent);
                moveParentView(moveY);
                return true;
            case MotionEvent.ACTION_UP:
                if (mValueAnimator.isRunning()) break;
                float upY = getRealY(motionEvent);
                if (isBetween(mMinParentY, mMiddleParentY, upY)) {
                    startAnimator(upY, mMinParentY, DURATION_SHORT);
                } else if (isBetween(mMiddleParentY, mMaxParentY, upY)) {
                    startAnimator(upY, mMaxParentY, DURATION_SHORT);
                }
                return true;
        }
        return super.onTouchEvent(motionEvent);
    }

    private Rect getWindowRect(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    private GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mValueAnimator.isRunning()) return true;
            float clickY = getRealY(e);
            if (clickY > mMiddleParentY) {
                startAnimator(mMaxParentY, mMinParentY, DURATION_LONG);
            } else if (clickY < mMiddleParentY) {
                startAnimator(mMinParentY, mMaxParentY, DURATION_LONG);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityY) > 100) {
                float e1DownY = getRealY(e1);
                if (isBetween(mMinParentY, mMiddleParentY, e1DownY) && e2.getRawY() - e1.getRawY() > 50) {
                    startAnimator(getRealY(e2), mMaxParentY, DURATION_MIDDLE);
                    return true;
                } else if (isBetween(mMiddleParentY, mMaxParentY, e1DownY) && e2.getRawY() - e1.getRawY() < -50) {
                    startAnimator(getRealY(e2), mMinParentY, DURATION_MIDDLE);
                    return true;
                }
            }
            return false;
        }
    });

    private void startAnimator(float startValue, float endValue, long duration) {
        if (mValueAnimator.isRunning()) mValueAnimator.cancel();
        if (startValue < mMinParentY) startValue = mMinParentY;
        if (startValue > mMaxParentY) startValue = mMaxParentY;
        if (endValue < mMinParentY) endValue = mMinParentY;
        if (endValue > mMaxParentY) endValue = mMaxParentY;

        mValueAnimator.setFloatValues(startValue, endValue);
        mValueAnimator.setDuration(duration);
        mValueAnimator.start();
    }

    private float getRealY(MotionEvent motionEvent) {
        return motionEvent.getRawY() - mStatusBarHeight - mDownX;
    }

    private boolean isBetween(float startValue, float endValue, float realValue) {
        return realValue >= startValue && realValue <= endValue;
    }

    public interface SlideListener {
        void onDrag(float minParentY, float nowParentY, float maxParentY);
    }

    private SlideListener mSlideListener;

    public void setSlideListener(SlideListener slideListener) {
        mSlideListener = slideListener;
    }
}
