package com.vhall.opensdk.interactive;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.ConfigActivity;
import com.vhall.opensdk.R;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.RendererCommon;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InteractiveFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "InteractiveFragment";
    Context mContext;
    HorizontalScrollView hsv_streams;
    LinearLayout mLayoutGroup;//远程流渲染view存放地
    VHRenderView localView;//本地流渲染view
    Stream localStream;
    Button mReqBtn, mJoinBtn, mQuitBtn, mMemberBtn;
    AlertDialog mDialog;
    ImageView mSwitchCameraBtn, mInfoBtn;
    CheckBox mBroadcastTB, mVideoTB, mAudioTB, mDualTB;
    TextView mOnlineTV;

    Handler mHandler = new Handler();
    public String mRoomId;
    public String mAccessToken;
    VHInteractive interactive = null;
    boolean isEnable = false;//是否可用
    boolean isOnline = false;//是否上麦
    String mBroadcastid = "";
    int mDefinition = 0;
    MemberPopu mMemberPopu;
    ActionPopu mActionPopu;
    InfoPopu mInfoPopu;

    public static InteractiveFragment getInstance(String roomid, String accessToken) {
        InteractiveFragment fragment = new InteractiveFragment();
        fragment.mRoomId = roomid;
        fragment.mAccessToken = accessToken;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_interactive, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        //取配置
        SharedPreferences sp = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
        mBroadcastid = sp.getString(ConfigActivity.KEY_BROCASTID, "");
        mDefinition = sp.getInt(ConfigActivity.KEY_PIX_TYPE, 0);
        interactive = new VHInteractive(mContext, new RoomListener());
        interactive.setOnMessageListener(new MyMessageListener());
        initLocalView();
        initLocalStream();
        interactive.init(mRoomId, mAccessToken, new VHInteractive.InitCallback() {
            @Override
            public void onSuccess() {
                isEnable = true;
                ClickEventEnter();
                refreshMembers();
            }

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                isEnable = false;
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initLocalView() {
        localView.init(null, null);
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
    }

    private void initLocalStream() {
        localStream = interactive.createLocalStream(Stream.VhallFrameResolutionValue.VhallFrameResolution320x240.getValue(), "paassdk");
        localView.setStream(localStream);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_join:
                clickEventJoin();
                break;
            case R.id.btn_quit:
                clickEventQuit();
                break;
            case R.id.btn_request:
                clickEventReq();
                break;
            case R.id.iv_camera:
                localStream.switchCamera();
                break;
            case R.id.btn_members:
                showMember(true, null);
                break;
            case R.id.iv_info:
                showInfo(true, null);
                break;
        }
    }

    public void ClickEventEnter() {//进入房间
        if (!isEnable)
            return;
        interactive.enterRoom("userdata");
    }

    public void clickEventReq() {//申请上麦
        if (!isEnable)
            return;
        interactive.requestPublish(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    final JSONObject obj = new JSONObject(result);
                    int code = obj.optInt("code");
                    if (code != 200) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, obj.optString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clickEventJoin() {//上麦
        if (!isEnable)
            return;
        if (isOnline)
            return;
        if (!interactive.isPushAvailable()) {
            Toast.makeText(mContext, "无上麦权限", Toast.LENGTH_SHORT).show();
            return;
        }
        interactive.publish();
        localStream.startStats(new Stream.StatsCallback() {
            @Override
            public void onResponse(String s, long l, final Map<String, String> map) {
                if (s.equals("video")) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            map.put("pixtype", getPixName(mDefinition));
                            showInfo(false, map);
                        }
                    });

                }
            }
        });
    }

    public void clickEventQuit() {//下麦
        if (!isEnable)
            return;
        if (isOnline)
            interactive.unpublish();
    }

    public void clickEventLeave() {//离开互动房间
        if (!isEnable)
            return;
        interactive.leaveRoom();
    }

    public void refreshMembers() {
        interactive.getMembers(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.i(TAG, "members:" + res);
                try {
                    JSONObject result = new JSONObject(res);
                    String msg = result.optString("msg");
                    int code = result.optInt("code");
                    if (code == 200) {
                        JSONObject data = result.getJSONObject("data");
                        JSONArray list = data.getJSONArray("lists");
                        if (list != null && list.length() > 0) {
                            final List<Member> members = new LinkedList<>();
                            for (int i = 0; i < list.length(); i++) {
                                JSONObject obj = list.getJSONObject(i);
                                Member member = new Member();
                                member.userid = obj.getString("third_party_user_id");
                                member.status = obj.getInt("status");
                                members.add(member);
                            }
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    showMember(false, members);
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class RoomListener implements Room.RoomDelegate {

        @Override
        public void onDidConnect(Room room, JSONObject jsonObject) {//进入房间
            Log.i(TAG, "onDidConnect");
            //订阅房间内的其他流
            for (Stream stream : room.getRemoteStreams()) {
                room.subscribe(stream);
            }
        }

        @Override
        public void onDidError(Room room, Room.VHRoomErrorStatus vhRoomErrorStatus, String s) {//进入房间失败
            Log.i(TAG, "onDidError");
            for (Stream stream : room.getRemoteStreams()) {
                removeStream(stream);
            }
        }

        @Override
        public void onDidPublishStream(Room room, Stream stream) {//上麦
            Log.i(TAG, "onDidPublishStream");
            isOnline = true;
        }

        @Override
        public void onDidUnPublishStream(Room room, Stream stream) {//下麦
            Log.i(TAG, "onDidUnPublishStream");
            isOnline = false;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setSpeakerphoneOn(true);
                }
            });
        }

        @Override
        public void onDidSubscribeStream(Room room, Stream stream) {//订阅其他流
            Log.e(TAG, "onDidSubscribeStream" + stream.streamId);
            addStream(stream);
        }

        @Override
        public void onDidUnSubscribeStream(Room room, Stream stream) {//取消订阅
            Log.i(TAG, "onDidUnSubscribeStream");
            removeStream(stream);
        }

        @Override
        public void onDidChangeStatus(Room room, Room.VHRoomStatus vhRoomStatus) {//状态改变
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 异常退出
                    //TODO 销毁页面
                    Log.e(TAG, "VHRoomStatusDisconnected");
                    break;
                case VHRoomStatusError:
                    Log.e(TAG, "VHRoomStatusError");
                    openErrorDialog();
                    break;
                case VHRoomStatusReady:
                    Log.e(TAG, "VHRoomStatusReady");
                    break;
                case VHRoomStatusConnected: // 重连进房间
                    removeAllStream();
                    if (isOnline) { // 当房间重连,如果之前已经上麦,则重连后自动上麦,如果之前没有上麦,则点击按钮上麦
                        isOnline = false;
                        clickEventJoin();
                    }
                    Log.e(TAG, "VHRoomStatusConnected");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDidAddStream(Room room, Stream stream) {//有流加入
            Log.i(TAG, "onDidAddStream");
            room.subscribe(stream);
        }

        @Override
        public void onDidRemoveStream(Room room, Stream stream) {//有流退出
            Log.e(TAG, "onDidRemoveStream : " + stream.streamId);
            removeStream(stream);
        }

        @Override
        public void onDidUpdateOfStream(Stream stream, JSONObject jsonObject) {//流状态更新
            Log.i(TAG, "onDidUpdateOfStream");

        }

        @Override
        public void onReconnect(int i, int i1) {
            Log.e(TAG, "onReconnect" + i + " i1 " + i1);
        }

        @Override
        public void onStreamMixed(JSONObject jsonObject) {

        }
    }

    private void openErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("已离开互动房间");
        builder.setPositiveButton("OK", (dialog, which) -> {
            getActivity().finish();//结束App
        });
        builder.show();
    }

    class MyMessageListener implements VHInteractive.OnMessageListener {

        @Override
        public void onMessage(JSONObject data) {
            try {
                String event = data.getString("inav_event");
                int status = data.optInt("status");
                String userid = data.optString("third_party_user_id");
                switch (event) {
                    case VHInteractive.apply_inav_publish://申请上麦消息
                        showDialog(VHInteractive.apply_inav_publish, userid);
                        break;
                    case VHInteractive.audit_inav_publish://申请审核结果消息
                        if (status == 1) {//批准上麦
                            interactive.publish();
                        } else {
                            Toast.makeText(mContext, "您的上麦请求未通过!", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case VHInteractive.askfor_inav_publish://邀请上麦消息
                        showDialog(VHInteractive.askfor_inav_publish, userid);
                        break;
                    case VHInteractive.kick_inav_stream:
                        Toast.makeText(mContext, "您已被请下麦！", Toast.LENGTH_SHORT).show();
                        break;
                    case VHInteractive.kick_inav:
                        Toast.makeText(mContext, "您已被踢出房间！", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        break;
                    case VHInteractive.user_publish_callback:
                        String action = "";
                        switch (status) {
                            case 1:
                                action = "上麦啦！";
                                break;
                            case 2:
                                action = "下麦啦！";
                                break;
                            case 3:
                                action = "拒绝上麦！";
                                break;
                        }
                        Toast.makeText(mContext, userid + ":" + action, Toast.LENGTH_SHORT).show();
                        break;
                    case VHInteractive.inav_close:
                        Toast.makeText(mContext, "直播间已关闭", Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                        break;

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRefreshMemberState() {
            refreshMembers();
        }

        @Override
        public void onRefreshMembers(JSONObject obj) {
            int onlineNum = obj.optInt("user_online_num");
            mOnlineTV.setText("online:" + onlineNum);
        }
    }

    private void addStream(final Stream stream) {
        if (stream == null)
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setSpeakerphoneOn(true);
                if (changeSteam(stream))
                    return;
                int height = mLayoutGroup.getHeight();
                int ori = getActivity().getRequestedOrientation();
                Log.i(TAG, "ori:" + ori);
                int width = 0;
                if (getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    width = 3 * height / 2;
                } else {
                    width = (2 * height) / 3;
                }
                View view = View.inflate(mContext, R.layout.item_remote_stream, null);
                VHRenderView renderView = view.findViewById(R.id.renderview);
                CheckBox videoBtn = view.findViewById(R.id.cb_video);
                CheckBox audioBtn = view.findViewById(R.id.cb_audio);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(params);
                renderView.init(null, null);
                renderView.setStream(stream);
                view.setTag(stream);
                videoBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)//关闭视频开
                            stream.muteVideo(null);
                        else//关闭视频关
                            stream.unmuteVideo(null);
                    }
                });
                audioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            stream.muteAudio(null);
                        else//关闭视频关
                            stream.unmuteAudio(null);
                    }
                });
                mLayoutGroup.addView(view);
            }
        });
    }

    public boolean changeSteam(Stream stream) {
        if (stream == null)
            return true;
        boolean added = false;
        for (int i = 0; i < mLayoutGroup.getChildCount(); i++) {
            View v = mLayoutGroup.getChildAt(i);
            Stream item = (Stream) v.getTag();
            if (item != null && item.streamId == stream.streamId) {
                VHRenderView renderView = v.findViewById(R.id.renderview);
                renderView.setStream(stream);
                added = true;
                break;
            }
        }
        return added;
    }

    private void removeStream(final Stream stream) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (stream == null)
                    return;
                int childCount = mLayoutGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View view = mLayoutGroup.getChildAt(i);
                    if ((view.getTag()) == stream) {
                        mLayoutGroup.removeView(view);
                        break;
                    }
                }
            }
        });

    }

    private void removeAllStream() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int childCount = mLayoutGroup.getChildCount();
                if (childCount > 0) {
                    mLayoutGroup.removeAllViews();
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        clickEventQuit();
    }

    @Override
    public void onDestroy() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        interactive.release();
        super.onDestroy();
    }


    private void initView() {
        mLayoutGroup = getView().findViewById(R.id.ll_streams);
        localView = getView().findViewById(R.id.localView);
        mJoinBtn = getView().findViewById(R.id.btn_join);
        mQuitBtn = getView().findViewById(R.id.btn_quit);
        mReqBtn = getView().findViewById(R.id.btn_request);
        mBroadcastTB = getView().findViewById(R.id.tb_broadcast);
        mVideoTB = getView().findViewById(R.id.tb_video);
        mAudioTB = getView().findViewById(R.id.tb_audio);
        mDualTB = getView().findViewById(R.id.tb_dual);
        hsv_streams = getView().findViewById(R.id.hsv_streams);
        mSwitchCameraBtn = getView().findViewById(R.id.iv_camera);
        mMemberBtn = getView().findViewById(R.id.btn_members);
        mInfoBtn = getView().findViewById(R.id.iv_info);
        mOnlineTV = getView().findViewById(R.id.tv_online);
        mJoinBtn.setOnClickListener(this);
        mQuitBtn.setOnClickListener(this);
        mReqBtn.setOnClickListener(this);
        mMemberBtn.setOnClickListener(this);
        mSwitchCameraBtn.setOnClickListener(this);
        mInfoBtn.setOnClickListener(this);
        mDualTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                localStream.changeVoiceType(isChecked ? 1 : 0);
            }
        });
        mBroadcastTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (TextUtils.isEmpty(mBroadcastid)) {
                    mBroadcastTB.setChecked(false);
                    Toast.makeText(mContext, "旁路ID为空，无法推旁路", Toast.LENGTH_SHORT).show();
                    return;
                }
                int type = isChecked ? 1 : 2;
                interactive.broadcastRoom(mBroadcastid, type, null);
            }
        });
        mVideoTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)//关闭视频开
                    localStream.muteVideo(null);
                else//关闭视频关
                    localStream.unmuteVideo(null);

            }
        });
        mAudioTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    localStream.muteAudio(null);
                else
                    localStream.unmuteAudio(null);

            }
        });
    }

    /**
     * @param event
     * @param userid
     */
    private void showDialog(String event, final String userid) {
        if (getActivity().isFinishing())
            return;
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        switch (event) {
            case VHInteractive.apply_inav_publish:
                mDialog = builder.setTitle("申请上麦")
                        .setMessage(userid + " 申请上麦，是否批准！")
                        .setNegativeButton("不批", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                interactive.checkPublishRequest(userid, 2, null);
                            }
                        })
                        .setPositiveButton("批准", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                interactive.checkPublishRequest(userid, 1, null);
                            }
                        })
                        .create();
                mDialog.show();
                break;
            case VHInteractive.askfor_inav_publish:
                mDialog = builder.setTitle("邀请上麦")
                        .setMessage(userid + " 邀请您上麦，是否同意！")
                        .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                interactive.refusePublish();
                            }
                        })
                        .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                interactive.publish();
                            }
                        })
                        .create();
                mDialog.show();
                break;
        }


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutGroup.invalidate();
    }

    private void showMember(boolean show, List<Member> data) {
        if (mMemberPopu == null) {
            mMemberPopu = new MemberPopu(mContext);
            mMemberPopu.setItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    showAction((Member) parent.getItemAtPosition(position));
                }
            });
        }
        if (data != null)
            mMemberPopu.refreshData(data);
        if (show)
            mMemberPopu.showAtLocation(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showInfo(boolean show, Map<String, String> map) {
        if (mInfoPopu == null) {
            mInfoPopu = new InfoPopu(mContext, mRoomId, "管理员");
        }
        if (map != null)
            mInfoPopu.refreshData(map);
        if (show)
            mInfoPopu.showAtLocation(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void showAction(Member member) {
        if (mActionPopu == null) {
            mActionPopu = new ActionPopu(mContext);
        }
        mActionPopu.setInteractive(interactive);
        mActionPopu.setMember(member);
        mActionPopu.showAtLocation(getActivity().getWindow().getDecorView().findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }


    private String getPixName(int type) {
        String name = "";
        switch (type) {
            case 0:
                name = "SD";
                break;
            case 1:
                name = "HD";
                break;
            case 2:
                name = "UHD";
                break;

        }
        return name;
    }

    AudioManager audioManager;

    //切换到杨声器播放
    private void setSpeakerphoneOn(boolean on) {
        if (audioManager == null) {
            audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        }
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(on);           //默认为扬声器播放
    }


}
