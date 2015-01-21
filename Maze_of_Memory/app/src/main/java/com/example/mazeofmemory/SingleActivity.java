package com.example.mazeofmemory;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class SingleActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single);

        // 튜토리얼 버튼
        Button btn;
        btn = (Button)findViewById(R.id.single_tutorial);
        btn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(SingleActivity.this, SingleTutorialActivity.class);
                startActivity(intent);
            }
        });

        // 랭킹보기 버튼
        Button btnShowRank = (Button)findViewById(R.id.btnShowRank);
        btnShowRank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SingleActivity.this, RankingActivity.class);
                startActivity(intent);
            }
        });


    }

}
