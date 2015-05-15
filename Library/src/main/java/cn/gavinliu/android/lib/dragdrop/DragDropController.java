package cn.gavinliu.android.lib.dragdrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gavin on 15-5-15.
 */
public class DragDropController {

    private static final String TAG = "DragDropController";

    private Context mContext;

    private boolean mIsDraging;

    private DragView mDragView;

    private MenuZone mMenuZone;

    private List<MenuZone> mMenus;

    private View mItemView;

    public DragDropController(Context ctx) {
        mContext = ctx;
    }

    void addMenuZone(MenuZone menuZone) {
        if (mMenus == null) {
            mMenus = new ArrayList<MenuZone>();
        }
        mMenus.add(menuZone);
    }

    interface DragDrorpListener {

        void onDragStart();

        void onDragEnter();

        void onDragExit();

        void onDragEnd();

        void onDrop();

    }

    public void startDrag(View v) {

        int[] position = new int[2];
        v.getLocationOnScreen(position);
        int x = position[0];
        int y = position[1];

        Bitmap bitmap = getViewBitmap(v);
        mDragView = new DragView(mContext, x, y, v.getWidth(), v.getHeight());
        mDragView.setImageBitmap(bitmap);
        mDragView.onDragStart();

        mIsDraging = true;

        mItemView = v;

        mItemView.setVisibility(View.INVISIBLE);
    }

    private float mMotionDownX;
    private float mMotionDownY;


    public boolean onInterceptTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionDownX = ev.getRawX();
                mMotionDownY = ev.getRawY();
                break;
        }

        return mIsDraging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!mIsDraging) {
            return false;
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionDownX = ev.getRawX();
                mMotionDownY = ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:

                int moveX = (int) (mMotionDownX - ev.getRawX());
                int moveY = (int) (mMotionDownY - ev.getRawY());

                if (mDragView != null) {
                    mDragView.move(moveX, moveY);
                }
                Log.d(TAG, "mMotionDownX:" + mMotionDownX);

                Log.d(TAG, "moveX:" + moveX);

                if (mMenus != null) {
                    for (MenuZone zone : mMenus) {
                        if (zone.isContains(ev.getRawX(), ev.getRawY())) {
                            zone.onDragEnter();
                            mDragView.onDragEnter();

                            mMenuZone = zone;
                        } else {
                            zone.onDragExit();
                            mDragView.onDragExit();

                            mMenuZone = null;
                        }
                    }
                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mDragView != null) {
                    mDragView.onDragEnd();
                    mDragView = null;
                }

                if (mMenuZone != null) {
                    mMenuZone.onDragEnd();
                    mMenuZone = null;
                }

                mIsDraging = false;

                mItemView.setVisibility(View.VISIBLE);

                break;
        }

        return true;
    }

    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
    }

}