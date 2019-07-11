package com.vhall.opensdk.document;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.vhall.document.DocumentView;
import com.vhall.document.IDocument;
import com.vhall.opensdk.R;
import com.vhall.ops.VHOPS;

import org.json.JSONException;
import org.json.JSONObject;

import static com.vhall.opensdk.ConfigActivity.KEY_DOC_ID;

/**
 * Created by Hank on 2017/12/18.
 */
public class DocActivity extends Activity {
    private static final String TAG = "DocActivity";
    private String mChannelId = "";
    private String mRoomId = "";
    private String mAccessToken = "";
    VHOPS mDocument;
    DocumentView mDocView;
    //demo view
    Switch mEditableView;
    ScrollView mEditViewContainer;//文档操作容器
    RadioGroup mDoodleActions;
    LinearLayout mDoodleTypeContainer;
    RadioGroup mDoodleTypes;
    EditText et_color, et_size, et_docid;
    TextView mPageView, mStepView;
    SharedPreferences sp;

    private IDocument.DrawAction mAction = IDocument.DrawAction.ADD;
    private IDocument.DrawType mType = IDocument.DrawType.PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRoomId = getIntent().getStringExtra("roomid");//纯文档直播可不传
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        sp = getSharedPreferences("config", MODE_PRIVATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        initView();
        mDocument = new VHOPS(mChannelId, mRoomId, mAccessToken,true);//是否展示上次文档，默认true
        mDocument.setDocumentView(mDocView);
        mDocument.join();
    }

    private void initView() {
        mDocView = findViewById(R.id.opsview);//展示文档核心view
        mEditableView = findViewById(R.id.switch_edit);
        mEditViewContainer = findViewById(R.id.sv_edit);
        mDoodleActions = findViewById(R.id.rg_actions);
        mDoodleTypeContainer = findViewById(R.id.ll_type);
        mDoodleTypes = findViewById(R.id.rg_types);
        et_color = findViewById(R.id.et_color);
        et_size = findViewById(R.id.et_size);
        et_docid = findViewById(R.id.et_docid);
        et_docid.setText(sp.getString(KEY_DOC_ID,""));
        mPageView = findViewById(R.id.tv_page);
        mStepView = findViewById(R.id.tv_step);
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
                mDocView.setAction(mAction);
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
                mDocView.setDrawType(mType);
            }
        });
        mEditableView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDocView.setEditable(isChecked);
                mEditViewContainer.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            }
        });
        mDocView.addListener(new DocumentView.EventListener() {
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
                            mPageView.setText("页数：" + (object.optInt("currentPage") + 1) + "/" + object.optInt("page"));
                            mStepView.setText("步数：" + (object.optInt("currentStep") + 1) + "/" + object.optInt("step"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });
        setLayout();
    }


    public void clickPrePage(View view) {
        mDocView.preSlide();
    }

    public void clickNextPage(View view) {
        mDocView.nextSlide();
    }

    public void clickPreStep(View view) {
        mDocView.preStep();
    }

    public void clickNextStep(View view) {
        mDocView.nextStep();
    }

    public void clickEventSet(View view) {
        String color = et_color.getText().toString();
        int size = 10;
        try {
            size = Integer.parseInt(et_size.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mDocView.setDrawOption(color, size);
    }

    public void clickEventClear(View view) {
        mDocView.clear();
    }

    public void clickEventSetDoc(View view) {
        String docid = et_docid.getText().toString();
        if (TextUtils.isEmpty(docid))
            return;
        mDocument.setDoc(docid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDocument.leave();
    }

    private void setLayout() {
        WindowManager manager = getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;  //以要素为单位
        int height = width * 9 / 16;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDocView.getLayoutParams();
        params.width = width;
        params.height = height;
//        mDocView.setLayoutParams(params);
    }
}
