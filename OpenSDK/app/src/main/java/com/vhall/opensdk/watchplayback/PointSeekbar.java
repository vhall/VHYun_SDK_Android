package com.vhall.opensdk.watchplayback;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

import java.util.List;

public class PointSeekbar extends AppCompatSeekBar {

    public interface OnPointClickListener {
        void onPoint(PointInfo pointInfo);
    }

    private Paint paint;
    private List<PointInfo> infos;
    private boolean dispatch = true;
    private OnPointClickListener listener;

    public PointSeekbar(Context context) {
        super(context);
        init();
    }

    public PointSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PointSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //创建画笔
        paint = new Paint();
        //设置画笔颜色
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        //设置画笔的样式
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (infos != null) {
            for (PointInfo info : infos) {
                int position = info.timePoint * 1000 * getWidth() / getMax() + getPaddingLeft();
                canvas.drawCircle(position, getHeight() / 2, 10, paint);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dispatch = true;
            int x = (int) (event.getX() - getPaddingLeft()) * getMax() / getWidth();
            PointInfo info = getCurrentTimeInfo(x);
            if (info != null) {
                if (listener != null) {
                    listener.onPoint(info);
                }
                dispatch = false;
                return true;
            }
        }
        return !dispatch || super.dispatchTouchEvent(event);
    }

    /*----------------------------------私有接口--------------------------------------*/

    private PointInfo getCurrentTimeInfo(int time) {
        int interval = 5000;
        int min = time - interval;
        int max = time + interval;
        if (infos != null) {
            for (int i = 0; i < infos.size(); i++) {
                PointInfo info = infos.get(i);
                if (min > info.timePoint * 1000) continue;
                if (max < info.timePoint * 1000) return null;
                return info;
            }
        }
        return null;
    }

    /*----------------------------------私有接口--------------------------------------*/


    /*----------------------------------对外接口--------------------------------------*/

    public void setPoints(List<PointInfo> infos) {
        this.infos = infos;
    }

    public void setOnPointClickListener(OnPointClickListener l) {
        listener = l;
    }

    /*----------------------------------对外接口--------------------------------------*/
}
