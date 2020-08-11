package pers.zhc.tools.diary;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import pers.zhc.tools.BaseActivity;
import pers.zhc.tools.R;
import pers.zhc.tools.utils.Common;
import pers.zhc.tools.utils.ScrollEditText;
import pers.zhc.tools.utils.sqlite.SQLite;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author bczhc
 */
public class DiaryTakingActivity extends BaseActivity {

    private EditText et;
    private TextView charactersCountTV;
    private MyDate mDate;
    private SQLiteDatabase diaryDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_taking_activity);
        diaryDatabase = DiaryMainActivity.getDiaryDatabase(this);
        et = ((ScrollEditText) findViewById(R.id.et)).getEditText();
        Handler debounceHandler = new Handler();
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                debounceHandler.removeCallbacksAndMessages(null);
                debounceHandler.postDelayed(() -> showCharactersCount(), 2000);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.diary);
            charactersCountTV = new TextView(this);
            charactersCountTV.setTextColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                charactersCountTV.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            }
            actionBar.setCustomView(charactersCountTV);
            actionBar.show();
        }
        final Intent intent = getIntent();
        int[] date = intent.getIntArrayExtra("date");
        if (date == null) {
            final Calendar calendar = Calendar.getInstance();
            date = new int[]{calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)};
        }
        mDate = new MyDate(date);
        initDB();
        prepareContent();
    }

    private void showCharactersCount() {
        charactersCountTV.setText(getString(R.string.characters_count_tv, et.length()));
    }

    private void prepareContent() {
        final Cursor cursor = diaryDatabase.rawQuery("SELECT content FROM diary WHERE date=?", new String[]{mDate.toString()});
        String content = null;
        if (cursor.moveToFirst()) {
            content = cursor.getString(cursor.getColumnIndex("content"));
        }
        cursor.close();
        if (content != null) {
            et.setText(content);
            showCharactersCount();
        }
    }

    private void recordTime() {
        final Date date = new Date();
        final String time = new SimpleDateFormat("[HH:mm]").format(date);
        et.setText(String.valueOf(et.getText()) + '\n' + time);
    }

    private void initDB() {
        boolean newRec = !SQLite.checkRecordExistence(diaryDatabase, "diary", "date", mDate.toString());
        if (newRec) {
            ContentValues cv = new ContentValues();
            cv.put("date", mDate.toString());
            cv.put("content", "");
            try {
                diaryDatabase.insertOrThrow("diary", null, cv);
            } catch (SQLException e) {
                Common.showException(e, this);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.diary_taking_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.record_time) {
            recordTime();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        save();
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        save();
        super.onSaveInstanceState(outState);
    }

    private void save() {
        ContentValues cv = new ContentValues();
        cv.put("date", mDate.toString());
        cv.put("content", this.et.getText().toString());
        diaryDatabase.update("diary", cv, "date=?", new String[]{mDate.toString()});
    }

    static class MyDate {
        /**
         * month: 0 is JANUARY!
         */
        int year, month, day;

        MyDate(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        MyDate() {
        }

        MyDate(int[] date) {
            year = date[0];
            month = date[1];
            day = date[2];
        }

        MyDate(String dateString) {
            final String[] split = dateString.split("\\.");
            year = Integer.parseInt(split[0]);
            month = Integer.parseInt(split[1]) - 1;
            day = Integer.parseInt(split[2]);
        }

        MyDate(long timestamp) {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            this.year = calendar.get(Calendar.YEAR);
            this.month = calendar.get(Calendar.MONTH);
            this.day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MyDate myDate = (MyDate) o;

            if (year != myDate.year) {
                return false;
            }
            if (month != myDate.month) {
                return false;
            }
            return day == myDate.day;
        }

        @Override
        public int hashCode() {
            int result = year;
            result = 31 * result + month;
            result = 31 * result + day;
            return result;
        }

        @Override
        public String toString() {
            return year + "." + (month + 1) + "." + day;
        }

    }
}
