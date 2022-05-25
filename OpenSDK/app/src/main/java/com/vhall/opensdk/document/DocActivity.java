package com.vhall.opensdk.document;

import static android.view.View.VISIBLE;
import static com.vhall.document.DocumentView.DOC_BOARD;
import static com.vhall.document.DocumentView.DOC_DOCUMENT;
import static com.vhall.ops.VHOPS.ERROR_CONNECT;
import static com.vhall.ops.VHOPS.ERROR_DOC_INFO;
import static com.vhall.ops.VHOPS.ERROR_SEND;
import static com.vhall.ops.VHOPS.KEY_OPERATE;
import static com.vhall.ops.VHOPS.TYPE_ACTIVE;
import static com.vhall.ops.VHOPS.TYPE_CREATE;
import static com.vhall.ops.VHOPS.TYPE_DESTROY;
import static com.vhall.ops.VHOPS.TYPE_SWITCHOFF;
import static com.vhall.ops.VHOPS.TYPE_SWITCHON;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.vhall.document.DocumentView;
import com.vhall.document.IDocument;
import com.vhall.opensdk.R;
import com.vhall.opensdk.WatermarkConfigActivity;
import com.vhall.opensdk.util.TabAdapter;
import com.vhall.ops.VHOPS;
import com.vhall.ops.WatermarkOption;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Hank on 2017/12/18.
 *
 * 纯文档直播
 */
