package com.example.clock.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ClockView extends View {

    private static final int FULL_CIRCLE_DEGREE = 360;
    private static final int UNIT_DEGREE = 6;

    private static final float UNIT_LINE_WIDTH = 8; // 刻度线的宽度
    private static final int HIGHLIGHT_UNIT_ALPHA = 0xFF;
    private static final int NORMAL_UNIT_ALPHA = 0x80;

    private static final float HOUR_NEEDLE_LENGTH_RATIO = 0.4f; // 时针长度相对表盘半径的比例
    private static final float MINUTE_NEEDLE_LENGTH_RATIO = 0.6f; // 分针长度相对表盘半径的比例
    private static final float SECOND_NEEDLE_LENGTH_RATIO = 0.8f; // 秒针长度相对表盘半径的比例
    private static final float WORLD_LENGTH_RATIO = 0.85f;  //数字的半径比例
    private static final float HOUR_NEEDLE_WIDTH = 12; // 时针的宽度
    private static final float MINUTE_NEEDLE_WIDTH = 8; // 分针的宽度
    private static final float SECOND_NEEDLE_WIDTH = 4; // 秒针的宽度
    private static final float WORLD_WIDTH = 1;   //数字的宽度
    private static final float WORD_SIZE = 45;    //数字的大小

    private static final float CIRCLE_BORDER_WIDTH = 12;    //中心圆的相关参数
    private static final float CIRCLE_INSIDE_WIDTH = 0;
    private static final float CENTER_CIRCLE_RATIO = 0.02f;

    private Calendar calendar = Calendar.getInstance();

    private float radius = 0; // 表盘半径
    private float centerX = 0; // 表盘圆心X坐标
    private float centerY = 0; // 表盘圆心Y坐标

    private float recHeight;    //字体的高度

    private List<RectF> unitLinePositions = new ArrayList<>();
    private Paint unitPaint = new Paint();
    private Paint needlePaint = new Paint();
    private Paint numberPaint = new Paint();
    private Paint centerPaint = new Paint();

    private Handler handler = new Handler();

    private String clockNumber[] = {"03","04","05","06","07","08","09","10","11","12","01","02"};

    public ClockView(Context context) {
        super(context);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        unitPaint.setAntiAlias(true);
        unitPaint.setColor(Color.WHITE);
        unitPaint.setStrokeWidth(UNIT_LINE_WIDTH);
        unitPaint.setStrokeCap(Paint.Cap.ROUND);
        unitPaint.setStyle(Paint.Style.STROKE);

        // TODO 设置绘制时、分、秒针的画笔: needlePaint
        needlePaint.setAntiAlias(true);
        needlePaint.setColor(Color.WHITE);
        needlePaint.setStrokeCap(Paint.Cap.ROUND);
        needlePaint.setStyle(Paint.Style.STROKE);

        // TODO 设置绘制时间数字的画笔: numberPaint
        numberPaint.setAntiAlias(true);
        numberPaint.setColor(Color.WHITE);
        numberPaint.setStrokeWidth(WORLD_WIDTH);
        numberPaint.setStrokeCap(Paint.Cap.ROUND);
        numberPaint.setAlpha(HIGHLIGHT_UNIT_ALPHA);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTextSize(WORD_SIZE);
        numberPaint.setStyle(Paint.Style.FILL);
        //获得文字矩形的高度
        Rect rect = new Rect();
        String tempText = "0123456789";
        numberPaint.getTextBounds(tempText, 0, tempText.length(), rect);
        recHeight = rect.height()/2.0f;

        //绘制中间的小圆的画笔
        centerPaint.setAntiAlias(true);
        centerPaint.setColor(Color.WHITE);
        centerPaint.setStrokeWidth(UNIT_LINE_WIDTH);
        centerPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        configWhenLayoutChanged();
    }

    private void configWhenLayoutChanged() {
        float newRadius = Math.min(getWidth(), getHeight()) / 2f;
        if (newRadius == radius) {
            return;
        }
        radius = newRadius;
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        // 当视图的宽高确定后就可以提前计算表盘的刻度线的起止坐标了
        for (int degree = 0; degree < FULL_CIRCLE_DEGREE; degree += UNIT_DEGREE) {
            double radians = Math.toRadians(degree);
            float startX = (float) (centerX + (radius * (1 - 0.05f)) * Math.cos(radians));
            float startY = (float) (centerX + (radius * (1 - 0.05f)) * Math.sin(radians));
            float stopX = (float) (centerX + radius * Math.cos(radians));
            float stopY = (float) (centerY + radius * Math.sin(radians));
            unitLinePositions.add(new RectF(startX, startY, stopX, stopY));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUnit(canvas);
        drawTimeNeedles(canvas);
        drawTimeNumbers(canvas);
        drawCenterCircle(canvas);
        // TODO 实现时间的转动，每一秒刷新一次
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                postInvalidate();
            }
            }, 1000);
    }

    // 绘制表盘上的刻度
    private void drawUnit(Canvas canvas) {
        for (int i = 0; i < unitLinePositions.size(); i++) {
            if (i % 5 == 0) {
                unitPaint.setAlpha(HIGHLIGHT_UNIT_ALPHA);
            } else {
                unitPaint.setAlpha(NORMAL_UNIT_ALPHA);
            }
            RectF linePosition = unitLinePositions.get(i);
            canvas.drawLine(linePosition.left, linePosition.top, linePosition.right, linePosition.bottom, unitPaint);
        }
    }

    private void drawTimeNeedles(Canvas canvas) {
        Time time = getCurrentTime();
        int hour = time.getHours();
        int minute = time.getMinutes();
        int second = time.getSeconds();
        // TODO 根据当前时间，绘制时针、分针、秒针
        /**
         * 思路：
         * 1、以时针为例，计算从0点（12点）到当前时间，时针需要转动的角度
         * 2、根据转动角度、时针长度和圆心坐标计算出时针终点坐标（起始点为圆心）
         * 3、从圆心到终点画一条线，此为时针
         * 注1：计算时针转动角度时要把时和分都得考虑进去
         * 注2：计算坐标时需要用到正余弦计算，请用Math.sin()和Math.cos()方法
         * 注3：Math.sin()和Math.cos()方法计算时使用不是角度而是弧度，所以需要先把角度转换成弧度，
         *     可以使用Math.toRadians()方法转换，例如Math.toRadians(180) = 3.1415926...(PI)
         * 注4：Android视图坐标系的0度方向是从圆心指向表盘3点方向，指向表盘的0点时是-90度或270度方向，要注意角度的转换
         */
        // int hourDegree = 180;
        // float endX = (float) (centerX + radius * HOUR_NEEDLE_LENGTH_RATIO * Math.cos(Math.toRadians(hourDegree)))

        //首先绘制秒针
        float secondDegree = second * UNIT_DEGREE - 90;
        float secendX = (float) (centerX + radius * SECOND_NEEDLE_LENGTH_RATIO * Math.cos(Math.toRadians(secondDegree)));
        float secendY = (float) (centerY + radius * SECOND_NEEDLE_LENGTH_RATIO * Math.sin(Math.toRadians(secondDegree)));
        needlePaint.setStrokeWidth(SECOND_NEEDLE_WIDTH);
        needlePaint.setAlpha(NORMAL_UNIT_ALPHA);
        canvas.drawLine(centerX,centerY,secendX,secendY,needlePaint);
        //然后绘制分针
        float minuteDegree = (minute + second/60.0f) * UNIT_DEGREE - 90;
        float minendX = (float) (centerX + radius * MINUTE_NEEDLE_LENGTH_RATIO * Math.cos(Math.toRadians(minuteDegree)));
        float minendY = (float) (centerY + radius * MINUTE_NEEDLE_LENGTH_RATIO * Math.sin(Math.toRadians(minuteDegree)));
        needlePaint.setStrokeWidth(MINUTE_NEEDLE_WIDTH);
        needlePaint.setAlpha(HIGHLIGHT_UNIT_ALPHA);
        canvas.drawLine(centerX, centerY, minendX,minendY,needlePaint);
        //最后绘制时针
        float hourDegree = (hour + minute/60.0f + second/3600.0f) * 30 - 90;
        float hourendX = (float) (centerX + radius * HOUR_NEEDLE_LENGTH_RATIO * Math.cos(Math.toRadians(hourDegree)));
        float hourendY = (float) (centerY + radius * HOUR_NEEDLE_LENGTH_RATIO * Math.sin(Math.toRadians(hourDegree)));
        needlePaint.setStrokeWidth(HOUR_NEEDLE_WIDTH);
        needlePaint.setAlpha(HIGHLIGHT_UNIT_ALPHA);
        canvas.drawLine(centerX, centerY, hourendX, hourendY, needlePaint);


    }

    private void drawTimeNumbers(Canvas canvas) {
        // TODO 绘制表盘时间数字（可选）
        for(int i=0; i<12; i++){
            float degree = i * 30;
            float wordX = (float) (centerX + radius * WORLD_LENGTH_RATIO * Math.cos(Math.toRadians(degree)));
            float wordY = (float) (centerY + radius * WORLD_LENGTH_RATIO * Math.sin(Math.toRadians(degree)))+recHeight;
            canvas.drawText(clockNumber[i],wordX,wordY,numberPaint);
        }
    }

    // 获取当前的时间：时、分、秒
    private Time getCurrentTime() {
        calendar.setTimeInMillis(System.currentTimeMillis());
        return new Time(
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }

    //绘制中心的小圆圈
    private void drawCenterCircle(Canvas canvas){
        centerPaint.setStrokeWidth(CIRCLE_BORDER_WIDTH);
        centerPaint.setStyle(Paint.Style.STROKE);
        centerPaint.setColor(Color.WHITE);
        canvas.drawCircle(centerX,centerY,radius*CENTER_CIRCLE_RATIO,centerPaint);
        centerPaint.setStrokeWidth(CIRCLE_INSIDE_WIDTH);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setARGB(255, 199, 199, 199);
        canvas.drawCircle(centerX,centerY,radius*CENTER_CIRCLE_RATIO,centerPaint);
    }
}
