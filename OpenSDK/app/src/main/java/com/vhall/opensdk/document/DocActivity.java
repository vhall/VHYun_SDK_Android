package com.vhall.opensdk.document;

import android.app.Activity;
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

import com.vhall.opensdk.R;
import com.vhall.ops.IVHOPS;
import com.vhall.ops.VHOPS;
import com.vhall.ops.VHOPSView;

import org.json.JSONObject;

/**
 * Created by Hank on 2017/12/18.
 */
public class DocActivity extends Activity {
    private static final String TAG = "DocActivity";
    private String mChannelId = "";
    private String mAccessToken = "";
    VHOPS mDocument;
    VHOPSView mDocView;
    //demo view
    Switch mEditableView;
    ScrollView mEditViewContainer;//文档操作容器
    RadioGroup mDoodleActions;
    LinearLayout mDoodleTypeContainer;
    RadioGroup mDoodleTypes;
    EditText et_color, et_size, et_docid;
    TextView mPageView, mStepView;

    private IVHOPS.DrawAction mAction = IVHOPS.DrawAction.ADD;
    private IVHOPS.DrawType mType = IVHOPS.DrawType.PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChannelId = getIntent().getStringExtra("channelid");
        mAccessToken = getIntent().getStringExtra("token");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.doc_layout);
        initView();
        mDocument = new VHOPS(mChannelId, "", mAccessToken);
        mDocument.setDocumentView(mDocView);
        mDocument.setEventListener(new VHOPS.OnEventListener() {
            @Override
            public void onEvent(JSONObject object) {
                Log.i(TAG, "object:" + object.toString());
                mPageView.setText("页数：" + (object.optInt("currentPage") + 1) + "/" + object.optInt("page"));
                mStepView.setText("步数：" + (object.optInt("currentStep") + 1) + "/" + object.optInt("step"));
            }
        });
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
        mPageView = findViewById(R.id.tv_page);
        mStepView = findViewById(R.id.tv_step);
        mDoodleActions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_action_add:
                        mAction = IVHOPS.DrawAction.ADD;
                        mDoodleTypeContainer.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rb_action_modify:
                        mAction = IVHOPS.DrawAction.MODIFY;
                        mDoodleTypeContainer.setVisibility(View.GONE);
                        break;
                    case R.id.rb_action_delete:
                        mAction = IVHOPS.DrawAction.DELETE;
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
                        mType = IVHOPS.DrawType.PATH;
                        break;
                    case R.id.rb_nite:
                        mType = IVHOPS.DrawType.NITE;
                        break;
                    case R.id.rb_rect:
                        mType = IVHOPS.DrawType.RECT;
                        break;
                    case R.id.rb_circle:
                        mType = IVHOPS.DrawType.CIRCLE;
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
        mDocView.setOnEventListener(new VHOPSView.EventListener() {
            @Override
            public void onEvent(int eventCode, String eventMsg) {
                switch (eventCode) {
                    case VHOPSView.EVENT_DOC_LOADED:
                        mDocView.setAction(mAction);
                        mDocView.setDrawType(mType);
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
        mDocView.setDoc(docid);
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
