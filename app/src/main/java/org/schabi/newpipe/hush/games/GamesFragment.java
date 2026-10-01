package org.schabi.newpipe.hush.games;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.schabi.newpipe.BaseFragment;
import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;
import org.schabi.newpipe.hush.ui.CompactPlaybackBar;
import org.schabi.newpipe.hush.ui.GamePreview;
import org.schabi.newpipe.hush.ui.HushUi;

/** Navigation and chrome; each game has its own controller and independent engine. */
public final class GamesFragment extends BaseFragment {
    private GameScreens.Screen screen;
    private String game;
    public static GamesFragment newInstance(@Nullable String selectedGame){
        GamesFragment result=new GamesFragment();Bundle args=new Bundle();args.putString("game",selectedGame);result.setArguments(args);return result;
    }
    @Override public void onCreate(@Nullable Bundle state){super.onCreate(state);game=getArguments()==null?null:getArguments().getString("game");}
    @Override public View onCreateView(@NonNull android.view.LayoutInflater inflater,@Nullable ViewGroup parent,@Nullable Bundle saved){
        LinearLayout root=column();root.setBackgroundColor(HushUi.color(requireContext(),com.google.android.material.R.attr.colorSurface));
        LinearLayout header=new LinearLayout(requireContext());header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(22),0,dp(22),0);
        ImageButton back=new ImageButton(requireContext());back.setImageDrawable(org.schabi.newpipe.hush.ui.HushIcons.drawable(back.getContext(),R.drawable.ic_hush_back));
        back.setImageTintList(android.content.res.ColorStateList.valueOf(HushUi.color(requireContext(),com.google.android.material.R.attr.colorOnSurface)));
        back.setBackgroundResource(android.R.drawable.list_selector_background);back.setContentDescription(getString(R.string.back));
        back.setOnClickListener(v->requireActivity().getSupportFragmentManager().popBackStack());header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));back.setVisibility(game==null?View.GONE:View.VISIBLE);
        int title=game==null?R.string.hush_all_games:"2048".equals(game)?R.string.hush_game_2048:"snake".equals(game)?R.string.hush_game_snake:
                "sudoku".equals(game)?R.string.hush_game_sudoku:R.string.hush_game_make24;
        TextView name=new TextView(requireContext());name.setText(title);name.setTextSize(25);name.setTypeface(name.getTypeface(),1);
        name.setTextColor(HushUi.color(requireContext(),com.google.android.material.R.attr.colorOnSurface));header.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        header.setMinimumHeight(dp(56));
        HushUi.bindContentWidth(header,game==null?960:480,game==null?960:864); root.addView(header,new LinearLayout.LayoutParams(-1,-2));
        ScrollView scroll=new ScrollView(requireContext());scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
        if(game==null)scroll.addView(hub());else {screen=GameScreens.create(this,game);scroll.addView(screen.view());}
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));root.addView(new CompactPlaybackBar(requireContext()),new LinearLayout.LayoutParams(-1,-2));return root;
    }
    public boolean isHub() { return game == null; }
    private View hub(){
        LinearLayout content=column();content.setPadding(dp(22),dp(16),dp(22),dp(28));
        String[] ids={"2048","snake","sudoku","make24"};int[] titles={R.string.hush_game_2048,R.string.hush_game_snake,R.string.hush_game_sudoku,R.string.hush_game_make24};
        int[] hints={R.string.hush_game_2048_hint,R.string.hush_game_snake_hint,R.string.hush_game_sudoku_hint,R.string.hush_game_make24_hint};
        android.view.View[] cards=new View[4]; for(int i=0;i<4;i++){
            String id=ids[i];LinearLayout card=column();card.setPadding(dp(20),dp(16),dp(20),dp(16));
            HushUi.clickable(card,HushUi.color(requireContext(),com.google.android.material.R.attr.colorSurfaceContainer),20,
                    HushUi.color(requireContext(),com.google.android.material.R.attr.colorOutlineVariant));
            TextView title=new TextView(requireContext());title.setText(titles[i]);title.setTextSize(20);title.setTypeface(title.getTypeface(),1);card.addView(title);
            TextView hint=new TextView(requireContext());hint.setText(hints[i]);hint.setTextSize(14);hint.setMinLines(2);card.addView(hint);
            card.addView(new GamePreview(requireContext(),id),new LinearLayout.LayoutParams(dp(80),dp(80)));
            card.setOnClickListener(v->requireActivity().getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(R.id.fragment_holder,newInstance(id)).addToBackStack(null).commit());
            cards[i]=card;
        }
        // Two columns on ordinary screens, one column with large text or narrow windows.
        for(int i=0;i<4;i+=2) {
            HushUi.Panes row=new HushUi.Panes(requireContext(),cards[i],cards[i+1]);
            row.setBreakpointDp(360); row.setGapDp(16);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.bottomMargin=dp(16);
            content.addView(row,lp);
        }
        HushUi.bindContentWidth(content,960,960);
        return content;
    }
    @Override public void onResume(){super.onResume();if(requireActivity() instanceof MainActivity){ ((MainActivity)requireActivity()).setSearchChrome(true); ((MainActivity)requireActivity()).setHomeNavigation(game==null?"Games":null); }if(screen!=null)screen.resume();}
    @Override public void onPause(){if(screen!=null)screen.pause();super.onPause();}
    @Override public void onSaveInstanceState(@NonNull Bundle out){if(screen!=null)screen.save();super.onSaveInstanceState(out);}
    @Override public void onDestroyView(){if(screen!=null)screen.pause();screen=null;super.onDestroyView();}
    private LinearLayout column(){LinearLayout view=new LinearLayout(requireContext());view.setOrientation(LinearLayout.VERTICAL);return view;}
    private int dp(int value){return HushUi.dp(requireContext(),value);}
}
