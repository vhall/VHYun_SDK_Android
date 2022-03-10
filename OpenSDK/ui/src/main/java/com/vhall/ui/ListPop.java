package com.vhall.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

/**
 * @author hkl
 * Date: 2019-07-24 16:58
 */
public class ListPop extends PopupWindow {
    private OnClickListener onClickListener;

    public ListPop(Context context, final List<String> data) {
        super(context);
        if (data == null) {
            return;
        }
        Activity activity = (Activity) context;
        Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int y = point.y;
        setWidth(y / 2);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        ColorDrawable dw = new ColorDrawable(Color.TRANSPARENT);
        setBackgroundDrawable(dw);
        setFocusable(true);
        View root = View.inflate(context, R.layout.pop_list_layout, null);
        LinearLayout ll = root.findViewById(R.id.ll);
        if (data.size() > 0) {
            ll.removeAllViews();
            for (int i = 0; i < data.size(); i++) {
                final String text = data.get(i);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.weight = 1;
                TextView textView = new TextView(context);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(20);
                textView.setText(text);
                textView.setLayoutParams(layoutParams);
                textView.setGravity(Gravity.CENTER);
                ll.addView(textView);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (onClickListener != null) {
                            onClickListener.onClick(text);
                            dismiss();
                        }
                    }
                });
            }
        }
        setContentView(root);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(String data);
        void dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (onClickListener != null) {
            onClickListener.dismiss();
        }
    }
}
