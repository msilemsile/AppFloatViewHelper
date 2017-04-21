import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

/**
 * 应用浮动窗体
 */

public class AppFloatViewHelper implements View.OnTouchListener {

    private WindowManager.LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;
    private View mContentView;
    private int mStatusHeight;
    private int mScreenWidth;

    private float mTouchStartX;
    private float mTouchStartY;
    private float mTouchMoveX;
    private float mTouchMoveY;
    private float x;
    private float y;

    private boolean isTouchClick;
    private int slop;
    private FloatOnClickListener floatOnClickListener;

    private AppFloatViewHelper() {
    }

    /**
     * @param context 上下文 最好是application 保证为应用全局
     */
    public AppFloatViewHelper(Context context) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.format = PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT > 24) {
                windowType = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                windowType = WindowManager.LayoutParams.TYPE_TOAST;
            }
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mWindowLayoutParams.type = windowType;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mStatusHeight = getStatusBarHeight(context);
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        slop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 获取状态栏高度(1.resource获取 2.反射获取 3.粗略计算)
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        if (result <= 0) {
            result = getStatusClassHeight(context);
        }
        if (result <= 0) {
            result = (int) (25 * context.getResources().getDisplayMetrics().density);
        }
        return result;
    }

    /**
     * 获得状态栏的高度
     */
    public static int getStatusClassHeight(Context context) {
        int statusHeight = -1;
        try {
            Class clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 设置浮动布局
     * @param contentView 浮动view （！注意创建view时最好要用application作为上下文）
     */
    public void setContentView(View contentView) {
        if (mWindowManager != null) {
            if (hasContentView()) {
                mWindowManager.removeView(mContentView);
                mContentView = null;
            }
            if (contentView != null) {
                mContentView = contentView;
                mWindowManager.addView(contentView, mWindowLayoutParams);
                mContentView.setOnTouchListener(this);
            }
        }
    }

    /**
     * 移除浮动布局
     */
    public void removeContentView() {
        if (mWindowManager != null) {
            if (hasContentView()) {
                mWindowManager.removeView(mContentView);
                mContentView = null;
            }
        }
    }

    /***
     * 是否有浮动窗体
     */
    public boolean hasContentView() {
        return mContentView != null && mContentView.getParent() != null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        x = event.getRawX();
        y = event.getRawY() - mStatusHeight;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                mTouchMoveX = x;
                mTouchMoveY = y;
                isTouchClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - mTouchMoveX) > slop || Math.abs(y - mTouchMoveY) > slop) {
                    isTouchClick = false;
                }
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                if (isTouchClick && floatOnClickListener != null && hasContentView()) {
                    floatOnClickListener.onFloatClick();
                    isTouchClick = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                isTouchClick = false;
                resetViewPosition();
                break;
        }
        return true;
    }

    /**
     * 悬浮view归位(左右停靠)
     */
    private void resetViewPosition() {
        int centerXPos = mScreenWidth / 2;
        if (mWindowLayoutParams.x >= centerXPos - mContentView.getWidth() / 2) {
            mWindowLayoutParams.x = mScreenWidth - mContentView.getWidth();
        } else {
            mWindowLayoutParams.x = 0;
        }
        mWindowManager.updateViewLayout(mContentView, mWindowLayoutParams);
    }

    /**
     * 更新悬浮view位置
     */
    private void updateViewPosition() {
        if (hasContentView()) {
            mWindowLayoutParams.x = (int) (x - mTouchStartX);
            mWindowLayoutParams.y = (int) (y - mTouchStartY);
            mWindowManager.updateViewLayout(mContentView, mWindowLayoutParams);
        }
    }

    public void setFloatOnClickListener(FloatOnClickListener floatOnClickListener) {
        this.floatOnClickListener = floatOnClickListener;
    }

    public interface FloatOnClickListener {
        void onFloatClick();
    }

}
