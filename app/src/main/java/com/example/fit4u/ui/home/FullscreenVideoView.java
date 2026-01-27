package com.example.fit4u.ui.home;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullscreenVideoView extends VideoView {

    private int videoW = 0;
    private int videoH = 0;

    public FullscreenVideoView(Context context) {
        super(context);
    }

    public FullscreenVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FullscreenVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoSize(int w, int h) {
        videoW = w;
        videoH = h;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewW = MeasureSpec.getSize(widthMeasureSpec);
        int viewH = MeasureSpec.getSize(heightMeasureSpec);

        if (videoW > 0 && videoH > 0 && viewW > 0 && viewH > 0) {
            float videoRatio = (float) videoW / (float) videoH;
            float viewRatio = (float) viewW / (float) viewH;

            int finalW = viewW;
            int finalH = viewH;

            // CenterCrop: ממלא את כל המסך (עם חיתוך קל אם צריך)
            if (videoRatio > viewRatio) {
                // הווידאו רחב יותר -> נגביל לפי גובה
                finalH = viewH;
                finalW = (int) (viewH * videoRatio);
            } else {
                // הווידאו גבוה יותר -> נגביל לפי רוחב
                finalW = viewW;
                finalH = (int) (viewW / videoRatio);
            }

            setMeasuredDimension(finalW, finalH);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
