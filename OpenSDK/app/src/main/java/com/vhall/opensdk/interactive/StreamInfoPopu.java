package com.vhall.opensdk.interactive;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vhall.opensdk.R;
import com.vhall.vhallrtc.client.Stream;

import java.util.Map;

/**
 * Created by zwp on 2019/3/15
 */
public class StreamInfoPopu extends PopupWindow {
    private static final String TAG = "StreamInfoPopu";
    Context context;
    TextView tvStreamInfo, tvVideoInfo, tvAudoInfo;

    public StreamInfoPopu(Context context) {
        super(context);
        this.context = context;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.pop_stream_info, null);
        setContentView(root);
        tvStreamInfo = root.findViewById(R.id.tv_pop_stream_info_id);
        tvVideoInfo = root.findViewById(R.id.tv_pop_stream_info_video);
        tvAudoInfo = root.findViewById(R.id.tv_pop_stream_info_audio);
    }

    public void refreshData(Stream stream, String type, long speed, Map<String, String> map) {
        tvStreamInfo.setText("userId:" + stream.userId);
        if (type.equals("video")) {
            if(stream.isLocal){
                tvVideoInfo.setText("video:" + speed + "--DPI:" + map.get("googFrameWidthSent") + "*" + map.get("googFrameHeightSent") + "--FPS:" + map.get("googFrameRateSent"));
            }else{
                tvVideoInfo.setText("video:" + speed + "--DPI:" + map.get("googFrameWidthReceived") + "*" + map.get("googFrameHeightReceived") + "--FPS:" + map.get("googFrameRateReceived"));
            }
        } else if (type.equals("audio")) {
            tvAudoInfo.setText("audio:" + speed);
        }
    }


}
