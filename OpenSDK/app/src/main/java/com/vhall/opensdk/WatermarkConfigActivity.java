package com.vhall.opensdk;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import android.support.annotation.Nullable;

public class WatermarkConfigActivity extends Activity {

    public static final String SP_NAME = "config_watermark";
    public static final String KEY_TEXT = "wmark_text";
    public static final String KEY_COLOR = "wmark_color";
    public static final String KEY_ANGLE = "wmark_angle";
    public static final String KEY_OPACITY = "wmark_opacity";
    public static final String KEY_FONTSIZE = "wmark_fontsize";

    private SharedPreferences mSp;
    private EditText wmark_text, wmark_color, wmark_angle, wmark_opacity, wmark_fontsize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_watermark);

        wmark_text = findViewById(R.id.wmark_text);
        wmark_color = findViewById(R.id.wmark_color);
        wmark_angle = findViewById(R.id.wmark_angle);
        wmark_opacity = findViewById(R.id.wmark_opacity);
        wmark_fontsize = findViewById(R.id.wmark_fontsize);

        mSp = this.getSharedPreferences(SP_NAME, MODE_PRIVATE);

        loadLocalSettings();
    }

    private void loadLocalSettings() {
        wmark_text.setText(mSp.getString(WatermarkConfigActivity.KEY_TEXT, ""));
        wmark_color.setText(mSp.getString(WatermarkConfigActivity.KEY_COLOR, ""));
        wmark_angle.setText(mSp.getString(WatermarkConfigActivity.KEY_ANGLE, ""));
        wmark_opacity.setText(mSp.getString(WatermarkConfigActivity.KEY_OPACITY, ""));
        wmark_fontsize.setText(mSp.getString(WatermarkConfigActivity.KEY_FONTSIZE, ""));
    }

    public void wmark_save(View view) {
        SharedPreferences.Editor editor = mSp.edit();

        editor.putString(KEY_TEXT, wmark_text.getText().toString());
        editor.putString(KEY_COLOR, wmark_color.getText().toString().trim());
        editor.putString(KEY_ANGLE, wmark_angle.getText().toString().trim());
        editor.putString(KEY_OPACITY, wmark_opacity.getText().toString().trim());
        editor.putString(KEY_FONTSIZE, wmark_fontsize.getText().toString().trim());

        editor.commit();
        finish();
    }

    public void wmark_clear(View view) {
        wmark_text.setText("");
        wmark_color.setText("");
        wmark_angle.setText("");
        wmark_opacity.setText("");
        wmark_fontsize.setText("");
    }
}
