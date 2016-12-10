package com.gamevenez.a21barth.a21barth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnPC = (Button) findViewById(R.id.btn_pc);
        btnPC.setOnClickListener(this);

        Button btnPlayOnline = (Button) findViewById(R.id.btn_play_online);
        btnPlayOnline.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId())
        {
            case R.id.btn_pc:
                intent = new Intent(this, GameActivity.class);
                startActivity(intent);
                break;

            case R.id.btn_play_online:
                intent = new Intent(this, OnlineActivity.class);
                startActivity(intent);
                break;
        }
    }
}
