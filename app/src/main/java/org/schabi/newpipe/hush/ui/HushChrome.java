package org.schabi.newpipe.hush.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.PlayerService;
import org.schabi.newpipe.player.helper.PlayerHolder;

/** Activity-owned navigation and in-app video host. Never owns a player or decoder. */
public final class HushChrome {
    private final MainActivity activity;
    private final ViewGroup parent;
    private final LinearLayout navigation;
    private final FrameLayout floating;
    private final FrameLayout video;
    private final ImageButton toggle, fallbackToggle;
    private final LinearLayout fallback;
    private final TextView fallbackTitle;
    private final Runnable observer;
    private String requested;
    private boolean expanded, rail, suppressed;
    private int top, bottom, left, right, keyboard;
    private float cornerX=1, cornerY=1;
    private View videoRoot;
    private Runnable expandAction, closeAction;
    public HushChrome(MainActivity activity, ViewGroup parent) {
        this.activity=activity;this.parent=parent;
        navigation=new LinearLayout(activity);navigation.setGravity(Gravity.CENTER_VERTICAL);
        navigation.setPadding(dp(8),dp(6),dp(8),dp(6));
        navigation.setBackground(HushUi.shape(activity,color(com.google.android.material.R.attr.colorSurfaceContainer),24,
                color(com.google.android.material.R.attr.colorOutlineVariant)));
        String[] names={"Search","Games","Breathe"}, icons={"search","games","hush-mark"};
        int[] labels={R.string.search,R.string.hush_nav_games,R.string.hush_breathe};
        int[] ids={R.id.hush_nav_search,R.id.hush_nav_games,R.id.hush_nav_breathe};
        for(int i=0;i<3;i++) {
            String destination=names[i];LinearLayout tab=new LinearLayout(activity);tab.setId(ids[i]);tab.setOrientation(LinearLayout.VERTICAL);
            tab.setGravity(Gravity.CENTER);tab.setMinimumHeight(dp(60));
            ImageView glyph=new ImageView(activity);glyph.setImageDrawable(HushIcons.drawable(activity,icons[i]));
            glyph.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            tab.addView(glyph,new LinearLayout.LayoutParams(dp(24),dp(24)));
            TextView label=new TextView(activity);label.setText(labels[i]);label.setTextSize(12);label.setGravity(Gravity.CENTER);label.setMaxLines(3);
            label.setTextColor(color(com.google.android.material.R.attr.colorOnSurface));tab.addView(label);
            tab.setContentDescription(activity.getString(labels[i]));tab.setTag(destination);
            HushUi.clickable(tab,0,18,0);tab.setOnClickListener(v->activity.openHushDestination(destination));
            navigation.addView(tab,new LinearLayout.LayoutParams(0,-2,1));
        }
        navigation.setVisibility(View.GONE);
        parent.addView(navigation,new CoordinatorLayout.LayoutParams(-1,-2));
        navigation.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)-> {
            if (b-t != ob-ot) activity.applySearchChromeInsets();
        });
        floating=new FrameLayout(activity);floating.setId(R.id.hush_floating_video);floating.setVisibility(View.GONE);floating.setElevation(dp(8));
        floating.setBackground(HushUi.shape(activity,color(com.google.android.material.R.attr.colorSurfaceContainer),16,
                color(com.google.android.material.R.attr.colorOutlineVariant)));
        floating.setClipToOutline(true);
        video=new FrameLayout(activity);floating.addView(video,new FrameLayout.LayoutParams(-1,-1));
        View drag=new View(activity);floating.addView(drag,new FrameLayout.LayoutParams(-1,-1));
        drag.setOnTouchListener(new View.OnTouchListener(){
            float x,y,originX,originY;boolean moved;
            @Override public boolean onTouch(View v,android.view.MotionEvent event){
                if(event.getActionMasked()==android.view.MotionEvent.ACTION_DOWN){
                    x=event.getRawX();y=event.getRawY();originX=floating.getX();originY=floating.getY();moved=false;return true;
                }
                if(event.getActionMasked()==android.view.MotionEvent.ACTION_MOVE){
                    float dx=event.getRawX()-x,dy=event.getRawY()-y;
                    moved|=Math.abs(dx)+Math.abs(dy)>dp(8);
                    FloatingBounds safe=bounds();
                    floating.setX(safe.x(originX+dx));
                    floating.setY(protectsControls() ? safe.maxY : safe.y(originY+dy));return true;
                }
                if(event.getActionMasked()==android.view.MotionEvent.ACTION_UP || event.getActionMasked()==android.view.MotionEvent.ACTION_CANCEL){
                    cornerX=floating.getX()+floating.getWidth()/2f>parent.getWidth()/2f?1:0;
                    cornerY=floating.getY()+floating.getHeight()/2f>parent.getHeight()/2f?1:0;
                    if(!moved && event.getActionMasked()==android.view.MotionEvent.ACTION_UP)v.performClick();
                    position();return true;
                }return false;
            }
        });
        ImageButton expand=control("expand",R.string.expand);expand.setId(R.id.hush_floating_expand);expand.setOnClickListener(v->{if(expandAction!=null)expandAction.run();});
        floating.addView(expand,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.START));
        ImageButton close=control("close",R.string.close);close.setId(R.id.hush_floating_close);close.setOnClickListener(v->{if(closeAction!=null)closeAction.run();});
        floating.addView(close,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.TOP|Gravity.END));
        toggle=control("pause",R.string.pause);toggle.setOnClickListener(v->activity.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE)));
        floating.addView(toggle,new FrameLayout.LayoutParams(dp(48),dp(48),Gravity.CENTER));
        parent.addView(floating,new CoordinatorLayout.LayoutParams(dp(200),dp(113)));
        fallback=new LinearLayout(activity);fallback.setId(R.id.hush_floating_audio_fallback);
        fallback.setGravity(Gravity.CENTER_VERTICAL);fallback.setMinimumHeight(dp(64));fallback.setPadding(dp(12),dp(8),dp(8),dp(8));
        fallback.setBackground(HushUi.shape(activity,color(com.google.android.material.R.attr.colorSurfaceContainer),16,color(com.google.android.material.R.attr.colorOutlineVariant)));
        fallbackTitle=new TextView(activity);fallbackTitle.setTextColor(color(com.google.android.material.R.attr.colorOnSurface));
        fallbackTitle.setTextSize(14);fallbackTitle.setMaxLines(2);fallbackTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        fallback.addView(fallbackTitle,new LinearLayout.LayoutParams(0,-2,1));
        fallbackToggle=control("pause",R.string.pause);fallbackToggle.setImageTintList(ColorStateList.valueOf(color(com.google.android.material.R.attr.colorOnSurface)));
        fallbackToggle.setOnClickListener(v->activity.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE)));
        ImageButton restore=control("expand",R.string.expand), stop=control("close",R.string.close);
        restore.setImageTintList(ColorStateList.valueOf(color(com.google.android.material.R.attr.colorOnSurface)));stop.setImageTintList(restore.getImageTintList());
        restore.setOnClickListener(v->{if(expandAction!=null)expandAction.run();});stop.setOnClickListener(v->{if(closeAction!=null)closeAction.run();});
        for(ImageButton action:new ImageButton[]{fallbackToggle,restore,stop})HushUi.clickable(action,color(com.google.android.material.R.attr.colorSurfaceContainer),24,0);
        fallback.addView(fallbackToggle,new LinearLayout.LayoutParams(dp(48),dp(48)));fallback.addView(restore,new LinearLayout.LayoutParams(dp(48),dp(48)));fallback.addView(stop,new LinearLayout.LayoutParams(dp(48),dp(48)));
        fallback.setVisibility(View.GONE);fallback.setElevation(dp(8));parent.addView(fallback,new CoordinatorLayout.LayoutParams(-1,-2));
        observer=()->floating.post(this::refreshPlayback);PlayerHolder.getInstance().addUiObserver(observer);
        parent.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            if(r-l != or-ol || b-t != ob-ot){ render(); activity.applySearchChromeInsets(); }
        });
    }
    private ImageButton control(String icon,int description){
        ImageButton button=new ImageButton(activity);button.setImageDrawable(HushIcons.drawable(activity,icon));
        button.setPadding(dp(12),dp(12),dp(12),dp(12));button.setContentDescription(activity.getString(description));
        button.setImageTintList(ColorStateList.valueOf(0xFFEDF3EC));
        HushUi.clickable(button,0x990F1713,24,0);return button;
    }
    public void navigation(String page){requested=page;render();}
    public void expanded(boolean value){expanded=value;render();}
    public int navigationWidth(){return rail && navigation.getVisibility()==View.VISIBLE ? dp(136):0;}
    public boolean isRail(){return rail;}
    public int navigationHeight(){return !rail && navigation.getVisibility()==View.VISIBLE ? Math.max(dp(72),navigation.getMeasuredHeight())+dp(12):0;}
    public void insets(int top,int bottom,int left,int right,int keyboard){
        this.top=top;this.bottom=bottom;this.left=left;this.right=right;this.keyboard=keyboard;render();
    }
    private void render(){
        float density=activity.getResources().getDisplayMetrics().density;
        int available=parent.getWidth()-left-right;
        boolean nextRail=TabletLayout.rail(available/density,
                (parent.getHeight()-top-bottom)/density);
        rail=nextRail;
        navigation.setOrientation(rail?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        navigation.setVisibility(!expanded
                && (requested!=null || rail) && (keyboard==0 || rail)?View.VISIBLE:View.GONE);
        CoordinatorLayout.LayoutParams p=(CoordinatorLayout.LayoutParams)navigation.getLayoutParams();
        int side=Math.max(dp(TabletLayout.gutter(available/density)),(available-dp(560))/2);
        p.width=rail?dp(96):Math.max(0,available-2*side);
        p.height=ViewGroup.LayoutParams.WRAP_CONTENT;
        p.gravity=rail?Gravity.START|Gravity.TOP:Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        p.leftMargin=rail?left+dp(16):left;
        p.rightMargin=rail?right:right;
        p.topMargin=rail?top+dp(24):0;
        p.bottomMargin=bottom+dp(12);
        navigation.setLayoutParams(p);
        for(int i=0;i<navigation.getChildCount();i++){
            LinearLayout.LayoutParams tabParams=new LinearLayout.LayoutParams(
                    rail?-1:0,-2,rail?0:1);
            if(rail && i>0)tabParams.topMargin=dp(16);
            navigation.getChildAt(i).setMinimumHeight(dp(72));
            navigation.getChildAt(i).setLayoutParams(tabParams);
        }
        for(int i=0;i<navigation.getChildCount();i++){
            View tab=navigation.getChildAt(i);boolean selected=tab.getTag().equals(requested==null?"Search":requested);tab.setSelected(selected);
            tab.setBackground(HushUi.shape(activity,selected?color(com.google.android.material.R.attr.colorPrimaryContainer):0,18,0));
        }
        position();
    }
    public void showVideo(View root,Runnable expand,Runnable close){
        if(root.getParent() instanceof ViewGroup)((ViewGroup)root.getParent()).removeView(root);
        video.removeAllViews();video.addView(root,new FrameLayout.LayoutParams(-1,-1));videoRoot=root;
        expandAction=expand;closeAction=close;floating.setVisibility(View.VISIBLE);refreshPlayback();
        activity.applySearchChromeInsets();position();
    }
    public void hideVideo(){
        if(videoRoot!=null && videoRoot.getParent()==video)video.removeView(videoRoot);
        videoRoot=null;expandAction=null;closeAction=null;floating.setVisibility(View.GONE);fallback.setVisibility(View.GONE);
        activity.applySearchChromeInsets();
    }
    public boolean isFloating(){return videoRoot!=null;}
    public void suppressVideo(boolean value){suppressed=value;render();activity.applySearchChromeInsets();}
    private boolean protectsControls(){
        androidx.fragment.app.Fragment page=activity.getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        return page instanceof org.schabi.newpipe.fragments.list.search.BreakSessionFragment
                || page instanceof org.schabi.newpipe.hush.games.GamesFragment
                && !((org.schabi.newpipe.hush.games.GamesFragment)page).isHub();
    }
    private FloatingBounds bounds(){
        float width=(parent.getWidth()-left-right)/activity.getResources().getDisplayMetrics().density;
        return FloatingBounds.inWindow(parent.getWidth(),parent.getHeight(),left+navigationWidth(),right,
                top,bottom,keyboard,navigationHeight(),dp(TabletLayout.floatingWidth(width)),
                dp(TabletLayout.gutter(width)),dp(12));
    }
    private float maxY(){return bounds().maxY;}
    private void position(){
        if(videoRoot==null)return;
        FloatingBounds candidate=bounds();
        boolean tooSmall=candidate.width<dp(144) || keyboard>0 && candidate.height<dp(108);
        boolean hide=suppressed || expanded || tooSmall;
        int availableHeight=parent.getHeight()-top-bottom-keyboard-navigationHeight()-dp(24);
        fallback.setVisibility(!suppressed && !expanded && tooSmall && availableHeight>=dp(64)?View.VISIBLE:View.GONE);
        if(fallback.getVisibility()==View.VISIBLE){
            CoordinatorLayout.LayoutParams audio=(CoordinatorLayout.LayoutParams)fallback.getLayoutParams();
            int availableWidth=parent.getWidth()-left-right-navigationWidth();
            int gutter=dp(TabletLayout.gutter(availableWidth/activity.getResources().getDisplayMetrics().density));
            audio.width=Math.min(dp(560),Math.max(0,availableWidth-2*gutter));
            audio.gravity=Gravity.BOTTOM|Gravity.END;audio.rightMargin=right+gutter;
            audio.bottomMargin=bottom+keyboard+navigationHeight()+dp(12);fallback.setLayoutParams(audio);
        }
        floating.setVisibility(hide?View.INVISIBLE:View.VISIBLE);
        if(parent.getWidth()==0)return;
        FloatingBounds b=bounds();
        ViewGroup.LayoutParams p=floating.getLayoutParams();if(p.width!=b.width || p.height!=b.height){p.width=b.width;p.height=b.height;floating.setLayoutParams(p);}
        floating.setX(cornerX==1?b.maxX:b.minX);
        floating.setY(protectsControls() || cornerY==1?b.maxY:b.minY);
    }
    private void refreshPlayback(){
        PlayerHolder holder=PlayerHolder.getInstance();boolean playing=holder.isPlaying();
        for(ImageButton button:new ImageButton[]{toggle,fallbackToggle}){button.setImageDrawable(HushIcons.drawable(activity,playing?"pause":"play"));button.setContentDescription(activity.getString(playing?R.string.pause:R.string.play_audio));}
        org.schabi.newpipe.player.playqueue.PlayQueue queue=holder.getPlayQueue();
        fallbackTitle.setText(queue!=null && queue.getItem()!=null?queue.getItem().getTitle():activity.getString(R.string.hush_playing_background));
    }
    public void savePosition(android.os.Bundle state){state.putFloat("hush-floating-x",cornerX);state.putFloat("hush-floating-y",cornerY);}
    public void restorePosition(android.os.Bundle state){if(state!=null){cornerX=state.getFloat("hush-floating-x",1);cornerY=state.getFloat("hush-floating-y",1);}}
    public void dispose(){PlayerHolder.getInstance().removeUiObserver(observer);floating.removeCallbacks(observer);hideVideo();}
    private int dp(int v){return HushUi.dp(activity,v);}
    private int color(int attr){return HushUi.color(activity,attr);}
}
