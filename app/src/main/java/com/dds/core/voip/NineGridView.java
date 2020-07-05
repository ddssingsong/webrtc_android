package com.dds.core.voip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;

import com.dds.webrtc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dds on 2020/7/5.
 * ddssingsong@163.com
 */
public class NineGridView extends GridLayout {
    private List<View> list = new ArrayList<>();

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

    public void addSurfaceView(View view) {
        int i = list.size() + 1;
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
        list.add(view);
    }

}
