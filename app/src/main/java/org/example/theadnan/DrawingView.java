package org.example.theadnan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawingView extends View {
    private Path drawPath;
    private Paint drawPaint, canvasPaint;
    private int paintColor = Color.BLACK;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean erase = false;
    private Bitmap pendingBitmap;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(10);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    private void initCanvas(int w, int h) {
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
        drawCanvas.drawColor(Color.WHITE);
        if (pendingBitmap != null) {
            Bitmap scaled = Bitmap.createScaledBitmap(pendingBitmap, w, h, true);
            drawCanvas.drawBitmap(scaled, 0, 0, canvasPaint);
            pendingBitmap = null;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            initCanvas(w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (drawCanvas == null) return true;
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void clear() {
        if (drawCanvas != null) {
            drawCanvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC);
            invalidate();
        }
        pendingBitmap = null;
    }

    public void setColor(int newColor) {
        paintColor = newColor;
        drawPaint.setColor(paintColor);
        drawPaint.setXfermode(null);
        erase = false;
        invalidate();
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) {
            drawPaint.setColor(Color.WHITE);
            drawPaint.setStrokeWidth(30);
        } else {
            drawPaint.setColor(paintColor);
            drawPaint.setStrokeWidth(10);
        }
    }

    public Bitmap getBitmap() {
        return canvasBitmap;
    }
    
    public void setBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        if (getWidth() > 0 && getHeight() > 0) {
            if (canvasBitmap == null) {
                initCanvas(getWidth(), getHeight());
            }
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
            drawCanvas.drawBitmap(scaled, 0, 0, canvasPaint);
            invalidate();
        } else {
            pendingBitmap = bitmap;
        }
    }
}
