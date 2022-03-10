package com.vhall.opensdk.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.vhall.framework.VhallSDK;
import com.vhall.opensdk.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by zwp on 2020-02-29
 */
public class ChangeUserInfoDialog extends Dialog {

    private EditText edtUserId;
    private EditText edtNickName;
    private Button btnSave;
    private SharedPreferences sp;

    public ChangeUserInfoDialog(Context context) {
        super(context);
    }

    public ChangeUserInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public ChangeUserInfoDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edt_user_info);
        edtUserId = findViewById(R.id.edt_input_user_id);
        edtNickName = findViewById(R.id.edt_input_nick_name);
        btnSave = findViewById(R.id.btn_save_user_info);
        sp = getContext().getSharedPreferences("config", Context.MODE_PRIVATE);
        edtUserId.setHint("null");
        edtNickName.setHint("null");
        edtUserId.setText(sp.getString("userId", ""));
        edtNickName.setText(sp.getString("nickName", ""));

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = edtUserId.getText().toString().trim();
                String nickName = edtNickName.getText().toString().trim();
                if (!TextUtils.isEmpty(userId)) {
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("third_party_user_id", userId);
                        if (TextUtils.isEmpty(nickName)) {
                            nickName = userId;
                        }
                        obj.put("nick_name", nickName);
                        obj.put("avatar", "");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sp.edit().putString("userId", userId)
                            .putString("nickName", nickName)
                            .commit();
                    VhallSDK.getInstance().setUserInfo(obj.toString());
                }
                dismiss();
            }
        });
    }
}
