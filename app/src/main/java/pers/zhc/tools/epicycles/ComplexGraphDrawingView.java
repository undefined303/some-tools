package pers.zhc.tools.epicycles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import pers.zhc.tools.BaseView;
import pers.zhc.u.math.util.ComplexValue;

import java.util.ArrayList;
import java.util.List;

public class ComplexGraphDrawingView extends BaseView {

    private float width;
    private float height;
    private Paint mCoPaint;
    private Paint mPaint;
    private Path mPath;
    private boolean instanceFirst = true;
    static List<ComplexValue> pointList = null;

    public ComplexGraphDrawingView(Context context) {
        super(context);
        init();
    }

    private void init() {
        mCoPaint = new Paint();
        mCoPaint.setStrokeWidth(1);
        mCoPaint.setStyle(Paint.Style.STROKE);
        mCoPaint.setColor(Color.GRAY);
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        if (pointList == null) {
            pointList = new ArrayList<>();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        width = ((float) getWidth());
        height = ((float) getHeight());
        if (instanceFirst && pointList.size() != 0) {
            mPath = new Path();
            ComplexValue complexValue = pointList.get(0);
            mPath.moveTo(((float) (complexValue.re + width / 2D)), ((float) (height / 2D - complexValue.im)));
            int length = pointList.size();
            for (int i = 1; i < length; i++) {
                complexValue = pointList.get(i);
                mPath.lineTo(((float) (complexValue.re + width / 2D)), ((float) (height / 2D - complexValue.im)));
            }
            mPath.close();
            instanceFirst = false;
        }
        canvas.drawLine(0F, height / 2F, width, height / 2F, mCoPaint);
        canvas.drawLine(width / 2F, 0F, width / 2F, height, mCoPaint);
//            画实轴和虚轴 无箭头
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pointList.clear();
                mPath = new Path();
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                mPath.close();
                break;
            default:
                break;
        }
        pointList.add(new ComplexValue(x - width / 2D, -y + height / 2D));
        invalidate();
        return true;
    }
}