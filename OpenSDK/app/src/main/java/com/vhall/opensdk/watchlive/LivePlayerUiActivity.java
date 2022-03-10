package com.vhall.opensdk.watchlive;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.vhall.lss.play.VHLivePlayer;
import com.vhall.opensdk.R;
import com.vhall.ui.VHLiveUiView;
import com.vhall.ui.VHUiPlayerLister;
import com.vhall.vod.VHVodPlayer;

/**
 * @author hkl
 */
public class LivePlayerUiActivity extends Activity {

    private VHLiveUiView liveUiView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        String roomId = getIntent().getStringExtra("roomId");
        String accessToken = getIntent().getStringExtra("token");
        setContentView(R.layout.activity_live_player_ui);
        liveUiView = findViewById(R.id.play_view);
        liveUiView.init(roomId, accessToken);
        liveUiView.setUiPlayerLister(new VHUiPlayerLister() {
            @Override
            public void onError(int errorCode, int i1, String msg) {
                Toast.makeText(LivePlayerUiActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        liveUiView.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VHLivePlayer playerView = liveUiView.getPlayerView();
        if (playerView != null && playerView.isPlaying()) {
            playerView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VHLivePlayer playerView = liveUiView.getPlayerView();
        if (playerView != null && playerView.resumeAble()) {
            playerView.resume();
        }
    }

}