public class DocActivity extends Activity {
    private static final String TAG = "DocActivity";
    private String mChannelId = "";
    protected String mRoomId = "";
    protected String mAccessToken = "";
    protected VHOPS mDocument;
    private DocumentView mDocView;
    private RelativeLayout rl, rlContainer;
    //demo view
    private Switch switchDemo;
    private ScrollView mEditViewContainer;//文档操作容器
    private RadioGroup mDoodleActions;
    private LinearLayout mDoodleTypeContainer;
    private RadioGroup mDoodleTypes, viewAdds;
    private EditText et_color, et_size, et_param;
    private TextView mPageView, mStepView;
    private SharedPreferences sp;
    private RecyclerView recyclerView;
    private TabAdapter tabAdapter;
    private LinearLayoutManager layoutManager;
    private List<String> idList = new ArrayList<>();
    private HashMap<String, DocumentView> viewMap = new HashMap<>();
    private String createType = DOC_BOARD;
    private IDocument.DrawAction mAction = IDocument.DrawAction.ADD;
    private IDocument.DrawType mType = IDocument.DrawType.PATH;
    private WatermarkOption mWatermarkOption;
    private SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = getIntent().getStringExtra("roomId");//纯文档直播可不传
        mChannelId = getIntent().getStringExtra("channelId");
        if (TextUtils.isEmpty(mRoomId)) {
            mRoomId = mChannelId;
        }
        mAccessToken = getIntent().getStringExtra("token");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initWatermarkOption();
        if (mDocument == null) {
            mDocument = new VHOPS(this, mChannelId, mRoomId, mAccessToken, true);
            if (null != mWatermarkOption) {
                mDocument.setWatermark(mWatermarkOption);
            }
            mDocument.setListener(opsCallback);
            mDocument.setEditable(true);
            mDocument.join();
        }
    }

    private void initWatermarkOption() {
        if (null == mSp) {
            mSp = getSharedPreferences(WatermarkConfigActivity.SP_NAME, MODE_PRIVATE);
        }
        String markStr = mSp.getString(WatermarkConfigActivity.KEY_TEXT, "");
        if (TextUtils.isEmpty(markStr)) {
            mWatermarkOption = null;
            return;
        }
        mWatermarkOption = new WatermarkOption(
                markStr,
                mSp.getString(WatermarkConfigActivity.KEY_COLOR, ""),
                Integer.valueOf(mSp.getString(WatermarkConfigActivity.KEY_ANGLE, "")),
                Float.valueOf(mSp.getString(WatermarkConfigActivity.KEY_OPACITY, "")),
                Integer.valueOf(mSp.getString(WatermarkConfigActivity.KEY_FONTSIZE, ""))
        );
    }

    private VHOPS.EventListener opsCallback = new VHOPS.EventListener() {

        @Override
        public void onEvent(String event, String type, String cid) {
            if (event.equals(KEY_OPERATE)) {
                if (type.equals(TYPE_ACTIVE)) {
                    //激活文档对象，切换文档实例
                    replaceView();
                } else if (type.equals(TYPE_CREATE)) {
                    //创建文档
                    if (mDocument.isEditAble()) {
                        idList.add(idList.size() - 1, cid);
                    } else {
                        idList.add(cid);
                    }
                    tabAdapter.setIdList(idList);
                } else if (type.equals(TYPE_DESTROY)) {
                    //删除编号 cid的文档
                    idList.remove(cid);
                    tabAdapter.setIdList(idList);
                } else if (type.equals(TYPE_SWITCHOFF)) {
                    //关闭文档演示（针对整个文档模块）
                    rlContainer.setVisibility(View.GONE);
                    switchDemo.setChecked(false);
                } else if (type.equals(TYPE_SWITCHON)) {
                    //打开文档演示(针对文档模块)
                    rlContainer.setVisibility(View.VISIBLE);
                    switchDemo.setChecked(true);
                }
            }
        }

        @Override
        public void onError(int errorCode, int innerError, String errorMsg) {
            switch (errorCode) {
                case ERROR_CONNECT:
                case ERROR_SEND:
                    Toast.makeText(DocActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    break;
                case ERROR_DOC_INFO:
                    try {
                        JSONObject obj = new JSONObject(errorMsg);
                        String msg = obj.optString("msg");
                        String cid = obj.optString("cid");
                        Toast.makeText(DocActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void replaceView() {

        tabAdapter.setActiveId(mDocument.getActiveCid());
        mDocView = mDocument.getActiveView();
        if (mDocView != null) {
            mDocView.addListener(eventListener);
            if (rl != null) {
                rl.removeAllViews();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                rl.addView(mDocView,params);

            }
        }
    }

    DocumentView.EventListener eventListener = new DocumentView.EventListener() {
        @Override
        public void onEvent(int i, String s) {
            switch (i) {
                case DocumentView.EVENT_PAGE_LOADED://界面加载完毕
                    break;
                case DocumentView.EVENT_DOC_LOADED://文档加载完毕
                    Log.i(TAG, "当前文档ID：" + mDocument.getDocId() + " 当前文档opts:" + s);
                    JSONObject optJson = null;
                    try {
                        optJson = new JSONObject(s);
                        mPageView.setText("页数：" + optJson.optString("show_page") + "/" + optJson.optString("page"));
//                            mStepView.setText("步数：" + (object.optInt("currentStep") + 1) + "/" + object.optInt("step"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case DocumentView.EVENT_DOODLE://绘制数据回调
                    Log.i(TAG, "object:" + s);
                    JSONObject object = null;
                    try {
                        object = new JSONObject(s);
                        if (object.has("info")) {
                            JSONObject info = object.optJSONObject("info");
                            mPageView.setText("页数：" + (info.optInt("slideIndex") + 1) + "/" + info.optInt("slidesTotal"));
                            mStepView.setText("步数：" + (info.optInt("stepIndex") + 1) + "/" + info.optInt("totalSteps"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;

                case DocumentView.EVENT_LOADED_FAILED:
                    Toast.makeText(DocActivity.this, s, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private void initView() {
//        mDocView = findViewById(R.id.opsview);//展示文档核心view
        rl = findViewById(R.id.ops_container);
        rlContainer = findViewById(R.id.rl_ops_container);
        mEditViewContainer = findViewById(R.id.sv_edit);
        mEditViewContainer.setVisibility(View.VISIBLE);
        mDoodleActions = findViewById(R.id.rg_actions);
        mDoodleTypeContainer = findViewById(R.id.ll_type);
        mDoodleTypes = findViewById(R.id.rg_types);
        et_color = findViewById(R.id.et_color);
        et_size = findViewById(R.id.et_size);
        et_param = findViewById(R.id.et_param);
        mPageView = findViewById(R.id.tv_page);
        mStepView = findViewById(R.id.tv_step);
        recyclerView = findViewById(R.id.rv_item_doc_tab);
        tabAdapter = new TabAdapter(this, idList);
        tabAdapter.setEditAble(true);
        switchDemo = findViewById(R.id.switch_demo);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(tabAdapter);

        viewAdds = findViewById(R.id.rg_adds);
        viewAdds.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_add_doc:
                        createType = DOC_DOCUMENT;
                        et_param.setText("");
                        break;
                    case R.id.rb_add_board:
                        createType = DOC_BOARD;
                        et_param.setText("#ffffff");
                        break;
                }
            }
        });

        mDoodleActions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_action_add:
                        mAction = IDocument.DrawAction.ADD;
                        mDoodleTypeContainer.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_action_modify:
                        mAction = IDocument.DrawAction.MODIFY;
                        mDoodleTypeContainer.setVisibility(View.GONE);
                        break;
                    case R.id.rb_action_delete:
                        mAction = IDocument.DrawAction.DELETE;
                        mDoodleTypeContainer.setVisibility(View.GONE);
                        break;
                }
                if (mDocView != null) {
                    mDocView.setAction(mAction);
                }
            }
        });
        mDoodleTypes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_path:
                        mType = IDocument.DrawType.PATH;
                        break;
                    case R.id.rb_nite:
                        mType = IDocument.DrawType.NITE;
                        break;
                    case R.id.rb_rect:
                        mType = IDocument.DrawType.RECT;
                        break;
                    case R.id.rb_circle:
                        mType = IDocument.DrawType.CIRCLE;
                        break;
                    case R.id.rb_isosceles_triangle:
                        mType = IDocument.DrawType.ISOSCELES_TRIANGLE;
                        break;
                    case R.id.rb_right_triangle:
                        mType = IDocument.DrawType.RIGHT_TRIANGLE;
                        break;
                    case R.id.rb_single_arrow:
                        mType = IDocument.DrawType.SINGLE_ARROW;
                        break;
                    case R.id.rb_double_arrow:
                        mType = IDocument.DrawType.DOUBLE_ARROW;
                        break;
                }
                if (mDocView != null) {
                    mDocView.setDrawType(mType);
                }
            }
        });

        switchDemo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rlContainer.setVisibility(VISIBLE);
                    if (mDocument != null) {
                        mDocument.switchOn();
                    }
                } else {
                    rlContainer.setVisibility(View.GONE);
                    if (mDocument != null) {
                        mDocument.switchOff();
                    }
                }
            }
        });

        tabAdapter.setItemClickListener(new TabAdapter.ItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mDocument.isEditAble()) {
                    String cid = idList.get(position);
                    if (TextUtils.isEmpty(cid)) {
                        String param = et_param.getText().toString().trim();
                        switch (viewAdds.getCheckedRadioButtonId()) {
                            case R.id.rb_add_doc:
                                if (TextUtils.isEmpty(param)) {
                                    Toast.makeText(DocActivity.this, "请输入文档ID", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                mDocument.addView(DOC_DOCUMENT, param, rl.getWidth(), rl.getHeight());
                                break;
                            case R.id.rb_add_board:
                                if (!TextUtils.isEmpty(param)) {
                                    if (param.length() != 7 && param.length() != 9) {
                                        Toast.makeText(DocActivity.this, "颜色格式不正确，格式例:#ffffff", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                                mDocument.addView(DOC_BOARD, param, rl.getWidth(), rl.getHeight());
                                break;
                            default:
                                break;
                        }
                    } else {
                        mDocument.activeView(cid);
                    }
                }
            }
        });

        tabAdapter.setClearClickListener(new TabAdapter.ClearClickListener() {
            @Override
            public void onClick(View view, int position) {
                String cid = idList.get(position);
                mDocument.deleteView(cid);
                idList.remove(cid);
                tabAdapter.setActiveId("");
                if (idList.size() > 1) {
                    String activeCid = idList.get(0);
                    mDocument.activeView(activeCid);
                }
            }
        });

    }

    public void clickPrePage(View view) {
        if (mDocView != null) {
            mDocView.preSlide();
        }
    }

    public void clickNextPage(View view) {
        if (mDocView != null) {
            mDocView.nextSlide();
        }
    }

    public void clickPreStep(View view) {
        if (mDocView != null) {
            mDocView.preStep();
        }
    }

    public void clickNextStep(View view) {
        if (mDocView != null) {
            mDocView.nextStep();
        }
    }

    public void clickEventSet(View view) {
        String color = et_color.getText().toString();
        int size = 10;
        try {
            size = Integer.parseInt(et_size.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (mDocView != null) {
            mDocView.setDrawOption(color, size);
        }
    }

    public void clickEventClear(View view) {
        if (null != mDocView) {
            mDocView.clear();
        }
    }

    public void clickEventSetDoc(View view) {
        String docid = et_param.getText().toString();
        if (TextUtils.isEmpty(docid))
            return;
        if (mDocView != null) {
            if (mDocView.getType().equals(DocumentView.DOC_DOCUMENT)) {
                mDocument.setDoc(docid);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDocument.leave();
    }
}