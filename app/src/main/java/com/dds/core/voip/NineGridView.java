package com.dds.core.voip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

/**
 * Created by dds on 2020/6/27.
 * 宫格视图，用来存放视频画面
 */
public class NineGridView extends TableLayout {

    private int rowNum = 0; // row number
    private int colNum = 0; // col number

    private BaseAdapter adapter = null;

    private Context context = null;

    public NineGridView(Context context) {
        super(context);
        initThis(context, null);
    }

    public NineGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initThis(context, attrs);
    }

    private void initThis(Context context, AttributeSet attrs) {
        this.context = context;
        if (this.getTag() != null) {
            String atb = (String) this.getTag();
            int ix = atb.indexOf(',');
            if (ix > 0) {
                rowNum = Integer.parseInt(atb.substring(0, ix));
                colNum = Integer.parseInt(atb.substring(ix + 1, atb.length()));
            }
        }
        if (rowNum <= 0)
            rowNum = 3;
        if (colNum <= 0)
            colNum = 3;

        if (this.isInEditMode()) {
            this.removeAllViews();
            for (int y = 0; y < rowNum; ++y) {
                TableRow row = new TableRow(context);
                row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1.0f));
                for (int x = 0; x < colNum; ++x) {
                    View button = new Button(context);
                    row.addView(button, new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
                }
                this.addView(row);
            }
        }
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BaseAdapter adapter) {
        if (adapter != null) {
            if (adapter.getCount() < this.rowNum * this.colNum) {
                throw new IllegalArgumentException("The view count of adapter is less than this gridview's items");
            }
            this.removeAllViews();
            for (int y = 0; y < rowNum; ++y) {
                TableRow row = new TableRow(context);
                row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
                for (int x = 0; x < colNum; ++x) {
                    View view = adapter.getView(y * colNum + x, this, row);
                    row.addView(view, new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
                }
                this.addView(row);
            }
        }
        this.adapter = adapter;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

}
