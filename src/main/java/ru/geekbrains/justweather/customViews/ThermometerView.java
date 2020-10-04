package ru.geekbrains.justweather.customViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import ru.geekbrains.justweather.R;

public class ThermometerView extends View {
    private static final String TAG = "BatteryView";

    private int thermometerColor = Color.GRAY;
    public static int levelColor = Color.GREEN;
    private int levelPressedColor = Color.WHITE;
    private RectF thermometerRectangle = new RectF();
    private Rect levelRectangle = new Rect();
    private RectF headRectangle = new RectF();
    private RectF headLevelRectangle = new RectF();
    private Paint levelPressedPaint;
    private Paint thermometerPaint;
    private Paint levelPaint;
    private int width = 0;
    private int height = 0;

    public static int level = 100;
    private boolean pressed = false;
    private OnClickListener listener;
    private static int padding = 10;
    private final static int round = 25;
    private final static int headRound = 30;
    public ThermometerView(Context context) {
        super(context);
        init();
    }
    public static int getLevel(){return level;}

    public ThermometerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs);
        init();
    }

    public ThermometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttr(context, attrs);
        init();
    }

    public ThermometerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttr(context, attrs);
        init();
    }

    private void initAttr(Context context, AttributeSet attrs) {

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThermometerView, 0,
                0);

        thermometerColor = typedArray.getColor(R.styleable.ThermometerView_thermometer_color, Color.GRAY);
        levelColor = typedArray.getColor(R.styleable.ThermometerView_level_color, Color.GREEN);
        levelPressedColor = typedArray.getColor(R.styleable.ThermometerView_level_pressed_color, Color.WHITE);
        level = typedArray.getInteger(R.styleable.ThermometerView_level, 100);
        padding = typedArray.getInteger(R.styleable.ThermometerView_padding, 10);
        typedArray.recycle();
    }

    private void init(){
        thermometerPaint = new Paint();
        thermometerPaint.setColor(thermometerColor);
        thermometerPaint.setStyle(Paint.Style.FILL);
        levelPaint = new Paint();
        levelPaint.setColor(levelColor);
        levelPaint.setStyle(Paint.Style.FILL);
        // Задать "краску" для нажатия на элемент +
        levelPressedPaint = new Paint();
        levelPressedPaint.setColor(levelPressedColor);
        levelPressedPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged");

        width = w - getPaddingLeft() - getPaddingRight();
        height = h - getPaddingTop() - getPaddingBottom();
        thermometerRectangle.set(padding,padding,width-padding,height-padding);
        headRectangle.set(0,height-width, width, height);
        headLevelRectangle.set(padding,height-width+padding, width-padding,height-padding);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");

        canvas.drawRoundRect(thermometerRectangle, round, round, thermometerPaint);
        canvas.drawRoundRect(headRectangle, headRound, headRound, thermometerPaint);

        if (pressed) {
            canvas.drawRect(levelRectangle, levelPressedPaint);
            canvas.drawRoundRect(headLevelRectangle, headRound, headRound, levelPressedPaint);
        } else {
            levelPaint.setColor(levelColor);
            setLevelRectangle();
            canvas.drawRect(levelRectangle, levelPaint);
            canvas.drawRoundRect(headLevelRectangle, headRound, headRound, levelPaint);
        }
    }

    private void setLevelRectangle(){
        levelRectangle.set(2*padding, (height-width+padding) - (int)((height-width)*((float)level/100))+padding,
                width-2*padding, height-width+padding+padding);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");

        int action = event.getAction();

        pressed = action == MotionEvent.ACTION_DOWN;
        if(pressed && listener != null){
            listener.onClick(this);
        }

        invalidate();

        return true;
    }

    @Override
    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }
    @Override
    protected void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow");
        super.onAttachedToWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        Log.d(TAG, "layout");
        super.layout(l, t, r, b);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas) {
        Log.d(TAG, "draw");
        super.draw(canvas);
    }

    @Override
    public void invalidate() {
        Log.d(TAG, "invalidate");
        super.invalidate();
    }

    @Override
    public void requestLayout() {
        Log.d(TAG, "requestLayout");
        super.requestLayout();
    }
}
