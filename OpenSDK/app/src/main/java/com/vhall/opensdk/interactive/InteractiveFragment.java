package com.vhall.opensdk.interactive;


import static android.content.Context.MEDIA_PROJECTION_SERVICE;
import static com.vhall.opensdk.ConfigActivity.KEY_PIX_TYPE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.ConfigActivity;
import com.vhall.opensdk.R;
import com.vhall.opensdk.beautysource.FaceBeautyDataFactory;
import com.vhall.opensdk.util.ListUtil;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.Stream;
import com.vhall.vhallrtc.client.VHRenderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.vhwebrtc.SurfaceViewRenderer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class InteractiveFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "InteractiveFragment";
    Context mContext;
    RecyclerView mStreamContainer;//远程流渲染view容器
    LinearLayoutManager mLayoutManager;
    StreamAdapter mAdapter;//容器Adapter

    VHRenderView localView;//本地流渲染view
    Stream localStream;//本地流
    Button mReqBtn, mJoinBtn, mQuitBtn, mMemberBtn;//操作隐藏，demo默认进入直接上麦
    Button btnScreen;
    AlertDialog mDialog;
    //功能按钮
    ImageView mSwitchCameraBtn, mInfoBtn;
    CheckBox mBroadcastTB, mVideoTB, mAudioTB, mDualTB;
    TextView mOnlineTV, tvScaleType;

    public String mRoomId;
    public String mAccessToken;
    public Boolean isNodelayLiveAudience=false;//无延迟直播观众角色
    VHInteractive interactive = null;
    boolean isEnable = false;//是否可用
    boolean isOnline = false;//是否上麦
    String mRoomAttr = "roomAttr";
    String mBroadcastid = "";
    int mDefinition = 0;
    MemberPopu mMemberPopu;
    ActionPopu mActionPopu;
    StreamInfoPopu streamPop;
    Stream tempLocal;
    Room interactiveRoom;
    int changePosition = -1;
    String[] scaleText = {"fit", "fill", "none"};
    int scaleType = 0;
    private FaceBeautyControlView mFaceBeautyControlView;
    private FaceBeautyDataFactory mFaceBeautyDataFactory;
    private boolean useBeautify = false;

    private MediaProjectionManager mediaProjectionManager = null;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<Stream> streams = (List<Stream>) msg.obj;
                    for (Stream stream : streams) {
                        try {
                            //订阅大小流设置 0 小流 1 大流 默认小流
                            stream.streamOption.put(Stream.kDualKey, 0);
                            //禁流设置 不设置，默认订阅音视频
                            //禁用视频，仅订阅音频
                            stream.muteStream.put(Stream.kStreamOptionVideo, true);
                            //禁用音频，仅订阅视频
//                            stream.muteStream.put(Stream.kStreamOptionAudio, true);
                            interactiveRoom.subscribe(stream); //订阅房间内的其他流
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return false;
        }
    });

    SharedPreferences sp;

    CopyOnWriteArrayList<Stream> mStreams = new CopyOnWriteArrayList<>();

    public static InteractiveFragment getInstance(String roomid, String accessToken) {
        InteractiveFragment fragment = new InteractiveFragment();
        fragment.mRoomId = roomid;
        fragment.mAccessToken = accessToken;
        return fragment;
    }

    public static InteractiveFragment getInstance(String roomid, String accessToken,Boolean isNodelayLiveAudience) {
        InteractiveFragment fragment = new InteractiveFragment();
        fragment.mRoomId = roomid;
        fragment.mAccessToken = accessToken;
        fragment.isNodelayLiveAudience=isNodelayLiveAudience;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
        if (context instanceof InteractiveActivity) {
            useBeautify = ((InteractiveActivity) context).getIntent().getBooleanExtra("beautify", false);
        }
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
        sp = mContext.getSharedPreferences("config", Context.MODE_PRIVATE);
        mBroadcastid = sp.getString(ConfigActivity.KEY_BROADCAST_ID, "");
        mDefinition = sp.getInt(KEY_PIX_TYPE, 0);
        interactive = new VHInteractive(mContext, new RoomListener());
        interactive.setOnMessageListener(new MyMessageListener());
//        VHTool.enableDebugLog(true);
        if(!isNodelayLiveAudience)
        {
            initLocalView();
            initLocalStream();
        }

        mAdapter = new StreamAdapter();
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mStreamContainer.setLayoutManager(mLayoutManager);
        mStreamContainer.setAdapter(mAdapter);
        mStreamContainer.setItemViewCacheSize(16);//最多16路

        mediaProjectionManager = (MediaProjectionManager) getActivity().getSystemService(MEDIA_PROJECTION_SERVICE);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isEnable) {
            initInteractive();
        } else if (!isOnline) {
            interactive.setListener(new RoomListener());
            interactive.enterRoom(mRoomAttr);
            if (localStream != null) {
                localStream.removeAllRenderView();
                localStream.addRenderView(localView);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //离开房间，清空stream列表
        leaveRoom();
        interactive.setListener(null);
        isOnline = false;
    }

    @Override
    public void onDestroy() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        interactive.release();
        super.onDestroy();
    }


    private void initInteractive() {
        interactive.init(mRoomId, mAccessToken,mBroadcastid, isNodelayLiveAudience?VHInteractive.MODE_LIVE:VHInteractive.MODE_RTC,isNodelayLiveAudience?VHInteractive.ROLE_AUDIENCE:VHInteractive.ROLE_HOST,new VHInteractive.InitCallback() {
            @Override
            public void onSuccess() {
                isEnable = true;
                interactive.enterRoom(mRoomAttr);//初始化成功，直接进入房间
                refreshMembers();
            }

            @Override
            public void onFailure(int errorCode, String errorMsg) {
                isEnable = false;
                Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    class RoomListener implements Room.RoomDelegate {

        @Override
        public void onDidConnect(Room room, JSONObject jsonObject) {//进入房间
            Log.i(TAG, "onDidConnect");
            interactiveRoom = room;
            subscribeStreams(room.getRemoteStreams());
            join();//进入房间成功，自动上麦

        }

        /**
         * 受设备硬件及网速原因影响，部分机型一次性加载16路流存在大概率奔溃风险；
         * 进行延时加载处理，demo单次最多加载5路。单次最多8路，8路以上存在风险不建议使用
         *
         * @param streams
         */
        private void subscribeStreams(List<Stream> streams) {
            List<List<Stream>> list = ListUtil.sublistAsNum(streams, 5);
            for (int i = 0; i < list.size(); i++) {
                Message message = new Message();
                message.what = 0;
                message.obj = list.get(i);
                mHandler.sendMessageDelayed(message, i * 1500);
            }
        }

        @Override
        public void onDidError(Room room, Room.VHRoomErrorStatus vhRoomErrorStatus, String s) {//进入房间失败
            Log.i(TAG, "onDidError");
            removeAllStream();
        }

        @Override
        public void onDidPublishStream(Room room, Stream stream) {//上麦
            Log.i(TAG, "onDidPublishStream");
            isOnline = true;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "上麦成功", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onDidUnPublishStream(Room room, Stream stream) {//下麦
            Log.i(TAG, "onDidUnPublishStream");
            isOnline = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "下麦成功", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onDidSubscribeStream(Room room, Stream stream) {//订阅其他流
            Log.i(TAG, "onDidSubscribeStream" + stream.streamId);
            addStream(stream);
        }

        @Override
        public void onDidUnSubscribeStream(Room room, Stream stream) {//取消订阅
            Log.i(TAG, "onDidUnSubscribeStream");
            removeStream(stream);
        }

        @Override
        public void onDidChangeStatus(Room room, Room.VHRoomStatus vhRoomStatus) {//状态改变
            Log.i(TAG, "onDidChangeStatus");
            switch (vhRoomStatus) {
                case VHRoomStatusDisconnected:// 断开连接
                    //TODO 销毁页面
                    removeAllStream();
                    Log.e(TAG, "VHRoomStatusDisconnected");
                    break;
                case VHRoomStatusError:
                    Log.e(TAG, "VHRoomStatusError");
                    openErrorDialog();
                    break;
                case VHRoomStatusReady:
                    Log.e(TAG, "VHRoomStatusReady");
                    break;
                case VHRoomStatusConnected: // 连接成功
                    removeAllStream();
//                    join();// 当房间重连,如果之前已经上麦,则重连后自动上麦
                    Log.e(TAG, "VHRoomStatusConnected");
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onDidAddStream(Room room, Stream stream) {//有流加入
            Log.i(TAG, "onDidAddStream");
            try {
                stream.streamOption.put(Stream.kDualKey, 0);
                stream.muteStream.put(Stream.kStreamOptionVideo, true);
                room.subscribe(stream);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDidRemoveStream(Room room, Stream stream) {//有流退出
            Log.i(TAG, "onDidRemoveStream : " + stream.streamId);
            removeStream(stream);
        }

        @Override
        public void onDidUpdateOfStream(Stream stream, JSONObject jsonObject) {//流状态更新
            Log.i(TAG, "onDidUpdateOfStream");
            JSONObject obj = jsonObject.optJSONObject("muteStream");
            boolean muteAudio = obj.optBoolean("audio");// true 禁音、false 未禁音
            boolean muteVideo = obj.optBoolean("video");// true 禁视频、 false 未禁视频
            //订阅端如需更新标识可自行处理业务逻辑
        }

        /**
         * 互动房间重连
         *
         * @param i  总重连次数
         * @param i1 当前重连次数
         */
        @Override
        public void onReconnect(int i, int i1) {
            Log.e(TAG, "onReconnect" + i + " i1 " + i1);
        }

        @Override
        public void onStreamMixed(JSONObject jsonObject) {

        }
    }


    private void initLocalView() {
        localView.init(null, null);
        localView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
        localView.setMirror(true);
    }

    //初始化本地流
    private void initLocalStream() {
        int layerType = sp.getInt(KEY_PIX_TYPE, 1);
        int pixType = 0;
        JSONObject option = new JSONObject();
        try {
//            switch (layerType) {
//                case 0:
//                    pixType = Stream.VhallFrameResolutionValue.VhallFrameResolution192x144.getValue();
//                    option.put(Stream.kFrameResolutionTypeKey, pixType);
//                    break;
//                case 1:
//                    pixType = Stream.VhallFrameResolutionValue.VhallFrameResolution320x240.getValue();
//                    option.put(Stream.kFrameResolutionTypeKey, pixType);
//                    break;
//                case 2:
//                    //该分辨率下支持双流
//                    pixType = Stream.VhallFrameResolutionValue.VhallFrameResolution480x360.getValue();
//                    option.put(Stream.kFrameResolutionTypeKey, pixType);
//                    //重置双流码率，当前分辨率默认码率仅支持单流
//                    option.put(Stream.kMinBitrateKbpsKey, 200);
//                    option.put(Stream.kCurrentBitrateKey, 400);
//                    option.put(Stream.kMaxBitrateKey, 600);
//                    break;
//            }
//            option.put(Stream.kStreamOptionStreamType, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo.getValue());
//            option.put(Stream.kNumSpatialLayersKey, layerType);//单双流设置 2 双流 其他默认单流


            option.put(Stream.kMinBitrateKbpsKey, 400);
            option.put(Stream.kCurrentBitrateKey, 1200);
            option.put(Stream.kMaxBitrateKey, 1500);
            option.put(Stream.kVideoHeightKey,720);
            option.put(Stream.kVideoWidthKey,1280);
//            option.put(Stream.kFrameResolutionTypeKey, Stream.VhallFrameResolutionValue.VhallFrameResolution640x480.getValue());



//            option.put(Stream.kMinBitrateKbpsKey,225);
//            option.put(Stream.kCurrentBitrateKey, 500);
//            option.put(Stream.kMaxBitrateKey, 600);

            option.put(Stream.kStreamOptionStreamType, Stream.VhallStreamType.VhallStreamTypeAudioAndVideo.getValue());
            option.put(Stream.kNumSpatialLayersKey, 2);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        localStream = interactive.createLocalStream(option, "paassdk");
//        localStream = interactive.createLocalStream(pixType, "paassdk", layerType);
        if (localStream != null) {
            localStream.removeAllRenderView();
            localStream.addRenderView(localView);
        }
        tempLocal = localStream;
    }


    public void join() {//上麦
        if (!interactive.isPushAvailable()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "无权限或观看的无延时直播", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        interactive.publish();
    }

    private void leaveRoom() {
        mStreams.clear();
        mAdapter.notifyDataSetChanged();
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


    private void openErrorDialog() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("已离开互动房间");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    getActivity().finish();//结束App
                });
                builder.show();
            }
        });
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
                    case VHInteractive.force_leave_inav:
                        Toast.makeText(mContext, "您已强制被踢出房间！", Toast.LENGTH_SHORT).show();
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
            int onlineNum = obj.optInt("uv");
            mOnlineTV.setText("online:" + onlineNum);
        }
    }

    private void addStream(final Stream stream) {
        if (stream == null)
            return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mStreams.contains(stream))
                    return;
                mStreams.add(stream);
                mAdapter.notifyItemInserted(mStreams.size() - 1);
//                mAdapter.notifyDataSetChanged();
            }
        });
    }

    public boolean changeStream(Stream stream) {
        if (stream == null)
            return true;
        boolean added = false;
        for (int i = 0; i < mStreamContainer.getChildCount(); i++) {
            View v = mStreamContainer.getChildAt(i);
            Stream item = (Stream) v.getTag();
            if (item != null && item.streamId == stream.streamId) {
                VHRenderView renderView = v.findViewById(R.id.renderview);
                stream.removeAllRenderView();
                stream.addRenderView(renderView);
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
                for (int i = 0; i < mStreams.size(); i++) {
                    if (mStreams.get(i).streamId == stream.streamId) {
                        mStreams.remove(stream);
                        mAdapter.notifyItemRemoved(i);
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
                mStreams.clear();
                mAdapter.notifyDataSetChanged();
            }
        });

    }


    private void initView() {
        mStreamContainer = getView().findViewById(R.id.ll_streams);
        localView = getView().findViewById(R.id.localView);
        mJoinBtn = getView().findViewById(R.id.btn_join);
        mQuitBtn = getView().findViewById(R.id.btn_quit);
        mReqBtn = getView().findViewById(R.id.btn_request);
        mBroadcastTB = getView().findViewById(R.id.tb_broadcast);
        mVideoTB = getView().findViewById(R.id.tb_video);
        mAudioTB = getView().findViewById(R.id.tb_audio);
        mDualTB = getView().findViewById(R.id.tb_dual);
        mSwitchCameraBtn = getView().findViewById(R.id.iv_camera);
        mMemberBtn = getView().findViewById(R.id.btn_members);
        mInfoBtn = getView().findViewById(R.id.iv_info);
        mOnlineTV = getView().findViewById(R.id.tv_online);
        btnScreen = getView().findViewById(R.id.btn_screen_record);
        Switch beautifySwitch = getView().findViewById(R.id.switch_beautify);
        beautifySwitch.setVisibility(useBeautify ? View.VISIBLE : View.GONE);
        beautifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchBeautifyState(isChecked);
        });
//无延迟直播
        if(isNodelayLiveAudience){
            localView.setVisibility(View.GONE);
            mReqBtn.setVisibility(View.GONE);
            mQuitBtn.setVisibility(View.GONE);
            btnScreen.setVisibility(View.GONE);
            mBroadcastTB.setVisibility(View.GONE);
            mVideoTB.setVisibility(View.GONE);
            mAudioTB.setVisibility(View.GONE);
            mDualTB.setVisibility(View.GONE);
            mSwitchCameraBtn.setVisibility(View.GONE);
            mInfoBtn.setVisibility(View.GONE);
        }

        btnScreen.setOnClickListener(this);
//        tvScaleType = getView().findViewById(R.id.tv_scale_type);
        mJoinBtn.setOnClickListener(this);
        mQuitBtn.setOnClickListener(this);
        mReqBtn.setOnClickListener(this);
        mMemberBtn.setOnClickListener(this);
        mSwitchCameraBtn.setOnClickListener(this);
        mInfoBtn.setOnClickListener(this);
        mDualTB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tempLocal.changeVoiceType(isChecked ? 1 : 0);
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
                interactive.broadcastRoom(type, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "推旁路失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, "推旁路成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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


        /*tvScaleType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = ++scaleType % 3;
                tvScaleType.setText(scaleText[type]);
                switch (type) {
                    case 0:
                        localView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFit);
                        break;
                    case 1:
                        localView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeAspectFill);
                        break;
                    case 2:
                        localView.setScalingMode(SurfaceViewRenderer.VHRenderViewScalingMode.kVHRenderViewScalingModeNone);
                        break;
                }
            }
        });*/

        Log.d(TAG, ">>>" + VHBeautifyKit.getInstance().isBeautifyEnable());
        initBeautifyData();
        VHBeautifyKit.getInstance().setFaceDetectionListener(faceNum -> Log.d(TAG, ">>>faceNum = " + faceNum));
    }

    private void initBeautifyData() {
        mFaceBeautyControlView = getView().findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setVisibility(useBeautify ? View.VISIBLE : View.GONE);
        if (useBeautify) {
            mFaceBeautyDataFactory = new FaceBeautyDataFactory(mFaceBeautyListener);
            mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
            mFaceBeautyControlView.setOnBottomAnimatorChangeListener(showRate -> {
            });
        }
    }

    private void switchBeautifyState(boolean enable) {
        if (null != localStream) {
            VHBeautifyKit.getInstance().setBeautifyEnable(enable);
            localStream.setEnableBeautify(false);//关闭默认美颜
        }
    }

    FaceBeautyDataFactory.FaceBeautyListener mFaceBeautyListener = new FaceBeautyDataFactory.FaceBeautyListener() {

        @Override
        public void onFilterSelected(int res) {
            Toast.makeText(requireContext(), getString(res), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFaceBeautyEnable(boolean enable) {
            switchBeautifyState(enable);
        }
    };

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_join:
                join();
                break;
            case R.id.btn_quit:
                interactive.unpublish();
                break;
            case R.id.btn_request:
                joinRequest();
                break;
            case R.id.iv_camera:
                localStream.switchCamera();
                break;
            case R.id.btn_members:
                showMember(true, null);
                break;
            case R.id.iv_info:
                if (infoListener != null) {
                    infoListener.onInfoClick(tempLocal);
                }
                break;
            case R.id.btn_screen_record:

                break;
        }
    }

    public void joinRequest() {//申请上麦
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
                                getActivity().finish();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onItemClick(int position) {
//            Stream stream = mStreams.get(position);
//            mStreams.remove(position);
//            if (!tempLocal.isLocal) {
//                interactive.switchDualStream(tempLocal, 0, null);
//            }
//            mStreams.add(position, tempLocal);
//            mAdapter.notifyItemChanged(position);
//            changePosition = position;
//            tempLocal = stream;
//            localView.setStream(tempLocal);
//            if (!tempLocal.isLocal) {
//                interactive.switchDualStream(tempLocal, 1, null);
//            }
        }
    };

    private InfoClickListener infoListener = new InfoClickListener() {
        @Override
        public void onInfoClick(Stream stream) {
            if (streamPop == null) {
                streamPop = new StreamInfoPopu(getContext());
            }
            streamPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    stream.startStats(null);

                }
            });
            stream.startStats(new Stream.StatsCallback() {
                @Override
                public void onResponse(String s, long l, Map<String, String> map) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            streamPop.refreshData(stream, s, l, map);
                        }
                    });
                }
            });
            streamPop.showAtLocation(getView(), Gravity.CENTER, 0, 0);
        }
    };

    interface ItemClickListener {
        void onItemClick(int position);
    }

    interface InfoClickListener {
        void onInfoClick(Stream stream);
    }

    class StreamAdapter extends RecyclerView.Adapter<MyHolder> {

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MyHolder holder = new MyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_remote_stream, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            Log.e(TAG, "onBindViewHolder:" + position);
            Stream stream = mStreams.get(position);
            if (stream != null) {
                stream.removeAllRenderView();
                stream.addRenderView(holder.renderView);
            }
            holder.renderView.setTag(stream);
            holder.tvDescribe.setText(stream.userId);
            holder.cbVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)//关闭视频开
                        stream.muteVideo(null);
                    else//关闭视频关
                        stream.unmuteVideo(null);
                }
            });
            holder.cbAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        stream.muteAudio(null);
                    else//关闭视频关
                        stream.unmuteAudio(null);
                }
            });

            holder.ivInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (infoListener != null) {
                        infoListener.onInfoClick(stream);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mStreams.size();
        }

        @Override
        public void onViewAttachedToWindow(@NonNull MyHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.e(TAG, "onViewAttachedToWindow");
            if (holder.getAdapterPosition() != changePosition) {
                holder.renderView.getStream().unmuteVideo(null);
            }
        }


        @Override
        public void onViewDetachedFromWindow(@NonNull MyHolder holder) {
            super.onViewDetachedFromWindow(holder);
            Log.e(TAG, "onViewDetachedFromWindow");
            changePosition = -1;
            if (holder.renderView.getStream() != tempLocal) {
                if (!holder.renderView.getStream().isLocal) {
                    holder.renderView.getStream().muteVideo(null);
                }
            }
        }
    }


    class MyHolder extends RecyclerView.ViewHolder {

        VHRenderView renderView;
        CheckBox cbVideo;
        CheckBox cbAudio;
        TextView tvDescribe;
        ImageView ivInfo;


        public MyHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(getAdapterPosition());
                    }
                }
            });
            renderView = itemView.findViewById(R.id.renderview);
            renderView.init(null, null);
            tvDescribe = itemView.findViewById(R.id.tv_speed);
            ivInfo = itemView.findViewById(R.id.iv_info);
            cbVideo = itemView.findViewById(R.id.cb_video);
            cbAudio = itemView.findViewById(R.id.cb_audio);
        }
    }


}
