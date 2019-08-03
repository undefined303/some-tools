package com.zhc.tools;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import com.zhc.codecs.R;
import com.zhc.tools.clipboard.Clip;
import com.zhc.tools.pi.Pi;
import com.zhc.tools.toast.AToast;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tools_activity);
        Button btn1 = findViewById(R.id.gen_pi);
        btn1.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, Pi.class);
            startActivity(intent);
        });
        Button btn2 = findViewById(R.id.toast);
        btn2.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, AToast.class);
            startActivity(intent);
        });
        Button btn3 = findViewById(R.id.clipboard);
        btn3.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(this, Clip.class);
            startActivity(intent);
        });
    }
}
