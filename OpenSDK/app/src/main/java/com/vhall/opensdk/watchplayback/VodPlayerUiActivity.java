package com.vhall.opensdk.watchplayback;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.opensdk.R;
import com.vhall.ui.VHUiPlayerLister;
import com.vhall.ui.VHVodPlayUiView;
import com.vhall.vod.VHVodPlayer;

/**
 * @author hkl
 */
public class VodPlayerUiActivity extends Activity {

    private static final String TAG = "VodActivity";
    private String recordId = "";
    private VHVodPlayUiView playUiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        recordId = getIntent().getStringExtra("channelId");
        if(TextUtils.isEmpty(recordId)){
            recordId = getIntent().getStringExtra("roomId");
        }
        String accessToken = getIntent().getStringExtra("token");
        setContentView(R.layout.activity_vod_player_ui);
        playUiView = findViewById(R.id.play_view);
        playUiView.init(recordId, accessToken);
        playUiView.setUiPlayerLister(new VHUiPlayerLister() {
            @Override
            public void onError(int errorCode, int i1, String msg) {
                Log.e("VodPlayerUiActivity", msg);
                Toast.makeText(VodPlayerUiActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        VHVodPlayer playerView = playUiView.getPlayerView();
        if (playerView != null && playerView.isPlaying()) {
            playerView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VHVodPlayer playerView = playUiView.getPlayerView();
        if (playerView != null) {
            playerView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playUiView != null) {
            playUiView.release();
        }
    }
}
