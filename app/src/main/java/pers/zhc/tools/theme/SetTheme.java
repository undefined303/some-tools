package pers.zhc.tools.theme;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.widget.Button;
import pers.zhc.tools.BaseActivity;
import pers.zhc.tools.R;

public class SetTheme extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_app_theme_activity);
        Button setBtn = findViewById(R.id.setting_btn);
        setBtn.setOnClickListener(v -> recreate());
        setBtn.setOnLongClickListener(v -> {
            setTheme(R.style.AppTheme);
            recreate();
            return true;
        });
    }
}
