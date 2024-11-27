package com.agile.tuon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends View {
    private Paint paint;
    private List<Float> data;
    private int barColor;
    private int textColor;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        data = new ArrayList<>();
        barColor = Color.parseColor("#0074D9");
        textColor = Color.parseColor("#333333");
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
        float maxValue = 0f;
        for (float value : data) {
            maxValue = Math.max(maxValue, value);
        }
        float barWidth = width / (data.size() * 2 + 1);

        paint.setTextSize(30f);

        for (int i = 0; i < data.size(); i++) {
            float value = data.get(i);
            float left = barWidth * (i * 2 + 1);
            float top = height * (1 - value / maxValue);
            float right = left + barWidth;
            float bottom = height;

            paint.setColor(barColor);
            canvas.drawRect(left, top, right, bottom, paint);

            paint.setColor(textColor);
            canvas.drawText(
                    String.valueOf((int) value),
                    left,
                    top - 10,
                    paint
            );
        }
    }
}
