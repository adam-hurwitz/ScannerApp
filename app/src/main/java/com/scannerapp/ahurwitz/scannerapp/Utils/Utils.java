package com.scannerapp.ahurwitz.scannerapp.Utils;

/**
 * Created by Ezequiel Adrian on 24/02/2017.
 */

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.images.Size;

import java.net.MalformedURLException;
import java.net.URL;


public class Utils {

    private static int displayWidth;
    private static int displayHeight;

    public static int getScreenHeight(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getScreenWidth(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float getScreenRatio(Context c) {
        DisplayMetrics metrics = c.getResources().getDisplayMetrics();
        return ((float)metrics.heightPixels / (float)metrics.widthPixels);
    }

    public static int getScreenRotation(Context c) {
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getRotation();
    }

    public static int distancePointsF(PointF p1, PointF p2) {
        return (int) Math.sqrt((p1.x - p2.x) *  (p1.x - p2.x) + (p1.y - p2.y) *  (p1.y - p2.y));
    }

    public static PointF middlePoint(PointF p1, PointF p2) {
        if(p1 == null || p2 == null)
            return null;
        return new PointF((p1.x+p2.x)/2, (p1.y+p2.y)/2);
    }

    public static Size[] sizeToSize(android.util.Size[] sizes) {
        Size[] size = new Size[sizes.length];
        for(int i=0; i<sizes.length; i++) {
            size[i] = new Size(sizes[i].getWidth(), sizes[i].getHeight());
        }
        return size;
    }


    public static int getDisplayWidth(Context context) {
        if (displayWidth == 0) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            displayWidth = metrics.widthPixels;
        }
        return displayWidth;
    }
    public static int getDisplayHeight(Context context) {
        if (displayHeight == 0) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            displayHeight = metrics.heightPixels;
        }
        return displayHeight;
    }
    public static int getStatusBarHeight(Context context) {
        int statusBar = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBar = (int) context.getResources().getDimension(resourceId);
        }
        return statusBar;
    }

    public static boolean isValidURL(String urlStr) {
        if (urlStr != null && !urlStr.isEmpty()) {
            try {
                URL url = new URL(urlStr);
                return true;
            } catch (MalformedURLException e) {
                return false;
            }
        } else {
            return false;
        }
    }


}
