package com.vhall.opensdk.interactive;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vhall.opensdk.R;

import java.util.Map;


/**
 * Created by huanan on 2017/5/15.
 */
public class InfoPopu extends PopupWindow {
    private static final String TAG = "InfoPopu";
    Context mContext;
    TextView tv_roomid, tv_role, tv_config, tv_pix, tv_bitrate, tv_framerate, tv_cpu, tv_version;
    String roomid, role;

    public InfoPopu(Context context, String roomid, String role) {
        super(context);
        this.mContext = context;
        this.role = role;
        this.roomid = roomid;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.info_popu, null);
        setContentView(root);
        tv_roomid = root.findViewById(R.id.tv_roomid);
        tv_role = root.findViewById(R.id.tv_role);
        tv_config = root.findViewById(R.id.tv_config);
        tv_pix = root.findViewById(R.id.tv_pix);
        tv_bitrate = root.findViewById(R.id.tv_bitrate);
        tv_framerate = root.findViewById(R.id.tv_framerate);
        tv_cpu = root.findViewById(R.id.tv_cpu);
        tv_version = root.findViewById(R.id.tv_version);
    }

    public void refreshData(Map<String, String> map) {
        tv_roomid.setText("房间ID：" + roomid);
        tv_role.setText("角色：" + role);
        tv_config.setText("配置：" + map.get("pixtype"));
        tv_pix.setText("编码格式：" + map.get("googFrameWidthSent") + "*" + map.get("googFrameHeightSent"));
        tv_bitrate.setText("码率：" + map.get("bytesSent"));
        tv_framerate.setText("帧率：" + map.get("googFrameRateSent"));
        tv_cpu.setText("CPU占用率：" + "");
        tv_version.setText("SDK版本：1.2");
    }

}
