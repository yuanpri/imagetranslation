package com.gpnu.yuan.livewords.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.gpnu.yuan.livewords.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置按钮点击事件，跳转页面
        findViewById(R.id.buttonGoToOnlineTranslationActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TranslationActivity.class);
                startActivity(intent);
            }
        });
        // 设置按钮点击事件，跳转
        findViewById(R.id.buttonGoToImageRecognition).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageTranslationActivity.class);
                startActivity(intent);
            }
        });
    }
}
