package org.schabi.newpipe.hush.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/** Independently scrolling fragment hosts; existing fragments survive window resizing. */
public final class SettingsPanes extends LinearLayout {
    public interface ModeChanged { void changed(boolean expanded); }
    private ModeChanged listener;
    private Boolean expanded;
    private boolean hasDetail=true, compactDetail;
    public void setCompactDetail(boolean value){compactDetail=value;expanded=null;requestLayout();}
    public void setHasDetail(boolean value){if(hasDetail!=value){hasDetail=value;expanded=null;requestLayout();}}
    public SettingsPanes(Context context) { this(context,null); }
    public SettingsPanes(Context context, AttributeSet attributes) { super(context,attributes); }
    public void setOnModeChanged(ModeChanged value){listener=value;requestLayout();}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int width=MeasureSpec.getSize(widthSpec);
        float density=getResources().getDisplayMetrics().density;
        boolean wide=width/density>=840 && getResources().getConfiguration().fontScale<1.5f;
        int gutter=HushUi.dp(getContext(),TabletLayout.gutter(width/density));
        setPadding(gutter,0,gutter,0);
        if(getChildCount()==2 && (expanded==null || wide!=expanded)){
            expanded=wide;setOrientation(HORIZONTAL);
            View first=getChildAt(0), second=getChildAt(1);
            boolean settings=getId()==org.schabi.newpipe.R.id.settings_panes;
            first.setVisibility((settings||compactDetail)&&!wide?GONE:VISIBLE);
            first.setLayoutParams(new LayoutParams(wide && hasDetail?HushUi.dp(getContext(),settings?260:280):settings?0:-1,-1));
            second.setVisibility(settings || compactDetail || wide && hasDetail?VISIBLE:GONE);
            LayoutParams detail=new LayoutParams(0,-1,1);
            detail.setMarginStart(wide?HushUi.dp(getContext(),24):0);second.setLayoutParams(detail);
            if(listener!=null)post(()->listener.changed(wide));
        }
        super.onMeasure(widthSpec,heightSpec);
        if(getId()==org.schabi.newpipe.R.id.settings_panes && getChildCount()==2){
            View detail=getChildAt(1);int end=wide?Math.max(0,detail.getMeasuredWidth()-HushUi.dp(getContext(),640)):0;
            if(detail.getPaddingRight()!=end){detail.setPadding(0,0,end,0);super.onMeasure(widthSpec,heightSpec);}
        }
    }
}
