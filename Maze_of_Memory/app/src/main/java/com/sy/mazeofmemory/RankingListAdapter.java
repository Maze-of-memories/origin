package com.sy.mazeofmemory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Jun on 2015-01-22.
 * 랭킹 리스트뷰 어댑터
 */
public class RankingListAdapter extends ArrayAdapter<RankingItem> {
    private ArrayList<RankingItem> items;
    private LayoutInflater inflater;
    private Context context;
    int itemView;

    public RankingListAdapter(Context context, int itemView, ArrayList<RankingItem> items) {
        super(context, itemView, items);
        this.items = items;
        this.context = context;
        this.itemView = itemView;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = inflater.inflate(itemView, null);
        }

        RankingItem r = items.get(position);

        if(r != null) {
            // 순위 출력
            TextView rank = (TextView)v.findViewById(R.id.rank);
            rank.setText(position + 1 + "");

            // 프로필 사진 출력
            ImageView profilePicture = (ImageView)v.findViewById(R.id.profilePicture);
            setProfilePicture(profilePicture, r.getPictureUrl());

            // 닉네임 출력
            TextView nickname = (TextView)v.findViewById(R.id.nickname);
            nickname.setText(r.getNickname());

            // 별개수 출력
            TextView starCnt = (TextView)v.findViewById(R.id.starCnt);
            starCnt.setText(" x " + r.getStarCnt());

            // 점수 출력
            TextView score = (TextView)v.findViewById(R.id.score);
            score.setText(r.getScore() + "");
        }

        return v;
    }

    // 이미지의 URL을 이용하여 view에 출력한다.
    private void setProfilePicture(final ImageView view, final String url) {
        new AsyncTask<Void, Void, Void>() {

            URL u = null;
            Bitmap bmp = null;

            @Override
            protected Void doInBackground(Void... params) {

                try {
                    u = new URL(url);
                    bmp = BitmapFactory.decodeStream(u.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                view.setImageBitmap(bmp);
            }
        }.execute();
    }
}
