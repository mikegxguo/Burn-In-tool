package bom.mitac.bist.burnin.util;

import java.io.BufferedInputStream;

import bom.mitac.bist.burnin.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class Element {
    private float mX;
    private float mY;

    private Bitmap mBitmap;

    public Element(Resources res, int x, int y) {
        mBitmap = BitmapFactory.decodeStream(new BufferedInputStream(res.openRawResource(R.drawable.ic_launcher)));
        mX = x - mBitmap.getWidth() / 2;
        mY = y - mBitmap.getHeight() / 2;
    }

    public void doDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, mX, mY, null);
    }
}
