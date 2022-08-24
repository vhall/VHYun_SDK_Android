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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhall.beautify.VHBeautifyKit;
import com.vhall.beautifykit.control.FaceBeautyControlView;
import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.ConfigActivity;
import com.vhall.opensdk.R;
import com.vhall.opensdk.beautysource.FaceBeautyDataFactory;
import com.vhall.opensdk.util.ListUtil;
import com.vhall.opensdk.util.SpUtils;
import com.vhall.rtc.VRTCCode;
import com.vhall.vhallrtc.client.FinishCallback;
import com.vhall.vhallrtc.client.Room;
import com.vhall.vhallrtc.client.SignalingChannel;
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
    private Context mContext;
    private InteractiveActivity mActivity;
    RecyclerView mStreamContainer;//远程流渲染view容器
    LinearLayoutManager mLayoutManager;
    StreamAdapter mAdapter;//容器Adapter

    VHRenderView localView;//本地流渲染view
    Stream localStream;//本地流
    Button mReqBtn, mJoinBtn, mQuitBtn, mMemberBtn;//操作隐藏，demo默认进入直接上麦
    AlertDialog mDialog;
    //功能按钮
    ImageView mSwitchCameraBtn, mInfoBtn;
    CheckBox mBroadcastTB, mVideoTB, mAudioTB, mDualTB;
    TextView mOnlineTV, tvScaleType;

    private AppCompatSpinner mDefSpinner, mMCUBgMode, mMixLayoutMode, mMixLayoutModeAdaptive;
    private final int DEFAULTRESWIDTH = 640;
    private final int MDEFAULTRESHEIGHT = 480;

    public String mRoomId;
    public String mAccessToken;
    public Boolean isNodelayLiveAudience=false;//无延迟直播观众角色
    public String mNoDelayAction = null;
    VHInteractive interactive = null;
    boolean isEnable = false;//是否可用
    boolean isOnline = false;//是否上麦
    String mRoomAttr = "roomAttr";
    String mBroadcastid = "";
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
    private Switch mSwitchMCUBg = null;
    private Switch mSwitchMCUPlaceholder = null;
    private View mMCUSwitchGroup = null;
    private int mMCUBgScaleType = VRTCCode.MCU_BG_MODE_FILL;
    private String mMCUBgUrl = "";
    private String mMCUPlaceholderUrl = "";

    private MediaProjectionManager mediaProjectionManager = null;

    private String mDocStreamId;

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

    public static InteractiveFragment getInstance(String roomid, String accessToken,Boolean isNodelayLiveAudience, String action) {
        InteractiveFragment fragment = new InteractiveFragment();
        fragment.mRoomId = roomid;
        fragment.mAccessToken = accessToken;
        fragment.isNodelayLiveAudience=isNodelayLiveAudience;
        fragment.mNoDelayAction = action;
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        mContext = context;
        super.onAttach(context);
        if (context instanceof InteractiveActivity) {
            mActivity = (InteractiveActivity) context;
            useBeautify = ((InteractiveActivity) context).getIntent().getBooleanExtra("beautify", false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
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
        mMCUBgUrl = sp.getString(ConfigActivity.KEY_RTC_MCU_BG, "");
        mMCUPlaceholderUrl = sp.getString(ConfigActivity.KEY_RTC_MCU_PLACEHOLDER, "");
        interactive = new VHInteractive(mContext, new RoomListener());
        interactive.setOnMessageListener(new MyMessageListener());
//        VHTool.enableDebugLog(true);
        if (!isNodelayAudiance()) {
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

    private boolean isNodelayAudiance() {
        return InteractiveActivity.NODELAY_ACTION_WATCH.equals(mNoDelayAction);
    }

    private void initInteractive() {
        interactive.init(
                mRoomId,
                mAccessToken,
                mBroadcastid,
                isNodelayLiveAudience ? VHInteractive.MODE_LIVE : VHInteractive.MODE_RTC,
                isNodelayAudiance() ? VHInteractive.ROLE_AUDIENCE : VHInteractive.ROLE_HOST,
                new VHInteractive.InitCallback() {
                    @Override
                    public void onSuccess() {
                        isEnable = true;
                        interactive.enterRoom(mRoomAttr);//初始化成功，直接进入房间
                        refreshMembers();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMsg) {
                        isEnable = false;
                        showToast(errorMsg);
                    }
                });
    }

    class RoomListener implements Room.RoomDelegate {

        @Override
        public void onDidConnect(Room room, JSONObject jsonObject) {//进入房间
            Log.i(TAG, "onDidConnect");
            interactiveRoom = room;
            subscribeStreams(room.getRemoteStreams());
            if (!(isNodelayLiveAudience && mNoDelayAction.equals(InteractiveActivity.NODELAY_ACTION_WATCH))) {
                join();//进入房间成功，自动上麦
            }
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
        public void onDidInternalStreamAdded(Room room, JSONObject jsonObject) {
            Log.i(TAG, "onDidInternalStreamAdded " + jsonObject.toString());
            showToast("onDidInternalStreamAdded " + jsonObject.toString());
            mDocStreamId = jsonObject.optString("id");
        }

        @Override
        public void onDidInternalStreamRemoved(Room room, JSONObject jsonObject) {
            Log.i(TAG, "onDidInternalStreamRemoved : " + jsonObject.toString());
            showToast("onDidInternalStreamRemoved : " + jsonObject.toString());
        }

        @Override
        public void onDidInternalStreamFailed(Room room, JSONObject jsonObject) {
            Log.i(TAG, "onDidInternalStreamRemoved : " + jsonObject.toString());
            showToast("onDidInternalStreamRemoved : " + jsonObject.toString());
        }

        @Override
        public void onDidUnPublishStream(Room room, Stream stream) {//下麦
            Log.i(TAG, "onDidUnPublishStream");
            isOnline = false;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("下麦成功");
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
                    showToast("无权限或观看的无延时直播");
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
                            showToast("您的上麦请求未通过!");
                        }
                        break;
                    case VHInteractive.askfor_inav_publish://邀请上麦消息
                        showDialog(VHInteractive.askfor_inav_publish, userid);
                        break;
                    case VHInteractive.kick_inav_stream:
                        showToast("您已被请下麦！");
                        break;
                    case VHInteractive.kick_inav:
                        showToast("您已被踢出房间！");
                        getActivity().finish();
                        break;
                    case VHInteractive.force_leave_inav:
                        showToast("您已强制被踢出房间！");
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
                        showToast(userid + ":" + action);
                        break;
                    case VHInteractive.inav_close:
                        showToast("直播间已关闭");
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
        getView().findViewById(R.id.btn_forceleave).setOnClickListener(this);

        mMixLayoutMode = getView().findViewById(R.id.layoutmode);
        mMixLayoutMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (0 < position) {
                    if (position >= 26) position++;//TODO code 26 is missing
                    changeMixLayoutMode(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mMixLayoutModeAdaptive = getView().findViewById(R.id.layoutmodeAdaptive);
        mMixLayoutModeAdaptive.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (0 < position) {
                    changeMixAdaptiveLayoutMode(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mMCUBgMode = getView().findViewById(R.id.mcu_bg_mode);
        mMCUBgMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (0 < position) {
                    changeMCUBgMode(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mDefSpinner = getView().findViewById(R.id.inav_def);
        mDefSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeDefinition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getView().findViewById(R.id.lable_beautify).setVisibility(useBeautify ? View.VISIBLE : View.GONE);
        Switch beautifySwitch = getView().findViewById(R.id.switch_beautify);
        beautifySwitch.setVisibility(useBeautify ? View.VISIBLE : View.GONE);
        beautifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> switchBeautifyState(isChecked));

        getView().findViewById(R.id.inav_doc).setOnClickListener(v -> mActivity.showDocFragment());

        Switch docmixSwitch = getView().findViewById(R.id.switch_docmix);
        docmixSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> docCloudMix(isChecked));

        getView().findViewById(R.id.btn_doc_main).setOnClickListener(v -> docCloudMix2Main());

        mMCUSwitchGroup = getView().findViewById(R.id.mcugroup);
        mSwitchMCUBg = getView().findViewById(R.id.switch_mcu_bg);
        mSwitchMCUBg.setOnCheckedChangeListener((buttonView, isChecked) -> switchMCUBg(isChecked, mMCUBgScaleType));
        mSwitchMCUPlaceholder = getView().findViewById(R.id.switch_mcu_placeholder);
        mSwitchMCUPlaceholder.setOnCheckedChangeListener((buttonView, isChecked) -> switchMCUPlaceholder(isChecked));

        //无延迟直播
        if (isNodelayAudiance()) {
            getView().findViewById(R.id.ll_menu).setVisibility(View.GONE);
            localView.setVisibility(View.GONE);
            mReqBtn.setVisibility(View.GONE);
            mQuitBtn.setVisibility(View.GONE);
            mJoinBtn.setVisibility(View.GONE);
            getView().findViewById(R.id.btn_forceleave).setVisibility(View.GONE);
        }

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
//                if (null != mMCUSwitchGroup) {
//                    mMCUSwitchGroup.setVisibility(View.GONE);
//                }
                if (TextUtils.isEmpty(mBroadcastid)) {
                    mBroadcastTB.setChecked(false);
                    showToast("旁路ID为空，无法推旁路");
                    return;
                }
                final int type = isChecked ? 1 : 2;
                interactive.broadcastRoom(type, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showToast(isChecked ? "推旁路失败" : "停止旁路失败");
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showToast( isChecked ? "推旁路成功" : "推旁路结束");
//                                if (isChecked && null != mMCUSwitchGroup) {
//                                    mMCUSwitchGroup.setVisibility(View.VISIBLE);
//                                }
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

    private void changeDefinition(int position) {
        int dWidth = DEFAULTRESWIDTH;
        int dHeight = MDEFAULTRESHEIGHT;
        if (position == Stream.VhallFrameResolutionValue.VhallFrameResolution640x480.getValue()) {
            dWidth = DEFAULTRESWIDTH;
            dHeight = MDEFAULTRESHEIGHT;
        } else if (position == Stream.VhallFrameResolutionValue.VhallFrameResolution480x360.getValue()) {
            dWidth = 480;
            dHeight = 360;
        } else if (position == Stream.VhallFrameResolutionValue.VhallFrameResolution320x240.getValue()) {
            dWidth = 320;
            dHeight = 240;
        } else if (position == Stream.VhallFrameResolutionValue.VhallFrameResolution240x160.getValue()) {
            dWidth = 240;
            dHeight = 160;
        } else if (position == Stream.VhallFrameResolutionValue.VhallFrameResolution192x144.getValue()) {
            dWidth = 192;
            dHeight = 144;
        }
        if (null != localStream) {
            localStream.ChangeCarameFormat(dWidth, dHeight, 15);
        }
    }

    private void initBeautifyData() {
        mFaceBeautyControlView = getView().findViewById(R.id.faceBeautyControlView);
        mFaceBeautyControlView.setVisibility(useBeautify ? View.VISIBLE : View.GONE);
        if (useBeautify) {
            mFaceBeautyDataFactory = new FaceBeautyDataFactory(mFaceBeautyListener);
            mFaceBeautyControlView.bindDataFactory(mFaceBeautyDataFactory);
            mFaceBeautyControlView.setOnBottomAnimatorChangeListener(showRate -> {
            });
        } else {
            VHBeautifyKit.getInstance().setBeautifyEnable(false);
        }
    }

    private void switchDoc(boolean enable) {
        if (null != localStream) {

        }
    }

    private void switchDocMix(boolean enable) {
        if (null != localStream) {
            VHBeautifyKit.getInstance().setBeautifyEnable(enable);
            localStream.setEnableBeautify(false);//关闭默认美颜
        }
    }

    private void docCloudMix(boolean isChecked) {
        if (isChecked) {
            interactive.startDocCloudRender(SpUtils.share().getAppId(), SpUtils.share().getChatId(), new FinishCallback() {
                @Override
                public void onFinish(int i, @Nullable String s) {
                    Log.e(TAG, "---> startDocCloudRender " + i + " - " + s);
                }
            });
        } else {
            interactive.stopDocCloudRender(SpUtils.share().getAppId(), SpUtils.share().getChatId(), new FinishCallback() {
                @Override
                public void onFinish(int i, @Nullable String s) {
                    Log.e(TAG, "---> stopDocCloudRender " + i + " - " + s);
                }
            });
        }
    }

    private void docCloudMix2Main() {
        if (!TextUtils.isEmpty(mDocStreamId)) {
            interactive.setMixLayoutMainScreen(mDocStreamId, new FinishCallback() {
                @Override
                public void onFinish(int i, @Nullable String s) {
                    Log.e(TAG, "---> " + i + " - " + s);
                }
            });
        }
    }

    private void switchBeautifyState(boolean enable) {
        if (null != localStream) {
            VHBeautifyKit.getInstance().setBeautifyEnable(enable);
            localStream.setEnableBeautify(false);//关闭默认美颜
        }
    }

    private void changeMixLayoutMode(int pos) {
        if (null != interactive) {
            interactive.setMixLayoutMode(pos, null, (code, msg) -> {
                showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
            });
        }
    }

    private void changeMixAdaptiveLayoutMode(int pos) {
        if (null != interactive) {
            interactive.setMixAdaptiveLayoutMode(pos, (code, msg) -> {
                showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
            });
        }
    }

    private void changeMCUBgMode(int pos) {
        mMCUBgScaleType = pos;
        if (mSwitchMCUBg.isChecked()) {
            switchMCUBg(true, pos);
        } else {
            mSwitchMCUBg.setChecked(true);
        }
    }

    private void switchMCUBg(boolean enable, int scaleType) {
        if (null != interactive) {
            if (enable) {
                interactive.setRoomBroadCastBackgroundImage(
                        mMCUBgUrl,
                        scaleType,
                        (code, msg) -> {
                            showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
                        });
            } else {
                interactive.resetRoomBroadCastBackgroundImage((code, msg) -> {
                    showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
                });
            }
        }
    }

    private void switchMCUPlaceholder(boolean enable) {
        if (null != interactive) {
            if (enable) {
                interactive.setRoomBroadCastPlaceholderImage(
                        mMCUPlaceholderUrl,
                        (code, msg) -> {
                            showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
                        });
            } else {
                interactive.resetRoomBroadCastPlaceholderImage((code, msg) -> {
                    showToast(VRTCCode.MCU_BG_RESULT == code ? "成功" : code + " : " + msg);
                });
            }
        }
    }

    FaceBeautyDataFactory.FaceBeautyListener mFaceBeautyListener = new FaceBeautyDataFactory.FaceBeautyListener() {

        @Override
        public void onFilterSelected(int res) {
            showToast(getString(res));
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
            case R.id.btn_forceleave:
                interactive.forceLeaveRoom(SpUtils.share().getUserId(), null);
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
                                showToast(obj.optString("msg"));
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

        @Override
        public void onMainScreenClick(Stream stream) {
            stream.setMixLayoutMainScreen(null, null);
        }
    };

    interface ItemClickListener {
        void onItemClick(int position);
    }

    interface InfoClickListener {
        void onInfoClick(Stream stream);
        void onMainScreenClick(Stream stream);
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

            holder.ivInfo.setOnClickListener(v -> {
                if (infoListener != null) {
                    infoListener.onInfoClick(stream);
                }
            });

            holder.ivMainScreen.setOnClickListener(v -> {
                if (infoListener != null) {
                    infoListener.onMainScreenClick(stream);
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
        ImageView ivInfo, ivMainScreen;


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
            ivMainScreen = itemView.findViewById(R.id.iv_mainscreen);
            cbVideo = itemView.findViewById(R.id.cb_video);
            cbAudio = itemView.findViewById(R.id.cb_audio);
        }
    }

    private void showToast(String msg) {
        try {
            requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity(), "-> " + msg, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}