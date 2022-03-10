package com.vhall.opensdk.interactive;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.vhall.framework.VhallSDK;
import com.vhall.ilss.VHInteractive;
import com.vhall.opensdk.R;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by huanan on 2017/5/15.
 */
public class MemberPopu extends PopupWindow {

    Context mContext;
    ListView mListView;
    List<Member> mUsers = new LinkedList<>();
    MyAdapter mAdapter;
    AdapterView.OnItemClickListener mOnItemClickListener;

    public MemberPopu(Context context) {
        super(context);
        this.mContext = context;
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.WHITE);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.member_layout, null);
        setContentView(root);
        mListView = root.findViewById(R.id.lv);
        mAdapter = new MyAdapter();
        mListView.setAdapter(mAdapter);
        if (mOnItemClickListener != null)
            mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public void setItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
        mListView.setOnItemClickListener(mOnItemClickListener);
    }

    public void refreshData(List<Member> memberList) {
        mUsers = memberList;
        mAdapter.notifyDataSetChanged();
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return mUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = View.inflate(mContext, R.layout.item_member, null);
            Member user = mUsers.get(position);
            String showText = user.userid;
            if (user.userid.equals(VhallSDK.getInstance().mUserId)) {
//                ((TextView) convertView).setText("我自己");
                showText = showText + "(本人)";
            }
            showText = showText + ":";
            switch (user.status) {
                case 1:
                    showText = showText + ":推流中";
                    break;
                case 2:
                    showText = showText + ":未上麦";
//                    ((TextView) convertView).setTextColor(Color.BLACK);//未上麦
                    break;
                case 3:
                    showText = showText + ":受邀中";
//                    ((TextView) convertView).setTextColor(Color.YELLOW);//受邀中
                    break;
                case 4:
                    showText = showText + ":申请中";
//                    ((TextView) convertView).setTextColor(Color.RED);//申请中
                    break;

            }
            ((TextView) convertView).setText(showText);
            return convertView;
        }
    }
}
