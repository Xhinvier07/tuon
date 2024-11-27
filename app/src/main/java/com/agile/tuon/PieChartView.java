package com.agile.tuon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PieChartView extends View {
    private Paint paint;
    private RectF rect;
    private List<Float> data;
    private List<Integer> colors;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new RectF();
        data = new ArrayList<>();
        colors = Arrays.asList(
                Color.parseColor("#FF4136"),
                Color.parseColor("#FF851B"),
                Color.parseColor("#FFDC00"),
                Color.parseColor("#2ECC40"),
                Color.parseColor("#0074D9")
        );
    }

    public void setData(List<Float> newData) {
        data = newData;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) return;

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f;
        float centerX = width / 2f;
        float centerY = height / 2f;

        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = 0f;
        float total = 0f;
        for (float value : data) {
            total += value;
        }

        for (int i = 0; i < data.size(); i++) {
            float sweepAngle = 360f * (data.get(i) / total);
            paint.setColor(colors.get(i % colors.size()));
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }
    }
}
