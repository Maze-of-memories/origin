package com.sy.mazeofmemory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class SingleSubPage extends android.support.v4.app.Fragment {

    private int btnCnt = 20;
    private Integer[] btn = new Integer[btnCnt];
    private ImageAdapter imageAdapter;

    private Context context;
    private int pageCnt;


    public SingleSubPage(Context context, int position) {
        this.context = context;
        this.pageCnt = position;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        for (int i = 0; i < btn.length; i++) {
            btn[i] = R.drawable.bo;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.activity_single_sub_page, container, false);

        GridView gridView = (GridView) linearLayout.findViewById(R.id.single_game_btn);
        gridView.setAdapter(imageAdapter = new ImageAdapter(getActivity()));
        gridView.setOnItemClickListener(gridviewOnItemClickListener);

        return linearLayout;
    }

    public GridView.OnItemClickListener gridviewOnItemClickListener
            = new GridView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Intent intent;

            //튜토리얼 클릭
            if (pageCnt == 0 && position == 0) {
                intent = new Intent(context, SingleTutorialActivity.class);
            } else {
                intent = new Intent(context, SingleGameActivity.class);
                if (pageCnt == 0) {
                    intent.putExtra("stage_num", position);
                } else {
                    intent.putExtra("stage_num", position + 1);
                }
            }
            intent.putExtra("position", position);
            startActivity(intent);

        }
    };

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return btn.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mContext);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setAdjustViewBounds(true);
            imageView.setPadding(10, 10, 10, 10);

            imageView.setImageResource(btn[position]);

            return imageView;
        }

    }

}

