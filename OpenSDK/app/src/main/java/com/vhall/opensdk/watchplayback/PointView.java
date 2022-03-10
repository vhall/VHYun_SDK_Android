package com.vhall.opensdk.watchplayback;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.vhall.opensdk.R;

public class PointView extends ConstraintLayout {

    private ImageView image;
    private TextView message;
    private Context context;
    private CountDownTimer countDownTimer = new CountDownTimer(5000, 5000) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            setVisibility(View.GONE);
        }
    };

    public PointView(Context context) {
        this(context, null);
    }

    public PointView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PointView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.point_view, this, true);
        image = view.findViewById(R.id.image);
        message = view.findViewById(R.id.msg);
    }

    public void showInfo(PointInfo info) {
        Glide.with(context).load(info.picurl).into(image);
        message.setText(info.msg);
        countDownTimer.cancel();
        countDownTimer.start();
    }
}
