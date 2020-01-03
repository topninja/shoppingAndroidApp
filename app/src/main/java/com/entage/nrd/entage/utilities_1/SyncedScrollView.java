package com.entage.nrd.entage.utilities_1;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class SyncedScrollView extends ScrollView implements ScrollNotifier {

    private ScrollListener scrollListener = null;

    public SyncedScrollView(Context context) {
        super(context);
    }

    public SyncedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SyncedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (scrollListener != null)
            scrollListener.onScrollChanged(this, l, t, oldl, oldt);
    }
    @Override
    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }
    @Override
    public ScrollListener getScrollListener() {
        return scrollListener;
    }


}