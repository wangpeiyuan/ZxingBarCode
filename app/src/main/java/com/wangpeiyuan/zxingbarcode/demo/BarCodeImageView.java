package com.wangpeiyuan.zxingbarcode.demo;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import com.wangpeiyuan.zxingbarcode.core.core.DecoderResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class BarCodeImageView extends AppCompatImageView {
    private static final String TAG = "BarCodeImageView";
    private Paint mPaint;
    private List<RectF> mRectFList;
    private int mViewWidth, mViewHeight;

    public BarCodeImageView(Context context) {
        super(context);
        init();
    }

    public BarCodeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarCodeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(8);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mViewHeight = h;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mRectFList = null;
        super.setImageBitmap(bm);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mRectFList != null && !mRectFList.isEmpty()) {
            canvas.save();
            canvas.concat(getImageMatrix());
            for (RectF rectF : mRectFList) {
                canvas.drawRect(rectF, mPaint);
            }
            canvas.restore();
        }
    }

    public Bitmap setImagePath(String path) {
        Log.d(TAG, "setImagePath: mViewWidth " + mViewWidth + " mViewHeight " + mViewHeight);
        Bitmap bitmap = BitmapUtil.INSTANCE.decodeFile(path, mViewWidth, mViewHeight);
        setImageBitmap(bitmap);
        return bitmap;
    }

    public void setQRCodeDecoderResult(List<DecoderResult> resultList) {
        if (resultList != null && !resultList.isEmpty()) {
            mRectFList = new ArrayList<>();
            for (DecoderResult result : resultList) {
                RectF resultRectF = result.getQRCodeExtendedRect();
                if (resultRectF == null) continue;
                float[] bitmapSize = getBitmapSize();
                float imageWidth = bitmapSize != null ? bitmapSize[0] : mViewWidth;
                float imageHeight = bitmapSize != null ? bitmapSize[1] : mViewHeight;
                resultRectF.left = (resultRectF.left * 1.0f / result.getBitmapWidth()) * imageWidth;
                resultRectF.top = (resultRectF.top * 1.0f / result.getBitmapHeight()) * imageHeight;
                resultRectF.right = (resultRectF.right * 1.0f / result.getBitmapWidth()) * imageWidth;
                resultRectF.bottom = (resultRectF.bottom * 1.0f / result.getBitmapHeight()) * imageHeight;
                Log.d(TAG, "setQRCodeDecoderResult: " + resultRectF.toString());
                mRectFList.add(resultRectF);
            }
            postInvalidate();
        }
    }

    private float[] getBitmapSize() {
        if (getDrawable() != null) {
            float[] size = new float[2];
            size[0] = ((BitmapDrawable) getDrawable()).getIntrinsicWidth();
            size[1] = ((BitmapDrawable) getDrawable()).getIntrinsicHeight();
            return size;
        }
        return null;
    }
}
