package ru.myapplication.sdslidersample.ui.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import ru.myapplication.sdslidersample.R;

public class IndicatorSeekBar extends View {
    private static final int THUMB_MAX_WIDTH = 30;
    private Context mContext;
    private Paint stockPaint; //the paint for seek bar drawing
    private OnSeekChangeListener mSeekChangeListener;
    private float mCustomDrawableMaxHeight;//the max height for custom drawable
    private float lastProgress;
    private float mFaultTolerance = -1;//the tolerance for user seek bar touching
    private boolean mClearPadding;
    private SeekParams mSeekParams;//save the params when seeking change.
    //seek bar
    private int mPaddingLeft;
    private int mPaddingRight;
    private int mMeasuredWidth;
    private int mPaddingTop;
    private float mSeekLength;//the total length of seek bar
    private float mSeekBlockLength;//the length for each section part to seek
    private boolean isTouching;//user is touching the seek bar
    private float mMax = 100f;
    private float mMin = 0f;
    private float progress;
    private boolean mIsFloatProgress;// true for the progress value in float,otherwise in int.
    private int mScale = 1;//the scale of the float progress.
    private boolean mUserSeekable; //true if the user can seek to change the progress,otherwise only can be changed by setProgress().
    private boolean mOnlyThumbDraggable;//only drag the seek bar's thumb can be change the progress
    private boolean mSeekSmoothly;//seek continuously
    private float[] progressArr = new float[]{0F, 10f, 20f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f}; //save the progress which at tickMark position.
    //indicator
    private boolean mIndicatorStayAlways;//true if the indicator didn't dismiss after initial.
    private View mIndicatorContentView;//the view to replace the raw indicator all view
    //tick marks
    private float[] mTickMarksX;//the tickMark's drawing X anchor
    private int mTicksCount;//the num of tickMarks
    private int mUnSelectedTickMarksColor;//the color for the tickMarks those thumb haven't reach.
    private int mSelectedTickMarksColor;//the color for the tickMarks those thumb swept.
    private float mTickRadius;//the tick's radius
    private Bitmap mUnselectTickMarksBitmap;//the drawable bitmap for tick
    private Bitmap mSelectTickMarksBitmap;//the drawable bitmap for tick
    private Drawable mTickMarksDrawable;
    private int mShowTickMarksType;
    private boolean mTickMarksEndsHide;//true if want to hide the tickMarks which in both side ends of seek bar
    private boolean mTickMarksSweptHide;//true if want to hide the tickMarks which on thumb left.
    private int mTickMarksSize;//the width of tickMark
    //track
    private boolean mTrackRoundedCorners;
    private RectF progressTrack;//the background track on the thumb start
    private RectF mBackgroundTrack;//the background track on the thumb ends
    private int mBackgroundTrackSize;
    private int mProgressTrackSize;
    private int mBackgroundTrackColor;
    private int mProgressTrackColor;
    //thumb
    private float mThumbRadius;//the thumb's radius
    private float mThumbTouchRadius;//the thumb's radius when touching
    private Bitmap mThumbBitmap;//the drawable bitmap for thumb
    private int mThumbColor;
    private int mThumbSize;
    private Drawable mThumbDrawable;
    private Bitmap mPressedThumbBitmap;//the bitmap for pressing status
    private int mPressedThumbColor;//the color for pressing status
    //thumb text
    private boolean mHideThumb;
    private boolean mAdjustAuto;

    public IndicatorSeekBar(Context context) {
        this(context, null);
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initAttrs(mContext, attrs);
        initParams();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initAttrs(mContext, attrs);
        initParams();
    }

