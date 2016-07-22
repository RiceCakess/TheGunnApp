package xyz.dchen.thegunnapp;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by David on 7/21/2016.
 */
public class WrappedList extends ListView {

        public WrappedList (Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public WrappedList  (Context context) {
            super(context);
        }

        public WrappedList  (Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        }
}
