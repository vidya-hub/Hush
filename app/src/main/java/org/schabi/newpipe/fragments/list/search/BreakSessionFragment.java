package org.schabi.newpipe.fragments.list.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.CompactPlaybackBar;
import org.schabi.newpipe.hush.ui.HushUi;

/** A full screen for an intentional pause, independent of video search. */
public final class BreakSessionFragment extends BaseFragment {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private BreakController state;
    private BreathingSound cues;
    private LinearLayout options;
    private TextView phase;
    private TextView clock;
    private BreathingOrbView orb;
    private MeditationRing ring;
    private MaterialButton primary;
    private MaterialButton secondary;
    private MaterialButton technique;
    private MaterialButton[] durations;
    private final MaterialButton[] modeButtons = new MaterialButton[2];
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (getView() == null) return;
            renderTick(); if (state.active && !state.paused) handler.postDelayed(this,80);
        }
    };
    public static BreakSessionFragment newInstance(boolean meditation) {
        BreakSessionFragment result = new BreakSessionFragment();
        Bundle args = new Bundle(); args.putBoolean("meditate",meditation); result.setArguments(args); return result;
    }
    @Override public void onCreate(@Nullable Bundle saved) {
        super.onCreate(saved); state = new ViewModelProvider(this).get(BreakController.class);
        if (!state.initialized) {
            state.initialized=true; state.meditation=getArguments()!=null && getArguments().getBoolean("meditate");
            SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(requireContext());
            try { state.technique=BreathingSession.Technique.valueOf(prefs.getString("hush_breath_technique","CALM")); }
            catch (RuntimeException ignored) { state.technique=BreathingSession.Technique.CALM; }
            state.minutes=prefs.getInt("hush_breath_duration",3);
            if (state.minutes!=1 && state.minutes!=3 && state.minutes!=5) state.minutes=3;
            state.sound=prefs.getBoolean("hush_breath_sound",true);
        }
    }
    @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup parent,@Nullable Bundle saved) {
        Context c=requireContext(); LinearLayout root=column(); root.setBackgroundColor(color(com.google.android.material.R.attr.colorSurface));
        LinearLayout header=new LinearLayout(c); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(22),0,dp(22),0);
        ImageButton back=icon(R.drawable.ic_hush_back,R.string.back); back.setOnClickListener(v -> exit());
        header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));back.setVisibility(View.GONE);
        TextView title=label(getString(state.meditation?R.string.hush_meditate:R.string.hush_breathe),25,true);
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        header.setMinimumHeight(dp(56));
        root.addView(header,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout modes=new LinearLayout(c);
        boolean stackModes=getResources().getConfiguration().fontScale>1.3f;
        modes.setOrientation(stackModes?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL);
        for(boolean meditation:new boolean[]{false,true}) {
            MaterialButton mode=button(meditation?R.string.hush_meditate:R.string.hush_breathe,false);
            modeButtons[meditation ? 1 : 0] = mode;
            LinearLayout.LayoutParams modeLayout = new LinearLayout.LayoutParams(stackModes?-1:0,-2,stackModes?0:1);
            if (meditation) { if(stackModes)modeLayout.topMargin=dp(8);else modeLayout.setMarginStart(dp(8)); }
            modes.addView(mode,modeLayout);
            mode.setOnClickListener(v->{if(state.active) return;state.meditation=meditation;
                title.setText(meditation?R.string.hush_meditate:R.string.hush_breathe);render();});
        }
        modes.setPadding(dp(22),dp(8),dp(22),dp(8));root.addView(modes);

        LinearLayout visual=column(); visual.setGravity(Gravity.CENTER);
        orb=new BreathingOrbView(c,null); visual.addView(orb,new LinearLayout.LayoutParams(-1,dp(160)));
        ring=new MeditationRing(c); visual.addView(ring,new LinearLayout.LayoutParams(-1,dp(160)));
        phase=label("",24,true); phase.setGravity(Gravity.CENTER); visual.addView(phase);
        clock=label("",17,false); clock.setGravity(Gravity.CENTER); visual.addView(clock,top(12));
        LinearLayout controls=column(); options=column();
        technique=button(R.string.breath_technique_calm,false); technique.setOnClickListener(v -> chooseTechnique());
        options.addView(technique,top(0));
        options.addView(label(getString(R.string.duration),16,true),top(24));
        LinearLayout durationRow=new LinearLayout(c); durations=new MaterialButton[3];
        boolean stackDurations = getResources().getConfiguration().fontScale > 1.3f;
        durationRow.setOrientation(stackDurations ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
        int[] values={1,3,5}; int[] labels={R.string.breath_duration_1,R.string.breath_duration_3,R.string.breath_duration_5};
        for (int i=0;i<3;i++) {
            int minutes=values[i]; MaterialButton choice=button(labels[i],false); durations[i]=choice;
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(stackDurations ? -1 : 0,-2,stackDurations ? 0 : 1);
            if(i>0) { if (stackDurations) lp.topMargin=dp(8); else lp.setMarginStart(dp(8)); }
            durationRow.addView(choice,lp); choice.setOnClickListener(v -> { state.minutes=minutes; saveOptions(); render(); });
        }
        options.addView(durationRow,top(12)); controls.addView(options);
        LinearLayout soundRow=new LinearLayout(c); soundRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView soundLabel=label(getString(R.string.hush_sound_cues),16,false);
        soundRow.addView(soundLabel,new LinearLayout.LayoutParams(0,-2,1));
        SwitchCompat sound=new SwitchCompat(c); sound.setChecked(state.sound); sound.setContentDescription(getString(R.string.hush_sound_cues));
        sound.setOnCheckedChangeListener((v,checked)->{state.sound=checked;saveOptions();if(!checked && cues!=null)cues.cancelPending();});
        soundRow.addView(sound,new LinearLayout.LayoutParams(dp(64),dp(56))); controls.addView(soundRow,top(20));
        primary=button(R.string.hush_start_breathing,true); controls.addView(primary,top(24));
        primary.setOnClickListener(v -> {
            if (!state.active) { state.start(SystemClock.elapsedRealtime()); if(state.meditation && state.sound)cue(BreathingSession.Phase.HOLD); }
            else if(state.paused)state.resume(SystemClock.elapsedRealtime());
            else {state.pause(SystemClock.elapsedRealtime());if(cues!=null)cues.cancelPending();}
            handler.removeCallbacks(tick); handler.post(tick);
        });
        secondary=button(R.string.breath_end,false); controls.addView(secondary,top(12)); secondary.setOnClickListener(v -> exit());
        LinearLayout content=column(); content.setPadding(dp(22),dp(12),dp(22),dp(24));
        HushUi.bindContentWidth(header,480,784); HushUi.bindContentWidth(modes,480,784); HushUi.bindContentWidth(content,480,784);
        HushUi.Panes panes=new HushUi.Panes(c,new HushUi.Bounded(c,visual,400),new HushUi.Bounded(c,controls,360));
        panes.setBreakpointDp(784); panes.setTag("hush-content-panes");content.addView(panes);
        ScrollView scroll=new ScrollView(c);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(new CompactPlaybackBar(c),new LinearLayout.LayoutParams(-1,-2)); return root;
    }
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle saved) {super.onViewCreated(view,saved);render();}
    @Override public void onResume() {
        super.onResume(); if(requireActivity() instanceof MainActivity){ ((MainActivity)requireActivity()).setSearchChrome(true); ((MainActivity)requireActivity()).setHomeNavigation("Breathe"); }
        handler.removeCallbacks(tick); handler.post(tick);
    }
    @Override public void onPause() {
        state.pause(SystemClock.elapsedRealtime()); handler.removeCallbacks(tick); if(cues!=null)cues.cancelPending(); super.onPause();
    }
    @Override public void onDestroyView() {
        handler.removeCallbacks(tick); if(cues!=null){cues.release();cues=null;} super.onDestroyView();
    }
    private void exit() {
        state.stop();handler.removeCallbacks(tick);if(cues!=null)cues.cancelPending();
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    /** Full refresh after a discrete state change (mode, start, pause, resume, finish). */
    private void render() {
        renderChrome();
        renderTick();
    }

    /** Structural updates only; safe to call on state changes, too heavy for the tick loop. */
    private void renderChrome() {
        if (primary == null) return;
        for (int i=0;i<modeButtons.length;i++) {
            HushUi.style(modeButtons[i], state.meditation == (i==1));
            modeButtons[i].setEnabled(!state.active);
        }
        orb.setVisibility(state.meditation ? View.GONE : View.VISIBLE);
        ring.setVisibility(state.meditation ? View.VISIBLE : View.GONE);
        options.setVisibility(state.active || state.completed ? View.GONE : View.VISIBLE);
        technique.setVisibility(state.meditation ? View.GONE : View.VISIBLE);
        technique.setText(techniqueTitle());
        for (int i = 0; i < 3; i++) {
            HushUi.style(durations[i], state.minutes == (i == 0 ? 1 : i == 1 ? 3 : 5));
        }
        primary.setText(state.completed ? R.string.hush_again : !state.active
                ? state.meditation ? R.string.hush_start_meditation
                : R.string.hush_start_breathing : state.paused
                ? R.string.breath_resume : R.string.breath_pause);
        secondary.setVisibility(state.active || state.completed ? View.VISIBLE : View.GONE);
        secondary.setText(state.completed ? R.string.hush_done : R.string.breath_end);
    }

    /** Light per-tick update; only touches views whose visible value actually changed. */
    private void renderTick() {
        if (primary == null) return;
        for (int i=0;i<modeButtons.length;i++) {
            HushUi.style(modeButtons[i], state.meditation == (i==1));
            modeButtons[i].setEnabled(!state.active);
        }
        long now = SystemClock.elapsedRealtime();
        long remaining = Math.max(0, state.minutes * 60_000L - state.elapsed(now));
        if (state.active && remaining == 0) {
            state.finish();
            if (state.sound) cue(BreathingSession.Phase.HOLD);
            renderChrome();
        }
        int seconds = (int) Math.ceil(remaining / 1000d);
        final CharSequence clockText = state.active
                ? getString(R.string.breath_time_remaining, seconds / 60, seconds % 60)
                : state.completed ? "" : getString(R.string.breath_duration_minutes, state.minutes);
        if (!clockText.equals(clock.getText())) clock.setText(clockText);

        final CharSequence phaseText;
        if (state.completed) {
            phaseText = getString(R.string.hush_session_done);
        } else if (!state.active) {
            phaseText = getString(state.meditation
                    ? R.string.hush_quiet_timer : techniqueTitle());
        } else if (state.meditation) {
            phaseText = getString(state.paused ? R.string.hush_paused : R.string.hush_quiet_timer);
        } else {
            BreathingSession.Snapshot snap = state.breathing.snapshot(now);
            orb.setState(snap.phase, snap.phaseProgress,
                    HushUi.motion(requireContext()) && !state.paused);
            if (state.sound && !state.paused && snap.phase != state.lastPhase) cue(snap.phase);
            state.lastPhase = snap.phase;
            final int label = snap.phase == BreathingSession.Phase.INHALE
                    ? R.string.breath_inhale : snap.phase == BreathingSession.Phase.EXHALE
                    ? R.string.breath_exhale : snap.phase == BreathingSession.Phase.HOLD
                    ? R.string.breath_hold : R.string.breath_rest;
            phaseText = state.paused ? getString(R.string.hush_paused)
                    : getString(label, snap.secondsLeft);
        }
        if (!phaseText.equals(phase.getText())) phase.setText(phaseText);
    }
    private int techniqueTitle(){return state.technique==BreathingSession.Technique.BOX?R.string.breath_technique_box
            :state.technique==BreathingSession.Technique.FOUR_SEVEN_EIGHT?R.string.breath_technique_478:R.string.breath_technique_calm;}
    private void chooseTechnique(){
        new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.hush_breathe).setItems(new String[]{
                getString(R.string.breath_technique_calm),getString(R.string.breath_technique_box),getString(R.string.breath_technique_478)},
                (d,which)->{state.technique=BreathingSession.Technique.values()[which];saveOptions();render();}).show();
    }
    private void cue(BreathingSession.Phase phase){if(cues==null)cues=new BreathingSound(requireContext());cues.play(phase);}
    private void saveOptions(){PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
            .putString("hush_breath_technique",state.technique.name()).putInt("hush_breath_duration",state.minutes)
            .putBoolean("hush_breath_sound",state.sound).apply();}
    private MaterialButton button(int title,boolean primary){MaterialButton b=new MaterialButton(requireContext());b.setText(title);HushUi.style(b,primary);return b;}
    private TextView label(String title,int size,boolean bold){TextView t=new TextView(requireContext());t.setText(title);t.setTextSize(size);
        t.setTextColor(color(com.google.android.material.R.attr.colorOnSurface));if(bold)t.setTypeface(t.getTypeface(),1);return t;}
    private ImageButton icon(int drawable,int title){ImageButton b=new ImageButton(requireContext());b.setImageResource(drawable);
        b.setContentDescription(getString(title));b.setImageTintList(android.content.res.ColorStateList.valueOf(color(com.google.android.material.R.attr.colorOnSurface)));
        b.setBackgroundResource(android.R.drawable.list_selector_background);return b;}
    private LinearLayout column(){LinearLayout l=new LinearLayout(requireContext());l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout.LayoutParams top(int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(margin);return p;}
    private int dp(int value){return HushUi.dp(requireContext(),value);}
    private int color(int attr){return HushUi.color(requireContext(),attr);}
    private static final class MeditationRing extends View {
        private final android.graphics.Paint paint=new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final int pine;
        private final float stroke;
        MeditationRing(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            pine=HushUi.color(c,androidx.appcompat.R.attr.colorPrimary);stroke=HushUi.dp(c,2);}
        @Override protected void onDraw(android.graphics.Canvas canvas){
            paint.setColor(pine);paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);float r=Math.min(getWidth(),getHeight())*.38f;
            canvas.drawCircle(getWidth()/2f,getHeight()/2f,r,paint);paint.setAlpha(80);
            canvas.drawCircle(getWidth()/2f,getHeight()/2f,r*.84f,paint);paint.setAlpha(255);
        }
    }
}