    /**
     * if you want a java build, the way like:
     * <p>
     * IndicatorSeekBar
     * .with(getContext())
     * .max(50)
     * .min(10)
     * ...
     * .build();
     */
    IndicatorSeekBar(Builder builder) {
        super(builder.context);
        this.mContext = builder.context;
        int defaultPadding = SizeUtils.dp2px(mContext, 16);
        setPadding(defaultPadding, getPaddingTop(), defaultPadding, getPaddingBottom());
        this.apply(builder);
        initParams();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        Builder builder = new Builder(context);
        if (attrs == null) {
            this.apply(builder);
            return;
        }
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.IndicatorSeekBar);
        //seekBar
        progress = ta.getFloat(R.styleable.IndicatorSeekBar_isb_progress, builder.progress);
        mIsFloatProgress = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_progress_value_float, builder.progressValueFloat);
        mUserSeekable = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_user_seekable, builder.userSeekable);
        mClearPadding = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_clear_default_padding, builder.clearPadding);
        mOnlyThumbDraggable = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_only_thumb_draggable, builder.onlyThumbDraggable);
        mSeekSmoothly = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_seek_smoothly, builder.seekSmoothly);
        //track
        mBackgroundTrackSize = ta.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_track_background_size, builder.trackBackgroundSize);
        mProgressTrackSize = ta.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_track_progress_size, builder.trackProgressSize);
        mBackgroundTrackColor = ta.getColor(R.styleable.IndicatorSeekBar_isb_track_background_color, builder.trackBackgroundColor);
        mProgressTrackColor = ta.getColor(R.styleable.IndicatorSeekBar_isb_track_progress_color, builder.trackProgressColor);
        mTrackRoundedCorners = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_track_rounded_corners, builder.trackRoundedCorners);
        //thumb
        mThumbSize = ta.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_thumb_size, builder.thumbSize);
        mThumbDrawable = ta.getDrawable(R.styleable.IndicatorSeekBar_isb_thumb_drawable);
        mAdjustAuto = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_thumb_adjust_auto, true);
        initThumbColor(ta.getColorStateList(R.styleable.IndicatorSeekBar_isb_thumb_color), builder.thumbColor);
        //tickMarks
        mTicksCount = ta.getInt(R.styleable.IndicatorSeekBar_isb_ticks_count, builder.tickCount);
        mShowTickMarksType = ta.getInt(R.styleable.IndicatorSeekBar_isb_show_tick_marks_type, builder.showTickMarksType);
        mTickMarksSize = ta.getDimensionPixelSize(R.styleable.IndicatorSeekBar_isb_tick_marks_size, builder.tickMarksSize);
        initTickMarksColor(ta.getColorStateList(R.styleable.IndicatorSeekBar_isb_tick_marks_color), builder.tickMarksColor);
        mTickMarksDrawable = ta.getDrawable(R.styleable.IndicatorSeekBar_isb_tick_marks_drawable);
        mTickMarksSweptHide = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_tick_marks_swept_hide, builder.tickMarksSweptHide);
        mTickMarksEndsHide = ta.getBoolean(R.styleable.IndicatorSeekBar_isb_tick_marks_ends_hide, builder.tickMarksEndsHide);
        //tickTexts

        int indicatorContentViewId = ta.getResourceId(R.styleable.IndicatorSeekBar_isb_indicator_content_layout, 0);
        if (indicatorContentViewId > 0) {
            mIndicatorContentView = View.inflate(mContext, indicatorContentViewId, null);
        }

        ta.recycle();
    }

    private void initParams() {
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mBackgroundTrackSize = mProgressTrackSize;
        }
        if (mThumbDrawable == null) {
            mThumbRadius = mThumbSize / 2.0f;
            mThumbTouchRadius = mThumbRadius * 1.2f;
        } else {
            mThumbRadius = Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH), mThumbSize) / 2.0f;
            mThumbTouchRadius = mThumbRadius;
        }
        if (mTickMarksDrawable == null) {
            mTickRadius = mTickMarksSize / 2.0f;
        } else {
            mTickRadius = Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH), mTickMarksSize) / 2.0f;
        }
        mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f;
        initStrokePaint();
        lastProgress = progress;

        collectTicksInfo();

        progressTrack = new RectF();
        mBackgroundTrack = new RectF();
        initDefaultPadding();
    }

    private void collectTicksInfo() {
        //todo remove limit on increase
        if (progressArr.length < 0 || progressArr.length > 50) {
            throw new IllegalArgumentException("the Argument: TICK COUNT must be limited between (0-50), Now is " + mTicksCount);
        }
        if (mTicksCount != 0) {
            mTickMarksX = new float[mTicksCount];
        }
    }

    public synchronized void setPossibleValues(float[] arr) {
        this.progressArr = arr;
        mMin = arr[0];
        mMax = arr[arr.length - 1];
        collectTicksInfo();
        refreshSeekBarLocation();
        invalidate();
    }

    public float[] getPossibleValues() {
        return progressArr;
    }

    private void initDefaultPadding() {
        if (!mClearPadding) {
            int normalPadding = SizeUtils.dp2px(mContext, 16);
            if (getPaddingLeft() == 0) {
                setPadding(normalPadding, getPaddingTop(), getPaddingRight(), getPaddingBottom());
            }
            if (getPaddingRight() == 0) {
                setPadding(getPaddingLeft(), getPaddingTop(), normalPadding, getPaddingBottom());
            }
        }
    }

    private void initStrokePaint() {
        if (stockPaint == null) {
            stockPaint = new Paint();
        }
        if (mTrackRoundedCorners) {
            stockPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        stockPaint.setAntiAlias(true);
        if (mBackgroundTrackSize > mProgressTrackSize) {
            mProgressTrackSize = mBackgroundTrackSize;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = Math.round(mCustomDrawableMaxHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(resolveSize(SizeUtils.dp2px(mContext, 170), widthMeasureSpec), height);
        initSeekBarInfo();
        refreshSeekBarLocation();
    }

    private void initSeekBarInfo() {
        mMeasuredWidth = getMeasuredWidth();
        mPaddingLeft = getPaddingStart();
        mPaddingRight = getPaddingEnd();
        mPaddingTop = getPaddingTop();
        mSeekLength = mMeasuredWidth - mPaddingLeft - mPaddingRight;
        mSeekBlockLength = mSeekLength / (mTicksCount - 1 > 0 ? mTicksCount - 1 : 1);
    }

    private void refreshSeekBarLocation() {
        initTrackLocation();

        //init tick's X and text's X location;
        if (mTickMarksX == null) {
            return;
        }
        //adjust thumb auto,so find out the closest progress in the mProgressArr array and replace it.
        //it is not necessary to adjust thumb while count is less than 2.
        if (mTicksCount > 2) {
            progress = progressArr[getClosestIndex()];
            lastProgress = progress;
        }
        refreshThumbCenterXByProgress(progress);
    }

    private void initTrackLocation() {
        progressTrack.left = mPaddingLeft;
        progressTrack.top = mPaddingTop + mThumbTouchRadius;
        //ThumbCenterX
        progressTrack.right = (progress - mMin) * mSeekLength / (getAmplitude()) + mPaddingLeft;
        progressTrack.bottom = progressTrack.top;
        //ThumbCenterX
        mBackgroundTrack.left = progressTrack.right;
        mBackgroundTrack.top = progressTrack.bottom;
        mBackgroundTrack.right = mMeasuredWidth - mPaddingRight;
        mBackgroundTrack.bottom = progressTrack.bottom;
    }


    /**
     * calculate the thumb's centerX by the changing progress.
     */
    private void refreshThumbCenterXByProgress(float progress) {
        Log.d("TAG1", "refreshThumbCenterXByProgress(float progress)");
        progressTrack.right = (progress - mMin) * mSeekLength / (getAmplitude()) + mPaddingLeft;
        Log.d("TAG1", "progressTrack.right = (progress - mMin) * mSeekLength / (getAmplitude()) + mPaddingLeft" + " === " +
               progressTrack.right + " = (" + progress + " - " + mMin + ") * " + mSeekLength + " / (" + getAmplitude() + " + " + mPaddingLeft + ")") ;
        mBackgroundTrack.left = progressTrack.right;
        Log.d("TAG1", "mBackgroundTrack.left = progressTrack.right" + " === " +
                mBackgroundTrack.left + " = " + progressTrack.right);
        Log.d("TAG1", "\n\n\n\n" + System.lineSeparator());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        drawTrack(canvas);
        drawTickMarks(canvas);
        drawThumb(canvas);
    }

    private void drawTrack(Canvas canvas) {
        //draw progress track
        stockPaint.setColor(mProgressTrackColor);
        stockPaint.setStrokeWidth(mProgressTrackSize);
        canvas.drawLine(progressTrack.left, progressTrack.top, progressTrack.right, progressTrack.bottom, stockPaint);

        //draw BG track
        stockPaint.setColor(mBackgroundTrackColor);
        stockPaint.setStrokeWidth(mBackgroundTrackSize);
        canvas.drawLine(mBackgroundTrack.left, mBackgroundTrack.top, mBackgroundTrack.right, mBackgroundTrack.bottom, stockPaint);
    }

    private void drawTickMarks(Canvas canvas) {
        if (mTicksCount == 0 || (mShowTickMarksType == TickMarkType.NONE && mTickMarksDrawable == null)) {
            return;
        }
        float thumbCenterX = getThumbCenterX();
        for (int i = 0; i < mTickMarksX.length; i++) {
            float thumbPosFloat = getThumbPosOnTickFloat();
            if (mTickMarksSweptHide) {
                if (thumbCenterX >= mTickMarksX[i]) {
                    continue;
                }
            }
            if (mTickMarksEndsHide) {
                if (i == 0 || i == mTickMarksX.length - 1) {
                    continue;
                }
            }
            if (i == getThumbPosOnTick() && mTicksCount > 2 && !mSeekSmoothly) {
                continue;
            }
            if (i <= thumbPosFloat) {
                stockPaint.setColor(getLeftSideTickColor());
            } else {
                stockPaint.setColor(getRightSideTickColor());
            }
            if (mTickMarksDrawable != null) {
                if (mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null) {
                    initTickMarksBitmap();
                }
                if (mSelectTickMarksBitmap == null || mUnselectTickMarksBitmap == null) {
                    //please check your selector drawable's format and correct.
                    throw new IllegalArgumentException("the format of the selector TickMarks drawable is wrong!");
                }
                if (i <= thumbPosFloat) {
                    canvas.drawBitmap(mSelectTickMarksBitmap, mTickMarksX[i] - mUnselectTickMarksBitmap.getWidth() / 2.0f, progressTrack.top - mUnselectTickMarksBitmap.getHeight() / 2.0f, stockPaint);
                } else {
                    canvas.drawBitmap(mUnselectTickMarksBitmap, mTickMarksX[i] - mUnselectTickMarksBitmap.getWidth() / 2.0f, progressTrack.top - mUnselectTickMarksBitmap.getHeight() / 2.0f, stockPaint);
                }
                continue;
            }
            if (mShowTickMarksType == TickMarkType.OVAL) {
                canvas.drawCircle(mTickMarksX[i], progressTrack.top, mTickRadius, stockPaint);
            } else if (mShowTickMarksType == TickMarkType.DIVIDER) {
                int rectWidth = SizeUtils.dp2px(mContext, 1);
                float dividerTickHeight;
                if (thumbCenterX >= mTickMarksX[i]) {
                    dividerTickHeight = getLeftSideTrackSize();
                } else {
                    dividerTickHeight = getRightSideTrackSize();
                }
                canvas.drawRect(mTickMarksX[i] - rectWidth, progressTrack.top - dividerTickHeight / 2.0f, mTickMarksX[i] + rectWidth, progressTrack.top + dividerTickHeight / 2.0f, stockPaint);
            } else if (mShowTickMarksType == TickMarkType.SQUARE) {
                canvas.drawRect(mTickMarksX[i] - mTickMarksSize / 2.0f, progressTrack.top - mTickMarksSize / 2.0f, mTickMarksX[i] + mTickMarksSize / 2.0f, progressTrack.top + mTickMarksSize / 2.0f, stockPaint);
            }
        }
    }

    private void drawThumb(Canvas canvas) {
        if (mHideThumb) {
            return;
        }
        float thumbCenterX = getThumbCenterX();
        if (mThumbDrawable != null) { //check user has set thumb drawable or not.ThumbDrawable first, thumb color for later.
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                initThumbBitmap();
            }
            if (mThumbBitmap == null || mPressedThumbBitmap == null) {
                //please check your selector drawable's format and correct.
                throw new IllegalArgumentException("the format of the selector thumb drawable is wrong!");
            }
            stockPaint.setAlpha(255);
            if (isTouching) {
                canvas.drawBitmap(mPressedThumbBitmap, thumbCenterX - mPressedThumbBitmap.getWidth() / 2.0f, progressTrack.top - mPressedThumbBitmap.getHeight() / 2.0f, stockPaint);
            } else {
                canvas.drawBitmap(mThumbBitmap, thumbCenterX - mThumbBitmap.getWidth() / 2.0f, progressTrack.top - mThumbBitmap.getHeight() / 2.0f, stockPaint);
            }
        } else {
            if (isTouching) {
                stockPaint.setColor(mPressedThumbColor);
            } else {
                stockPaint.setColor(mThumbColor);
            }
            canvas.drawCircle(thumbCenterX, progressTrack.top, isTouching ? mThumbTouchRadius : mThumbRadius, stockPaint);
        }
    }

    private float getThumbCenterX() {
        return progressTrack.right;
    }

    private int getLeftSideTickColor() {
        return mSelectedTickMarksColor;
    }

    private int getRightSideTickColor() {
        return mUnSelectedTickMarksColor;
    }


    /**
     * get the track size which on the thumb left in R2L/L2R case.
     */
    private int getLeftSideTrackSize() {
        return mProgressTrackSize;
    }

    /**
     * get the track size which on the thumb right in R2L/L2R case.
     */
    private int getRightSideTrackSize() {
        return mBackgroundTrackSize;
    }

    private int getThumbPosOnTick() {
        if (mTicksCount != 0) {
            return Math.round((getThumbCenterX() - mPaddingLeft) / mSeekBlockLength);
        }
        //when tick count = 0 ; seek bar has not tick(continuous series), return 0;
        return 0;
    }

    private float getThumbPosOnTickFloat() {
        if (mTicksCount != 0) {
            return (getThumbCenterX() - mPaddingLeft) / mSeekBlockLength;
        }
        return 0;
    }

    private int getHeightByRatio(Drawable drawable, int width) {
        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        return Math.round(1.0f * width * intrinsicHeight / intrinsicWidth);
    }

    private Bitmap getDrawBitmap(Drawable drawable, boolean isThumb) {
        if (drawable == null) {
            return null;
        }
        int width;
        int height;
        int maxRange = SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH);
        int intrinsicWidth = drawable.getIntrinsicWidth();
        if (intrinsicWidth > maxRange) {
            if (isThumb) {
                width = mThumbSize;
            } else {
                width = mTickMarksSize;
            }
            height = getHeightByRatio(drawable, width);

            if (width > maxRange) {
                width = maxRange;
                height = getHeightByRatio(drawable, width);
            }
        } else {
            width = drawable.getIntrinsicWidth();
            height = drawable.getIntrinsicHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * initial the color for the thumb.
     * <p>
     * <p>
     * NOTICE: make sure the format of color selector you set is right.
     * int[][] states = colorStateList.getStates();
     * (1) if the states.length == 1,the way you set the thumb color like :
     * app:isb_thumb_color="#XXXXXX"  or
     * app:isb_thumb_color="@color/color_name" ;
     * <p>
     * (2) if the states.length == 3,the way you set the thumb color like :
     * app:isb_thumb_color="@color/selector_color_file_name". the file(located at res/color/)'s format should like:
     * <p>
     * <?xml version="1.0" encoding="utf-8"?>
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:color="#555555" android:state_pressed="true" />  <!--this color is for thumb which is at pressing status-->
     * <item android:color="#555555"/>                                <!--for thumb which is at normal status-->
     * </selector>
     * <p>
     * (3) if the states.length == other, the color's format you set is not support.
     */
    private void initThumbColor(ColorStateList colorStateList, int defaultColor) {
        //if you didn't set the thumb color, set a default color.
        if (colorStateList == null) {
            mThumbColor = defaultColor;
            mPressedThumbColor = mThumbColor;
            return;
        }
        int[][] states = null;
        int[] colors = null;
        Class<? extends ColorStateList> aClass = colorStateList.getClass();
        try {
            Field[] f = aClass.getDeclaredFields();
            for (Field field : f) {
                field.setAccessible(true);
                if ("mStateSpecs".equals(field.getName())) {
                    states = (int[][]) field.get(colorStateList);
                }
                if ("mColors".equals(field.getName())) {
                    colors = (int[]) field.get(colorStateList);
                }
            }
            if (states == null || colors == null) {
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Something wrong happened when parseing thumb selector color.");
        }
        if (states.length == 1) {
            mThumbColor = colors[0];
            mPressedThumbColor = mThumbColor;
        } else if (states.length == 2) {
            for (int i = 0; i < states.length; i++) {
                int[] attr = states[i];
                if (attr.length == 0) {//didn't have state,so just get color.
                    mPressedThumbColor = colors[i];
                    continue;
                }
                switch (attr[0]) {
                    case android.R.attr.state_pressed:
                        mThumbColor = colors[i];
                        break;
                    default:
                        //the color selector file was set by a wrong format , please see above to correct.
                        throw new IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.");
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw new IllegalArgumentException("the selector color file you set for the argument: isb_thumb_color is in wrong format.");
        }

    }

    /**
     * initial the color for the tick masks
     * <p>
     * <p>
     * NOTICE: make sure the format of color selector you set is right.
     * int[][] states = colorStateList.getStates();
     * (1) if the states.length == 1,the way you set the tick marks' color like :
     * app:isb_tick_marks_color="#XXXXXX"  or
     * app:isb_tick_marks_color="@color/color_name" ;
     * <p>
     * (2) if the states.length == 2,the way you set the tick marks' color like :
     * app:isb_tick_marks_color="@color/selector_color_file_name". the file(located at res/color/)'s format should like:
     * <p>
     * <?xml version="1.0" encoding="utf-8"?>
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:color="#555555" android:state_selected="true" />  <!--this color is for marks those are at left side of thumb-->
     * <item android:color="#555555"/>                                 <!--for marks those are at right side of thumb-->
     * </selector>
     * <p>
     * (3) if the states.length == other, the way you set is not support.
     */
    private void initTickMarksColor(ColorStateList colorStateList, int defaultColor) {
        //if you didn't set the tick's text color, set a default selector color file.
        if (colorStateList == null) {
            mSelectedTickMarksColor = defaultColor;
            mUnSelectedTickMarksColor = mSelectedTickMarksColor;
            return;
        }
        int[][] states = null;
        int[] colors = null;
        Class<? extends ColorStateList> aClass = colorStateList.getClass();
        try {
            Field[] f = aClass.getDeclaredFields();
            for (Field field : f) {
                field.setAccessible(true);
                if ("mStateSpecs".equals(field.getName())) {
                    states = (int[][]) field.get(colorStateList);
                }
                if ("mColors".equals(field.getName())) {
                    colors = (int[]) field.get(colorStateList);
                }
            }
            if (states == null || colors == null) {
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Something wrong happened when parsing thumb selector color." + e.getMessage());
        }
        if (states.length == 1) {
            mSelectedTickMarksColor = colors[0];
            mUnSelectedTickMarksColor = mSelectedTickMarksColor;
        } else if (states.length == 2) {
            for (int i = 0; i < states.length; i++) {
                int[] attr = states[i];
                if (attr.length == 0) {//didn't have state,so just get color.
                    mUnSelectedTickMarksColor = colors[i];
                    continue;
                }
                switch (attr[0]) {
                    case android.R.attr.state_selected:
                        mSelectedTickMarksColor = colors[i];
                        break;
                    default:
                        //the color selector file was set by a wrong format , please see above to correct.
                        throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.");
                }
            }
        } else {
            //the color selector file was set by a wrong format , please see above to correct.
            throw new IllegalArgumentException("the selector color file you set for the argument: isb_tick_marks_color is in wrong format.");
        }
    }

    /**
     * initial the bitmap for the thumb.
     * <p>
     * <p>
     * NOTICE: make sure the format of drawable selector file you set is right.
     * int stateCount = listDrawable.getStateCount();
     * (1) if the drawable instanceof BitmapDrawable,the way you set like :
     * app:isb_thumb_drawable="@drawable/ic_launcher"
     * <p>
     * (2) if the drawable instanceof StateListDrawable,the way you set like :
     * app:isb_thumb_drawable="@drawable/selector_thumb_drawable". the file(located at res/drawable/)'s format should like:
     * <p>
     * <?xml version="1.0" encoding="utf-8"?>
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:drawable="@drawable/ic_launcher" android:state_pressed="true" />  <!--this drawable is for thumb when pressing-->
     * <item android:drawable="@drawable/ic_launcher_round" />  <!--for thumb when normal-->
     * </selector>
     */
    private void initThumbBitmap() {
        if (mThumbDrawable == null) {
            return;
        }
        if (mThumbDrawable instanceof StateListDrawable) {
            try {
                StateListDrawable listDrawable = (StateListDrawable) mThumbDrawable;
                Class<? extends StateListDrawable> aClass = listDrawable.getClass();
                int stateCount = (int) aClass.getMethod("getStateCount").invoke(listDrawable);
                if (stateCount == 2) {
                    Method getStateSet = aClass.getMethod("getStateSet", int.class);
                    Method getStateDrawable = aClass.getMethod("getStateDrawable", int.class);
                    for (int i = 0; i < stateCount; i++) {
                        int[] stateSet = (int[]) getStateSet.invoke(listDrawable, i);
                        if (stateSet.length > 0) {
                            if (stateSet[0] == android.R.attr.state_pressed) {
                                Drawable stateDrawable = (Drawable) getStateDrawable.invoke(listDrawable, i);
                                mPressedThumbBitmap = getDrawBitmap(stateDrawable, true);
                            } else {
                                //please check your selector drawable's format, please see above to correct.
                                throw new IllegalArgumentException("the state of the selector thumb drawable is wrong!");
                            }
                        } else {
                            Drawable stateDrawable = (Drawable) getStateDrawable.invoke(listDrawable, i);
                            mThumbBitmap = getDrawBitmap(stateDrawable, true);
                        }
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw new IllegalArgumentException("the format of the selector thumb drawable is wrong!");
                }
            } catch (Exception e) {
                mThumbBitmap = getDrawBitmap(mThumbDrawable, true);
                mPressedThumbBitmap = mThumbBitmap;
            }
        } else {
            mThumbBitmap = getDrawBitmap(mThumbDrawable, true);
            mPressedThumbBitmap = mThumbBitmap;
        }
    }

    /**
     * initial the bitmap for the thickMarks.
     * <p>
     * <p>
     * NOTICE: make sure the format of drawable selector file you set is right.
     * int stateCount = listDrawable.getStateCount();
     * (1) if the drawable instanceof BitmapDrawable,the way you set like :
     * app:isb_tick_marks_drawable="@drawable/ic_launcher"
     * <p>
     * (2) if the drawable instanceof StateListDrawable,the way you set like :
     * app:isb_tick_marks_drawable="@drawable/selector_thumb_drawable". the file(located at res/drawable/)'s format should like:
     * <p>
     * <?xml version="1.0" encoding="utf-8"?>
     * <selector xmlns:android="http://schemas.android.com/apk/res/android">
     * <item android:drawable="@drawable/ic_launcher" android:state_selected="true" />  <!--this drawable is for thickMarks which thumb swept-->
     * <item android:drawable="@drawable/ic_launcher_round" />  <!--for thickMarks which thumb haven't reached-->
     * </selector>
     */
    private void initTickMarksBitmap() {
        if (mTickMarksDrawable instanceof StateListDrawable) {
            StateListDrawable listDrawable = (StateListDrawable) mTickMarksDrawable;
            try {
                Class<? extends StateListDrawable> aClass = listDrawable.getClass();
                Method getStateCount = aClass.getMethod("getStateCount");
                int stateCount = (int) getStateCount.invoke(listDrawable);
                if (stateCount == 2) {
                    Method getStateSet = aClass.getMethod("getStateSet", int.class);
                    Method getStateDrawable = aClass.getMethod("getStateDrawable", int.class);
                    for (int i = 0; i < stateCount; i++) {
                        int[] stateSet = (int[]) getStateSet.invoke(listDrawable, i);
                        if (stateSet.length > 0) {
                            if (stateSet[0] == android.R.attr.state_selected) {
                                Drawable stateDrawable = (Drawable) getStateDrawable.invoke(listDrawable, i);
                                mSelectTickMarksBitmap = getDrawBitmap(stateDrawable, false);
                            } else {
                                //please check your selector drawable's format, please see above to correct.
                                throw new IllegalArgumentException("the state of the selector TickMarks drawable is wrong!");
                            }
                        } else {
                            Drawable stateDrawable = (Drawable) getStateDrawable.invoke(listDrawable, i);
                            mUnselectTickMarksBitmap = getDrawBitmap(stateDrawable, false);
                        }
                    }
                } else {
                    //please check your selector drawable's format, please see above to correct.
                    throw new IllegalArgumentException("the format of the selector TickMarks drawable is wrong!");
                }
            } catch (Exception e) {
                mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false);
                mSelectTickMarksBitmap = mUnselectTickMarksBitmap;
            }
        } else {
            mUnselectTickMarksBitmap = getDrawBitmap(mTickMarksDrawable, false);
            mSelectTickMarksBitmap = mUnselectTickMarksBitmap;
        }

    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled == isEnabled()) {
            return;
        }
        super.setEnabled(enabled);
        if (isEnabled()) {
            setAlpha(1.0f);
            if (mIndicatorStayAlways) {
                mIndicatorContentView.setAlpha(1.0f);
            }
        } else {
            setAlpha(0.3f);
            if (mIndicatorStayAlways) {
                mIndicatorContentView.setAlpha(0.3f);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        ViewParent parent = getParent();
        if (parent == null) {
            return super.dispatchTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                parent.requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                parent.requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("isb_instance_state", super.onSaveInstanceState());
        bundle.putFloat("isb_progress", progress);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            setProgress(bundle.getFloat("isb_progress"));
            super.onRestoreInstanceState(bundle.getParcelable("isb_instance_state"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mUserSeekable || !isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                performClick();
                float mX = event.getX();
                if (isTouchSeekBar(mX, event.getY())) {
                    if ((mOnlyThumbDraggable && !isTouchThumb(mX))) {
                        return false;
                    }
                    isTouching = true;
                    if (mSeekChangeListener != null) {
                        mSeekChangeListener.onStartTrackingTouch(this);
                    }
                    refreshSeekBar(event);
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                refreshSeekBar(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                if (mSeekChangeListener != null) {
                    mSeekChangeListener.onStopTrackingTouch(this);
                }
                if (!autoAdjustThumb()) {
                    invalidate();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void refreshSeekBar(MotionEvent event) {
        refreshThumbCenterXByProgress(calculateProgress(calculateTouchX(adjustTouchX(event))));
        setSeekListener(true);
        invalidate();
    }

    private boolean progressChange() {
        if (mIsFloatProgress) {
            return lastProgress != progress;
        } else {
            return Math.round(lastProgress) != Math.round(progress);
        }
    }

    private float adjustTouchX(MotionEvent event) {
        float mTouchXCache;
        if (event.getX() < mPaddingLeft) {
            mTouchXCache = mPaddingLeft;
        } else if (event.getX() > mMeasuredWidth - mPaddingRight) {
            mTouchXCache = mMeasuredWidth - mPaddingRight;
        } else {
            mTouchXCache = event.getX();
        }
        return mTouchXCache;
    }

    private float calculateProgress(float touchX) {
        lastProgress = progress;
        progress = mMin + (getAmplitude()) * (touchX - mPaddingLeft) / mSeekLength;
        return progress;
    }

    private float calculateTouchX(float touchX) {
        float touchXTemp = touchX;
        //make sure the seek bar to seek smoothly always
        // while the tick's count is less than 3(tick's count is 1 or 2.).
        if (mTicksCount > 2 && !mSeekSmoothly) {
            int touchBlockSize = Math.round((touchX - mPaddingLeft) / mSeekBlockLength);
            touchXTemp = mSeekBlockLength * touchBlockSize + mPaddingLeft;
        }
        return touchXTemp;
    }

    private boolean isTouchSeekBar(float mX, float mY) {
        if (mFaultTolerance == -1) {
            mFaultTolerance = SizeUtils.dp2px(mContext, 5);
        }
        boolean inWidthRange = mX >= (mPaddingLeft - 2 * mFaultTolerance) && mX <= (mMeasuredWidth - mPaddingRight + 2 * mFaultTolerance);
        boolean inHeightRange = mY >= progressTrack.top - mThumbTouchRadius - mFaultTolerance && mY <= progressTrack.top + mThumbTouchRadius + mFaultTolerance;
        return inWidthRange && inHeightRange;
    }

    private boolean isTouchThumb(float mX) {
        float rawTouchX;
        refreshThumbCenterXByProgress(progress);
        rawTouchX = progressTrack.right;
        return rawTouchX - mThumbSize / 2f <= mX && mX <= rawTouchX + mThumbSize / 2f;
    }

    private boolean autoAdjustThumb() {
        if (mTicksCount < 3 || !mSeekSmoothly) {//it is not necessary to adjust while count less than 2.
            return false;
        }
        if (!mAdjustAuto) {
            return false;
        }
        final int closestIndex = getClosestIndex();
        final float touchUpProgress = progress;
        ValueAnimator animator = ValueAnimator.ofFloat(0, Math.abs(touchUpProgress - progressArr[closestIndex]));
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lastProgress = progress;
                if (touchUpProgress - progressArr[closestIndex] > 0) {
                    progress = touchUpProgress - (Float) animation.getAnimatedValue();
                } else {
                    progress = touchUpProgress + (Float) animation.getAnimatedValue();
                }
                refreshThumbCenterXByProgress(progress);
                //the auto adjust was happened after user touched up, so from user is false.
                setSeekListener(false);
                invalidate();
            }
        });
        return true;
    }

    /**
     * transfer the progress value to string type
     */
    private String getProgressString(float progress) {
        if (mIsFloatProgress) {
            return FormatUtils.fastFormat(progress, mScale);
        } else {
            return String.valueOf(Math.round(progress));
        }
    }

    private int getClosestIndex() {
        int closestIndex = 0;
        float amplitude = Math.abs(mMax - mMin);
        for (int i = 0; i < progressArr.length; i++) {
            float amplitudeTemp = Math.abs(progressArr[i] - progress);
            if (amplitudeTemp <= amplitude) {
                amplitude = amplitudeTemp;
                closestIndex = i;
            }
        }
        return closestIndex;
    }

    private float getAmplitude() {
        return (mMax - mMin) > 0 ? (mMax - mMin) : 1;
    }

    private void setSeekListener(boolean formUser) {
        if (mSeekChangeListener == null) {
            return;
        }
        if (progressChange()) {
            mSeekChangeListener.onSeeking(collectParams(formUser));
        }
    }

    private SeekParams collectParams(boolean formUser) {
        if (mSeekParams == null) {
            mSeekParams = new SeekParams(this);
        }
        mSeekParams.progress = getProgress();
        mSeekParams.progressFloat = getProgressFloat();
        mSeekParams.fromUser = formUser;
        //for discrete series seek bar
        if (mTicksCount > 2) {
            int rawThumbPos = getThumbPosOnTick();
            mSeekParams.thumbPosition = rawThumbPos;
        }
        return mSeekParams;
    }

    private void apply(Builder builder) {
        //seek bar
        this.progress = builder.progress;
        this.mIsFloatProgress = builder.progressValueFloat;
        this.mTicksCount = builder.tickCount;
        this.mSeekSmoothly = builder.seekSmoothly;
        this.mUserSeekable = builder.userSeekable;
        this.mClearPadding = builder.clearPadding;
        this.mOnlyThumbDraggable = builder.onlyThumbDraggable;
        //indicator
        this.mIndicatorContentView = builder.indicatorContentView;
        //track
        this.mBackgroundTrackSize = builder.trackBackgroundSize;
        this.mBackgroundTrackColor = builder.trackBackgroundColor;
        this.mProgressTrackSize = builder.trackProgressSize;
        this.mProgressTrackColor = builder.trackProgressColor;
        this.mTrackRoundedCorners = builder.trackRoundedCorners;
        //thumb
        this.mThumbSize = builder.thumbSize;
        this.mThumbDrawable = builder.thumbDrawable;
        initThumbColor(builder.thumbColorStateList, builder.thumbColor);
        //tickMarks
        this.mShowTickMarksType = builder.showTickMarksType;
        this.mTickMarksSize = builder.tickMarksSize;
        this.mTickMarksDrawable = builder.tickMarksDrawable;
        this.mTickMarksEndsHide = builder.tickMarksEndsHide;
        this.mTickMarksSweptHide = builder.tickMarksSweptHide;
        initTickMarksColor(builder.tickMarksColorStateList, builder.tickMarksColor);
    }

    /**
     * @param indicatorStayAlways IndicatorStayLayout call this, always true.
     */
    void setIndicatorStayAlways(boolean indicatorStayAlways) {
        this.mIndicatorStayAlways = indicatorStayAlways;
    }

    View getIndicatorContentView() {
        return mIndicatorContentView;
    }



    /*------------------API START-------------------*/

    /**
     * call this to new a builder with default params.
     *
     * @param context context environment
     * @return Builder
     */
    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    /**
     * get the tick' count
     *
     * @return tick' count
     */
    public int getTickCount() {
        return mTicksCount;
    }

    /**
     * Get the seek bar's current level of progress in float type.
     *
     * @return current progress in float type.
     */
    public synchronized float getProgressFloat() {
        BigDecimal bigDecimal = BigDecimal.valueOf(progress);
        return bigDecimal.setScale(mScale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Get the seek bar's current level of progress in int type.
     *
     * @return progress in int type.
     */
    public int getProgress() {
        return Math.round(progress);
    }

    /**
     * @return the upper limit of this seek bar's range.
     */
    public float getMax() {
        return mMax;
    }

    /**
     * the lower limit of this seek bar's range.
     *
     * @return the seek bar min value
     */
    public float getMin() {
        return mMin;
    }

    /**
     * the listener to listen the seeking params changing.
     *
     * @return seeking listener.
     */
    public OnSeekChangeListener getOnSeekChangeListener() {
        return mSeekChangeListener;
    }

    /**
     * Sets the current progress to the specified value.also,
     * if the seek bar's tick'count is larger than 2,the progress will adjust to the closest tick's progress auto.
     *
     * @param progress a new progress value , if the new progress is less than min ,
     *                 it will set to min;
     *                 if over max ,will be max.
     */
    public synchronized void setProgress(float progress) {
        lastProgress = this.progress;
        this.progress = progress < mMin ? mMin : (progress > mMax ? mMax : progress);
        //adjust to the closest tick's progress
        if ((!mSeekSmoothly) && mTicksCount > 2) {
            this.progress = progressArr[getClosestIndex()];
        }
        setSeekListener(false);
        refreshThumbCenterXByProgress(this.progress);
        postInvalidate();
    }


    /**
     * Set a new thumb drawable.
     *
     * @param drawable the drawable for thumb,selector drawable is ok.
     *                 selector format:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="@drawable/ic_launcher" android:state_pressed="true" />  <!--this drawable is for thumb when pressing-->
    //<item android:drawable="@drawable/ic_launcher_round" />  < !--for thumb when normal-->
    //</selector>
    public void setThumbDrawable(Drawable drawable) {
        if (drawable == null) {
            this.mThumbDrawable = null;
            this.mThumbBitmap = null;
            this.mPressedThumbBitmap = null;
        } else {
            this.mThumbDrawable = drawable;
            this.mThumbRadius = Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH), mThumbSize) / 2.0f;
            this.mThumbTouchRadius = mThumbRadius;
            this.mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f;
            initThumbBitmap();
        }
        requestLayout();
        invalidate();
    }

    /**
     * call this will do not draw thumb, true if hide.
     */
    public void hideThumb(boolean hide) {
        mHideThumb = hide;
        invalidate();
    }

    /**
     * set the seek bar's thumb's color.
     *
     * @param thumbColor colorInt
     */
    public void thumbColor(@ColorInt int thumbColor) {
        this.mThumbColor = thumbColor;
        this.mPressedThumbColor = thumbColor;
        invalidate();
    }

    /**
     * set the seek bar's thumb's selector color.
     *
     * @param thumbColorStateList color selector
     *                            selector format like:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_pressed="true" />  <!--this color is for thumb which is at pressing status-->
    //<item android:color="@color/color_blue" />                                <!--for thumb which is at normal status-->
    //</selector>
    public void thumbColorStateList(@NonNull ColorStateList thumbColorStateList) {
        initThumbColor(thumbColorStateList, mThumbColor);
        invalidate();
    }

    /**
     * Set a new tick marks drawable.
     *
     * @param drawable the drawable for marks,selector drawable is ok.
     *                 selector format:
     */
    //< ?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:drawable="@drawable/ic_launcher" android:state_selected="true" />  < !--this drawable is for thickMarks which thumb swept-->
    //<item android:drawable="@drawable/ic_launcher_round" />  < !--for thickMarks which thumb haven't reached-->
    //</selector>
    public void setTickMarksDrawable(Drawable drawable) {
        if (drawable == null) {
            this.mTickMarksDrawable = null;
            this.mUnselectTickMarksBitmap = null;
            this.mSelectTickMarksBitmap = null;
        } else {
            this.mTickMarksDrawable = drawable;
            this.mTickRadius = Math.min(SizeUtils.dp2px(mContext, THUMB_MAX_WIDTH), mTickMarksSize) / 2.0f;
            this.mCustomDrawableMaxHeight = Math.max(mThumbTouchRadius, mTickRadius) * 2.0f;
            initTickMarksBitmap();
        }
        invalidate();
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColor colorInt
     */
    public void tickMarksColor(@ColorInt int tickMarksColor) {
        this.mSelectedTickMarksColor = tickMarksColor;
        this.mUnSelectedTickMarksColor = tickMarksColor;
        invalidate();
    }

    /**
     * set the seek bar's tick's color.
     *
     * @param tickMarksColorStateList colorInt
     *                                selector format like:
     */
    //<?xml version="1.0" encoding="utf-8"?>
    //<selector xmlns:android="http://schemas.android.com/apk/res/android">
    //<item android:color="@color/colorAccent" android:state_selected="true" />  <!--this color is for marks those are at left side of thumb-->
    //<item android:color="@color/color_gray" />                                 <!--for marks those are at right side of thumb-->
    //</selector>
    public void tickMarksColor(@NonNull ColorStateList tickMarksColorStateList) {
        initTickMarksColor(tickMarksColorStateList, mSelectedTickMarksColor);
        invalidate();
    }


    /**
     * The specified scale for the progress value,
     * make sure you had chosen the float progress type
     * <p>
     * such as:
     * scale = 3; progress: 1.78627347 to 1.786
     * scale = 4; progress: 1.78627347 to 1.7863
     * <p>
     * make sure you have call the attr progress_value_float=true before, otherwise no change.
     *
     * @param scale scale for the float type progress value.
     */
    public void setDecimalScale(int scale) {
        this.mScale = scale;
    }


    /**
     * Set the listener to listen the seeking params changing.
     *
     * @param listener OnSeekChangeListener
     */
    public void setOnSeekChangeListener(@NonNull OnSeekChangeListener listener) {
        this.mSeekChangeListener = listener;
    }


    /**
     * prevent user from seeking
     *
     * @param seekAble true if user can seek
     */
    public void setUserSeekAble(boolean seekAble) {
        this.mUserSeekable = seekAble;
    }

    /**
     * Sets the tick count
     *
     * @param tickCount
     */
    public synchronized void setTickCount(int tickCount) {
        if (mTicksCount < 0 || mTicksCount > 50) {
            throw new IllegalArgumentException("the Argument: TICK COUNT must be limited between (0-50), Now is " + mTicksCount);
        }
        mTicksCount = tickCount;
        collectTicksInfo();
        initSeekBarInfo();
        refreshSeekBarLocation();
        invalidate();
    }

    /**
     * Sets the thumb move to the closed tick after touched up automatically, default true
     *
     * @param adjustAuto true if auto move after touched up.
     */
    public void setThumbAdjustAuto(boolean adjustAuto) {
        mAdjustAuto = adjustAuto;
    }


}
