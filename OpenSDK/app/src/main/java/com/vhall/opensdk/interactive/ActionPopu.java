package com.vhall.opensdk.interactive;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.vhall.framework.VhallSDK;
import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.R;


/**
 * Created by huanan on 2017/5/15.
 */
public class ActionPopu extends PopupWindow implements View.OnClickListener {

    Context context;
    VHInteractive mInteractive;
    Member mMember;

    Button mBtnInvite, mBtnQuit, mBtnKick;

    public ActionPopu(Context context) {
        super(context);
        this.context = context;
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.action_popu, null);
        setContentView(root);
        mBtnInvite = root.findViewById(R.id.btn_invite);
        mBtnQuit = root.findViewById(R.id.btn_quit);
        mBtnKick = root.findViewById(R.id.btn_kick);
        mBtnInvite.setOnClickListener(this);
        mBtnQuit.setOnClickListener(this);
        mBtnKick.setOnClickListener(this);
    }

    public void setInteractive(VHInteractive interactive) {
        this.mInteractive = interactive;
    }

    public void setMember(Member member) {
        mMember = member;
        refreshPage();
    }

    private void refreshPage() {
        if (mMember.userid.equals(VhallSDK.getInstance().mUserId))
            return;
        switch (mMember.status) {//用户状态 1 推流中 2 观看中 3 受邀中 4 申请中
            case 1:
                mBtnInvite.setVisibility(View.GONE);
                if (mInteractive.isKickoutStreamAvailable())
                    mBtnQuit.setVisibility(View.VISIBLE);
                else
                    mBtnQuit.setVisibility(View.GONE);
                if (mInteractive.isKickoutRoomAvailable())
                    mBtnKick.setVisibility(View.VISIBLE);
                else
                    mBtnKick.setVisibility(View.GONE);
                break;
            case 2:
                if (mInteractive.isInviteAvailable())
                    mBtnInvite.setVisibility(View.VISIBLE);
                else
                    mBtnInvite.setVisibility(View.GONE);
                mBtnQuit.setVisibility(View.GONE);
                if (mInteractive.isKickoutRoomAvailable())
                    mBtnKick.setVisibility(View.VISIBLE);
                else
                    mBtnKick.setVisibility(View.GONE);
                break;
            case 3:
                mBtnInvite.setVisibility(View.GONE);
                mBtnQuit.setVisibility(View.GONE);
                if (mInteractive.isKickoutRoomAvailable())
                    mBtnKick.setVisibility(View.VISIBLE);
                else
                    mBtnKick.setVisibility(View.GONE);
                break;
            case 4:
                mBtnInvite.setVisibility(View.GONE);
                mBtnQuit.setVisibility(View.GONE);
                if (mInteractive.isKickoutRoomAvailable())
                    mBtnKick.setVisibility(View.VISIBLE);
                else
                    mBtnKick.setVisibility(View.GONE);
                break;
            default:
                mBtnInvite.setVisibility(View.GONE);
                mBtnQuit.setVisibility(View.GONE);
                mBtnKick.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_invite:
                mInteractive.invitePublish(mMember.userid, null);
                break;
            case R.id.btn_quit:
                mInteractive.kickoutStream(mMember.userid, null);
                break;
            case R.id.btn_kick:
                mInteractive.kickoutRoom(mMember.userid, null);
                break;
        }
        this.dismiss();
    }
}
