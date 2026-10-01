package org.schabi.newpipe.hush.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import org.json.*;
import java.io.*;
import java.util.*;

/** Draws the unmodified imagegen PNG alpha within a uniform glyph slot. */
public final class HushIcons {
    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, String> VIEWS = new HashMap<>();
    private static final Map<String, Rect> BOUNDS = new HashMap<>();
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(12 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount(); }
    };
    static {
        VIEWS.put("button_download","download");
        VIEWS.put("switchCommentsVisibility","comments");
        VIEWS.put("playWithKodi","cast");
        VIEWS.put("sleep_timer","sleep-timer");
        VIEWS.put("repeatButton","repeat");
        VIEWS.put("home_brand","hush-mark");
        VIEWS.put("profile_sheet_close","close");
        VIEWS.put("detail_controls_playlist_append","bookmark");
        VIEWS.put("detail_controls_background","headphones");
        VIEWS.put("detail_controls_download","download");
        VIEWS.put("playlist_ctrl_play_all_button","play");
        VIEWS.put("playlist_ctrl_play_bg_button","headphones");
        VIEWS.put("playlist_ctrl_play_popup_button","pip");
        VIEWS.put("playlist_row_icon","playlist");
        VIEWS.put("playlist_row_chevron","chevron-right");
        ALIASES.put("ic_replay","retry");
        ALIASES.put("exo_controls_repeat_off","repeat");
        ALIASES.put("exo_controls_repeat_one","repeat-one");
        ALIASES.put("exo_controls_repeat_all","repeat");
        ALIASES.put("ic_timer","sleep-timer");
        ALIASES.put("ic_timer_off","sleep-timer");
        ALIASES.put("ic_bullet_comment_enabled","comments");
        ALIASES.put("ic_bullet_comment_disabled","comments-off");
        ALIASES.put("ic_file_download","download");
        ALIASES.put("ic_picture_in_picture","pip");
        ALIASES.put("ic_skip_next","next");
        ALIASES.put("ic_skip_previous","previous");
        ALIASES.put("ic_subtitles","captions");
        ALIASES.put("ic_cast","cast");
        ALIASES.put("ic_brightness_high","brightness");
        ALIASES.put("ic_notifications","notifications");
        ALIASES.put("ic_hush_mark","hush-mark");
        ALIASES.put("ic_hush_mark_trim","hush-mark");
        ALIASES.put("ic_hush_search","search");
        ALIASES.put("ic_search","search");
        ALIASES.put("ic_hush_games","games");
        ALIASES.put("ic_hush_incognito","incognito");
        ALIASES.put("ic_more_vert","more-vertical");
        ALIASES.put("ic_more_horiz","more-horizontal");
        ALIASES.put("ic_hush_history","history");
        ALIASES.put("ic_history","history");
        ALIASES.put("ic_hush_saved","bookmark");
        ALIASES.put("ic_bookmark","bookmark");
        ALIASES.put("ic_hush_download","download");
        ALIASES.put("ic_download","download");
        ALIASES.put("ic_hush_back","back");
        ALIASES.put("ic_arrow_back","back");
        ALIASES.put("ic_hush_chevron","chevron-right");
        ALIASES.put("ic_expand_more","chevron-down");
        ALIASES.put("ic_hush_close","close");
        ALIASES.put("ic_close","close");
        ALIASES.put("ic_hush_play","play");
        ALIASES.put("ic_play_arrow","play");
        ALIASES.put("ic_hush_pause","pause");
        ALIASES.put("ic_pause","pause");
        ALIASES.put("ic_fullscreen","expand");
        ALIASES.put("ic_fullscreen_exit","fullscreen-exit");
        ALIASES.put("ic_headset","headphones");
        ALIASES.put("ic_art_track","queue");
        ALIASES.put("ic_description","info");
        ALIASES.put("ic_playlist_play","playlist");
        ALIASES.put("ic_add","add");
        ALIASES.put("ic_hush_check","check");
        ALIASES.put("ic_check","check");
        ALIASES.put("ic_hush_profile","profile");
        ALIASES.put("ic_drag_handle","drag");
        ALIASES.put("ic_delete","trash");
        ALIASES.put("ic_edit","edit");
        ALIASES.put("ic_hush_restart","retry");
        ALIASES.put("ic_refresh","retry");
        ALIASES.put("ic_hush_sound","speaker");
        ALIASES.put("ic_volume_up","speaker");
        ALIASES.put("ic_hush_mute","mute");
        ALIASES.put("ic_volume_off","mute");
        ALIASES.put("ic_settings","settings");
        ALIASES.put("ic_backup","backup");
        ALIASES.put("ic_view_list","list");
        ALIASES.put("ic_grid_view","grid");
        ALIASES.put("ic_share","share");
        ALIASES.put("ic_shuffle","shuffle");
        ALIASES.put("ic_repeat","repeat");
        ALIASES.put("ic_repeat_one","repeat-one");
        ALIASES.put("ic_stop","stop");
        ALIASES.put("ic_info","info");
        ALIASES.put("ic_folder","folder");
        ALIASES.put("ic_hush_music","music");
        ALIASES.put("ic_hush_undo","undo");
        ALIASES.put("ic_hush_erase","erase");
        ALIASES.put("ic_hush_hint","hint");
        ALIASES.put("ic_hush_notes","notes");
        ALIASES.put("ic_hush_swap","swap");
        ALIASES.put("ic_hush_notification","notifications");
        VIEWS.put("control_play_pause","pause");
        VIEWS.put("control_repeat","repeat");
        VIEWS.put("control_shuffle","shuffle");
        VIEWS.put("itemHandle","drag");
        VIEWS.put("item_more","more-vertical");
        VIEWS.put("closeButton","close");
        VIEWS.put("handle","drag");
        VIEWS.put("toolbar_search_submit_icon","search");
        VIEWS.put("toolbar_search_clear_icon","close");
        VIEWS.put("backButton","back");
        VIEWS.put("refreshIcon","retry");
        VIEWS.put("profile_row_check","check");
        VIEWS.put("delete_button","trash");
        VIEWS.put("icon","add");
        VIEWS.put("profile_expand","chevron-down");
        VIEWS.put("home_menu","more-vertical");
        VIEWS.put("home_search_leading","search");
        VIEWS.put("home_view_toggle","list");
        VIEWS.put("home_search_clear","close");
        VIEWS.put("home_incognito","incognito");
        VIEWS.put("now_playing_toggle","pause");
        VIEWS.put("now_playing_close","close");
        VIEWS.put("wo_sound_icon","mute");
        VIEWS.put("button_close","close");
        VIEWS.put("button_share","share");
        VIEWS.put("playIcon","playlist");
        VIEWS.put("channel_add_to_group_button","add");
        VIEWS.put("detail_toggle_secondary_controls_view","chevron-down");
        VIEWS.put("detail_controls_overflow","more-vertical");
        VIEWS.put("overlay_play_pause_button","play");
        VIEWS.put("overlay_close_button","close");
        VIEWS.put("addInstanceButton","add");
        VIEWS.put("addTabsButton","add");
        VIEWS.put("fab_add_filter","add");
        VIEWS.put("toolbar_search_clear_feed","close");
        VIEWS.put("playerCloseButton","close");
        VIEWS.put("moreOptionsButton","more-vertical");
        VIEWS.put("share","share");
        VIEWS.put("switchMute","mute");
        VIEWS.put("fullScreenButton","expand");
        VIEWS.put("screenRotationButton","expand");
        VIEWS.put("playPauseButton","pause");
        VIEWS.put("shuffleButton","shuffle");
        VIEWS.put("itemsListClose","close");
        VIEWS.put("import_export_expand_icon","chevron-down");
        VIEWS.put("pitchToogleControlModes","chevron-down");
    }
    private HushIcons() { }
    public static Drawable drawable(Context context, int resource) {
        String id = ALIASES.get(context.getResources().getResourceEntryName(resource));
        return id == null ? ContextCompat.getDrawable(context, resource) : drawable(context, id);
    }
    public static synchronized Drawable drawable(Context context, String id) {
        try {
            if (BOUNDS.isEmpty()) {
                try (InputStream in = context.getAssets().open("hush-icons/metrics.json")) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] buffer = new byte[4096];
                    int n; while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                    JSONArray all = new JSONArray(out.toString("UTF-8"));
                    for (int i=0; i<all.length(); i++) {
                        JSONObject item=all.getJSONObject(i); JSONArray b=item.getJSONArray("bounds");
                        BOUNDS.put(item.getString("id"), new Rect(b.getInt(0)/4,b.getInt(1)/4,
                                (b.getInt(2)+3)/4,(b.getInt(3)+3)/4));
                    }
                }
            }
            Bitmap bitmap=CACHE.get(id);
            if(bitmap==null) {
                BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=4;
                try(InputStream in=context.getAssets().open("hush-icons/"+id+".png")) {
                    bitmap=BitmapFactory.decodeStream(in,null,options);
                }
                if(bitmap==null) throw new IOException("Invalid glyph: "+id);
                CACHE.put(id,bitmap);
            }
            Glyph result=new Glyph(bitmap,BOUNDS.get(id),HushUi.dp(context,24),
                    id.equals("back") || id.equals("chevron-right"));
            result.setTint(HushUi.color(context,com.google.android.material.R.attr.colorOnSurface));
            return result;
        } catch(IOException | JSONException e) { throw new IllegalStateException("Missing generated icon: "+id,e); }
    }
    public static void apply(View root) {
        if(root.getId()!=View.NO_ID) {
            String id=VIEWS.get(root.getResources().getResourceEntryName(root.getId()));
            if(id!=null) {
                if(root instanceof ImageView) {
                    ((ImageView)root).setImageDrawable(drawable(root.getContext(),id));
                    String name=root.getResources().getResourceEntryName(root.getId());
                    if(name.equals("item_more") || name.equals("itemHandle")
                            || root instanceof android.widget.ImageButton && root.getPaddingLeft()==0) {
                        int padding=HushUi.dp(root.getContext(),12);root.setPadding(padding,padding,padding,padding);
                    }
                }
                else if(root instanceof com.google.android.material.button.MaterialButton)
                    ((com.google.android.material.button.MaterialButton)root).setIcon(drawable(root.getContext(),id));
            }
        }
        if(root instanceof ViewGroup) for(int i=0;i<((ViewGroup)root).getChildCount();i++)
            apply(((ViewGroup)root).getChildAt(i));
    }
    private static final class Glyph extends Drawable {
        private final Bitmap bitmap; private final Rect source; private final int size;
        private final boolean directional; private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private ColorStateList tint;
        Glyph(Bitmap bitmap,Rect source,int size,boolean directional) {
            this.bitmap=bitmap;this.source=source;this.size=size;this.directional=directional;
        }
        @Override public void draw(@NonNull Canvas canvas) {
            Rect b=getBounds();float scale=Math.min(b.width()/(float)source.width(),b.height()/(float)source.height());
            float w=source.width()*scale,h=source.height()*scale;
            RectF dest=new RectF(b.exactCenterX()-w/2,b.exactCenterY()-h/2,b.exactCenterX()+w/2,b.exactCenterY()+h/2);
            int save=canvas.save();if(directional && getLayoutDirection()==View.LAYOUT_DIRECTION_RTL)
                canvas.scale(-1,1,b.exactCenterX(),b.exactCenterY());
            canvas.drawBitmap(bitmap,source,dest,paint);canvas.restoreToCount(save);
        }
        @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(@Nullable ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
        @Override public void setTint(int color){setTintList(ColorStateList.valueOf(color));}
        @Override public void setTintList(@Nullable ColorStateList colors){tint=colors;onStateChange(getState());}
        @Override protected boolean onStateChange(int[] state){if(tint!=null)setColorFilter(new PorterDuffColorFilter(tint.getColorForState(state,tint.getDefaultColor()),PorterDuff.Mode.SRC_IN));return true;}
        @Override public boolean isStateful(){return tint!=null && tint.isStateful();}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
        @Override public int getIntrinsicWidth(){return size;}
        @Override public int getIntrinsicHeight(){return size;}
    }
}
