package com.wangpeiyuan.zxingbarcode.core.zxing;

import android.graphics.*;
import android.media.ThumbnailUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.wangpeiyuan.zxingbarcode.core.core.QRCodeParams;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangpeiyuan on 2018/12/30.
 */
public class ZXingEncoder {
    private ZXingEncoder() {
    }

    public static Bitmap encoderQRCode(QRCodeParams qrCodeParams) throws Exception {
        if (qrCodeParams == null || !qrCodeParams.validate()) {
            throw new IllegalArgumentException("code params must be no null or code params is invalid");
        }
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        int width = qrCodeParams.getWidth();
        int height = qrCodeParams.getHeight();

        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeParams.getContent(), BarcodeFormat.QR_CODE,
                width, height, getEncodeHint(qrCodeParams));

        Bitmap preBitmap = qrCodeParams.getPreBitmap();
        if (preBitmap != null) {
            preBitmap = Bitmap.createScaledBitmap(preBitmap, width, height, false);
        }

        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y)) {// true 表示该处为二维码的有效信息
                    pixels[y * width + x] = preBitmap != null ? preBitmap.getPixel(x, y) : qrCodeParams.getPreColor(); // 信息色块像素设置
                } else {
                    pixels[y * width + x] = qrCodeParams.getBgColor(); // 背景色块像素设置
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        if (qrCodeParams.getLogoInfo() != null || qrCodeParams.getBackgroundInfo() != null) {
            return addLogoOrBackground(bitmap, qrCodeParams.getLogoInfo(), qrCodeParams.getBackgroundInfo());
        }

        return bitmap;
    }

    private static Map<EncodeHintType, Object> getEncodeHint(QRCodeParams qrCodeParams) {
        Map<EncodeHintType, Object> hints = new HashMap<>(3);
        //容错级别 (支持级别:{@link ErrorCorrectionLevel })。传null时,zxing源码默认使用 "L"
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, qrCodeParams.getMargin());
        return hints;
    }

    /**
     * 增加 logo、背景
     */
    private static Bitmap addLogoOrBackground(Bitmap srcBitmap, QRCodeParams.LogoInfo logoInfo,
                                              QRCodeParams.BackgroundInfo backgroundInfo) {
        if (srcBitmap == null) {
            return null;
        }
        if (logoInfo != null && logoInfo.getLogoBitmap() != null && !logoInfo.getLogoBitmap().isRecycled()) {
            srcBitmap = addLogo(srcBitmap, logoInfo);
        }
        if (backgroundInfo != null && backgroundInfo.getBgBitmap() != null && !backgroundInfo.getBgBitmap().isRecycled()) {
            int sW = srcBitmap.getWidth();
            int sH = srcBitmap.getHeight();

            Bitmap bgBitmap = backgroundInfo.getBgBitmap();
            int bgW = bgBitmap.getWidth();
            int bgH = bgBitmap.getHeight();

            if (bgW < sW) {
                bgW = sW;
            }
            if (bgH < sH) {
                bgH = sH;
            }

            if (bgW != bgBitmap.getWidth() || bgH != bgBitmap.getHeight()) {
                bgBitmap = Bitmap.createScaledBitmap(bgBitmap, bgW, bgH, true);
            }
            Bitmap bitmap = Bitmap.createBitmap(bgW, bgH, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bgBitmap, 0, 0, null);
            Paint paint = new Paint();
            paint.setAlpha((int) (backgroundInfo.getBarCodeAlpha() * (255.0f / 100)));
            canvas.drawBitmap(srcBitmap, backgroundInfo.getBarCodeLeft(), backgroundInfo.getBarCodeTop(), paint);

            recycleBitmap(srcBitmap);
            recycleBitmap(bgBitmap);

            return bitmap;
        }

        return srcBitmap;
    }

    private static Bitmap addLogo(Bitmap srcBitmap, QRCodeParams.LogoInfo logoInfo) {
        if (srcBitmap == null) {
            return null;
        }
        if (logoInfo == null || logoInfo.getLogoBitmap() == null) {
            return srcBitmap;
        }
        Bitmap logoBitmap = logoInfo.getLogoBitmap();

        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();

        logoBitmap = ThumbnailUtils.extractThumbnail(logoBitmap,
                (int) (srcWidth * 0.25f) - logoInfo.getPaddingWhite() * 2,
                (int) (srcHeight * 0.25f) - logoInfo.getPaddingWhite() * 2,
                ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);

        int left = srcWidth / 2 - logoWidth / 2;
        int top = srcHeight / 2 - logoHeight / 2;

        drawPaddingWhiteAndStroke(logoInfo, canvas, left, top, logoWidth, logoHeight);

        canvas.drawBitmap(logoBitmap, left, top, null);

        recycleBitmap(srcBitmap);
        recycleBitmap(logoBitmap);

        return bitmap;
    }

    private static void drawPaddingWhiteAndStroke(QRCodeParams.LogoInfo logoInfo, Canvas canvas,
                                                  int left, int top, int logoWidth, int logoHeight) {
        if (logoInfo == null) return;
        if (logoInfo.getPaddingWhite() > 0) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.WHITE);
            RectF whiteRect = new RectF(left - logoInfo.getPaddingWhite(),
                    top - logoInfo.getPaddingWhite(),
                    left + logoWidth + logoInfo.getPaddingWhite(),
                    top + logoHeight + logoInfo.getPaddingWhite());
            canvas.save();
            if (logoInfo.getRound() > 0) {
                canvas.drawRoundRect(whiteRect, logoInfo.getRound(), logoInfo.getRound(), paint);
            } else {
                canvas.drawRect(whiteRect, paint);
            }

            if (logoInfo.getStrokeColor() != -1) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(logoInfo.getStrokeColor());
                paint.setStrokeWidth(1);
                RectF strokeRect = new RectF(left - logoInfo.getPaddingWhite() * 0.5f,
                        top - logoInfo.getPaddingWhite() * 0.5f,
                        left + logoWidth + logoInfo.getPaddingWhite() * 0.5f,
                        top + logoHeight + logoInfo.getPaddingWhite() * 0.5f);
                if (logoInfo.getRound() > 0) {
                    canvas.drawRoundRect(strokeRect, logoInfo.getRound(), logoInfo.getRound(), paint);
                } else {
                    canvas.drawRect(strokeRect, paint);
                }
            }
            canvas.restore();
        }
    }

    private static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
