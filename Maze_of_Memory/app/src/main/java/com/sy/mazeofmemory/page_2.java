package com.sy.mazeofmemory;

import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class page_2 extends android.support.v4.app.Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.single_sub_page, container, false);
        LinearLayout background = (LinearLayout) linearLayout.findViewById(R.id.background);
        TextView page_num = (TextView) linearLayout.findViewById(R.id.page_num);
        page_num.setText(String.valueOf(2));
        background.setBackground(new ColorDrawable(0xff26abb5));
        return linearLayout;
    }
}
