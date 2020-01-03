package com.entage.nrd.entage.utilities_1;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Handler;
import android.util.Pair;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ViewPathAnimator
{
    public static final int DEFAULT_DELAY     = 1000 / 10;
    public static final int DEFAULT_FRAMESKIP = 3;

    private static Handler                        handler;
    private static HashMap<Integer, PathRunnable> animatedViews;

    public static void animate(View view, Path path)
    {
        animate(view, path, DEFAULT_DELAY, DEFAULT_FRAMESKIP);
    }

    public static void animate(View view, Path path, int delay)
    {
        animate(view, path, delay, DEFAULT_FRAMESKIP);
    }

    public static void animate(View view, Path path, int delay, int frameSkip)
    {
        if (animatedViews == null)
        {
            animatedViews = new HashMap<>();
        }

        if (handler == null)
        {
            handler = new Handler();
        }

        if (animatedViews.containsKey(view.hashCode()))
        {
            cancel(view);
        }

        PathRunnable runnable = new PathRunnable(view, path, delay, frameSkip);
        animatedViews.put(view.hashCode(), runnable);
        handler.postDelayed(runnable, delay);
    }

    public static void cancel(View view)
    {
        if (animatedViews != null && handler != null)
        {
            PathRunnable task = animatedViews.get(view.hashCode());
            if (task != null)
            {
                handler.removeCallbacks(task);
                animatedViews.remove(view.hashCode());
            }
        }
    }

    private static class PathRunnable implements Runnable
    {
        private WeakReference<View> view;
        Pair<Float, Float>[] points;
        private int delay;
        private int frameSkip;
        private int frame;

        PathRunnable(View view, Path path, int delay, int frameSkip)
        {
            this.view = new WeakReference<>(view);
            this.points = getPoints(path);
            this.delay = delay;
            this.frameSkip = Math.max(frameSkip, 0);
            this.frame = 0;
        }

        @Override
        public void run()
        {
            frame = (frame + frameSkip + 1) % points.length;
            Pair<Float, Float> pair = points[frame];

            View v = view.get();
            if (v != null)
            {
                v.setTranslationX(pair.first);
                v.setTranslationY(pair.second);

                handler.postDelayed(this, delay);
            }
        }

        // https://stackoverflow.com/questions/7972780/how-do-i-find-all-the-points-in-a-path-in-android
        private Pair<Float, Float>[] getPoints(Path path)
        {
            PathMeasure pathMeasure = new PathMeasure(path, true);
            int         frames      = (int) pathMeasure.getLength();

            Pair<Float, Float>[] pointArray   = new Pair[frames];
            float                length       = pathMeasure.getLength();
            float                distance     = 0f;
            float                speed        = length / pointArray.length;
            int                  counter      = 0;
            float[]              aCoordinates = new float[2];

            while ((distance < length) && (counter < pointArray.length))
            {
                // get point from the path
                pathMeasure.getPosTan(distance, aCoordinates, null);
                pointArray[counter] = new Pair<>(aCoordinates[0], aCoordinates[1]);
                counter++;
                distance = distance + speed;
            }

            return pointArray;
        }
    }
}