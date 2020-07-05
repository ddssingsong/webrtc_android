package com.dds.core.voip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;

import com.dds.webrtc.R;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by dds on 2020/7/5.
 * ddssingsong@163.com
 */
public class NineGridView extends GridLayout {
    private Map<String, View> map = new ConcurrentHashMap<>();

    public NineGridView(Context context) {
        this(context, null);
    }

    public NineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setColumnCount(3);
        setRowCount(3);
    }

    public void addView(String userId, View view) {
        map.put(userId, view);
        resetView();
    }


    public void removeView(String userId) {
        map.remove(userId);
        resetView();
    }

    private void resetView() {
        this.removeAllViews();
        int i = 0;
        Iterator<Map.Entry<String, View>> inter = map.entrySet().iterator();
        while (inter.hasNext()) {
            Map.Entry<String, View> entry = inter.next();
            String key = entry.getKey();
            View view = map.get(key);
            GridLayout.Spec rowSpec = GridLayout.spec(i / 3, 1f);
            GridLayout.Spec columnSpec = GridLayout.spec(i % 3, 1f);
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
            layoutParams.height = 0;
            layoutParams.width = 0;
            if (i / 3 == 0)
                layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dp_2);
            if (i % 3 == 1) {
                layoutParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.dp_2);
                layoutParams.rightMargin = getResources().getDimensionPixelSize(R.dimen.dp_2);
            }
            this.addView(view, layoutParams);
            i++;
        }


    }


}
