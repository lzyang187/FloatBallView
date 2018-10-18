package com.lzy.floatballview.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.lzy.floatballview.R;
import com.lzy.floatballview.model.FloatBall;
import com.lzy.floatballview.model.FloatPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author: cyli8
 * @date: 2018/10/15 16:31
 */
public class FloatBallView extends FrameLayout {
    private static final int WHAT_ADD_PROGRESS = 1;
    /**
     * view变化的y抖动范围
     */
    private static final int CHANGE_RANGE = 10;
    /**
     * 控制抖动动画执行的快慢，人眼不能识别16ms以下的
     */
    public static final int PROGRESS_DELAY_MILLIS = 12;
    /**
     * 添加水滴时动画显示view执行的时间
     */
    public static final int ANIMATION_SHOW_VIEW_DURATION = 500;
    /**
     * 控制水滴动画的快慢
     */
    private List<Float> mSpeedds = Arrays.asList(0.5f, 0.3f, 0.2f, 0.1f);

    public static final List<FloatPoint> FLOAT_POINT_LIST = Arrays.asList(
            new FloatPoint(0.4f, 0.4f), new FloatPoint(0.1f, 0.1f), new FloatPoint(0.4f, 0.1f),
            new FloatPoint(0.7f, 0.1f), new FloatPoint(0.25f, 0.25f), new FloatPoint(0.55f, 0.25f),
            new FloatPoint(0.1f, 0.4f), new FloatPoint(0.7f, 0.4f), new FloatPoint(0.25f, 0.55f),
            new FloatPoint(0.55f, 0.55f), new FloatPoint(0.1f, 0.7f), new FloatPoint(0.4f, 0.7f),
            new FloatPoint(0.7f, 0.7f)
    );
    private List<FloatPoint> mCurrentCanChose = new ArrayList<>();

    private Random mRandom = new Random();
    private List<View> mViews = new ArrayList<>();
    private LayoutInflater mInflater;
    private boolean mIsOpenAnimtion;//是否开启动画
    private boolean mIsCancelAnimtion;//是否销毁动画
    private int mMaxX, mMaxY;//子view的x坐标和y坐标的最大取值

    public interface OnItemClickListener {
        void onItemClick(int position, FloatBall ball);
    }

    private OnItemClickListener mClickListener;

    public void setmClickListener(OnItemClickListener clickListener) {
        this.mClickListener = clickListener;
    }

    public FloatBallView(@NonNull Context context) {
        this(context, null);
    }

