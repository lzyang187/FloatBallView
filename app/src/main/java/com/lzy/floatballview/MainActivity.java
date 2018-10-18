package com.lzy.floatballview;

import android.os.Bundle;
import android.widget.Toast;

import com.lzy.floatballview.model.FloatBall;
import com.lzy.floatballview.view.FloatBallView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatBallView floatBallView = findViewById(R.id.floatballview);
        List<FloatBall> floatBalls = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            floatBalls.add(new FloatBall("搜索热词" + i));
        }
        floatBalls.add(new FloatBall("很长很长很长搜索热词"));
        floatBallView.setDatas(floatBalls);
        floatBallView.setmClickListener(new FloatBallView.OnItemClickListener() {
            @Override
            public void onItemClick(int position, FloatBall ball) {
                Toast.makeText(MainActivity.this, position + " " + ball.name, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