    public FloatBallView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatBallView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(getContext());
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_ADD_PROGRESS:
                    //根据isCancelAnimtion来标识是否退出，防止界面销毁时，再一次改变UI
                    if (mIsCancelAnimtion) {
                        return;
                    }
                    setOffSet();
                    mHandler.sendEmptyMessageDelayed(WHAT_ADD_PROGRESS, PROGRESS_DELAY_MILLIS);
                    break;
            }
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxX = w;
        mMaxY = h;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onDestroy();
    }

    private void reset() {
        mIsCancelAnimtion = true;
        mIsOpenAnimtion = false;
        for (int i = 0; i < mViews.size(); i++) {
            removeView(mViews.get(i));
        }
        mViews.clear();
        mCurrentCanChose.clear();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置数据源
     */
    public void setDatas(final List<FloatBall> balls) {
        if (balls == null || balls.isEmpty()) {
            return;
        }
        //确保初始化完成
        post(new Runnable() {
            @Override
            public void run() {
                handleDatas(balls);
            }
        });
    }

    /**
     * 设置数据源
     */
    private void handleDatas(List<FloatBall> balls) {
        reset();
        mIsCancelAnimtion = false;
        resetCurrentCanChoseRandoms();
        addFloatBallView(balls);
        setViewsSpeed();
        startAnimation();
    }

    private void resetCurrentCanChoseRandoms() {
        mCurrentCanChose.addAll(FLOAT_POINT_LIST);
    }

    /**
     * 添加水滴view
     */
    private void addFloatBallView(List<FloatBall> waters) {
        for (int i = 0; i < waters.size(); i++) {
            final FloatBall ball = waters.get(i);
            View view = mInflater.inflate(R.layout.ball_item, this, false);
            TextView tv = view.findViewById(R.id.tv_ball);
            view.setTag(ball);
            tv.setText(ball.name);
            setViewClick(i, view);
            //随机设置view动画的方向
            view.setTag(R.string.isUp, mRandom.nextBoolean());
            setChildViewLocation(view);
            mViews.add(view);
            addShowViewAnimation(view);
        }
    }

    /**
     * 添加显示动画
     */
    private void addShowViewAnimation(View view) {
        addView(view);
        view.setAlpha(0);
        view.setScaleX(0);
        view.setScaleY(0);
        view.animate().alpha(1).scaleX(1).scaleY(1).setDuration(ANIMATION_SHOW_VIEW_DURATION).start();
    }

    /**
     * 处理view点击
     */
    private void setViewClick(final int index, final View view) {
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tag = view.getTag();
                if (tag instanceof FloatBall) {
                    FloatBall ball = (FloatBall) tag;
                    if (mClickListener != null) {
                        mClickListener.onItemClick(index, ball);
                    }
                }
                view.setTag(R.string.original_y, view.getY());
            }
        });
    }

    /**
     * 设置view在父控件中的位置
     */
    private void setChildViewLocation(View view) {
        FloatPoint point = getPoint();
        view.setX(point.x * mMaxX);
        view.setY(point.y * mMaxY);
        view.setTag(R.string.original_y, view.getY());
    }

    private FloatPoint getPoint() {
        if (mCurrentCanChose.size() <= 0) {
            //防止水滴别可选项的个数还要多，这里就重新对可选项赋值
            resetCurrentCanChoseRandoms();
        }
        int i = mRandom.nextInt(mCurrentCanChose.size());
        FloatPoint point = mCurrentCanChose.get(i);
        mCurrentCanChose.remove(i);
        return point;
    }

    /**
     * 设置所有子view的加速度
     */
    private void setViewsSpeed() {
        for (int i = 0; i < mViews.size(); i++) {
            View view = mViews.get(i);
            setSpeed(view);
        }
    }

    /**
     * 设置View的spd
     */
    private void setSpeed(View view) {
        float spd = mSpeedds.get(mRandom.nextInt(mSpeedds.size()));
        view.setTag(R.string.speed, spd);
    }

    /**
     * 设置偏移
     */
    private void setOffSet() {
        for (int i = 0; i < mViews.size(); i++) {
            View view = mViews.get(i);
            //拿到上次view保存的速度
            float spd = (float) view.getTag(R.string.speed);
            //水滴初始的位置
            float original = (float) view.getTag(R.string.original_y);
            float step = spd;
            boolean isUp = (boolean) view.getTag(R.string.isUp);
            float translationY;
            //根据水滴tag中的上下移动标识移动view
            if (isUp) {
                translationY = view.getY() - step;
            } else {
                translationY = view.getY() + step;
            }
            //对水滴位移范围的控制
            if (translationY - original > CHANGE_RANGE) {
                translationY = original + CHANGE_RANGE;
                view.setTag(R.string.isUp, true);
            } else if (translationY - original < -CHANGE_RANGE) {
                translationY = original - CHANGE_RANGE;
                setSpeed(view);
                view.setTag(R.string.isUp, false);
            }
            view.setY(translationY);
        }
    }

    /**
     * 开启水滴抖动动画
     */
    private void startAnimation() {
        if (mIsOpenAnimtion) {
            return;
        }

        mHandler.sendEmptyMessage(WHAT_ADD_PROGRESS);
        mIsOpenAnimtion = true;
    }

    /**
     * 销毁
     */
    private void onDestroy() {
        mIsCancelAnimtion = true;
        mHandler.removeCallbacksAndMessages(this);
    }
}
