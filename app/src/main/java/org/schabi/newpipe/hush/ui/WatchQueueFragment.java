package org.schabi.newpipe.hush.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.schabi.newpipe.R;
import org.schabi.newpipe.player.helper.PlayerHolder;
import org.schabi.newpipe.player.playqueue.PlayQueue;
import org.schabi.newpipe.player.playqueue.PlayQueueAdapter;
import org.schabi.newpipe.player.playqueue.PlayQueueItem;
import org.schabi.newpipe.player.playqueue.PlayQueueItemBuilder;
import org.schabi.newpipe.player.playqueue.PlayQueueItemHolder;

/** A view of the existing session queue, without owning or restarting playback. */
public final class WatchQueueFragment extends Fragment {
    private RecyclerView list;
    private TextView empty;
    private PlayQueue shown;
    private PlayQueueAdapter adapter;
    private final Runnable observer=()->{if(list!=null)list.post(this::refresh);};
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,@Nullable Bundle state){
        FrameLayout root=new FrameLayout(requireContext());
        list=new RecyclerView(requireContext());list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setClipToPadding(false);list.setPadding(0,0,0,HushUi.dp(requireContext(),24));root.addView(list);
        empty=new TextView(requireContext());empty.setText(R.string.hush_queue_empty);empty.setTextSize(16);
        int inset=HushUi.dp(requireContext(),24);empty.setPadding(inset,inset,inset,inset);root.addView(empty);
        return root;
    }
    @Override public void onStart(){super.onStart();PlayerHolder.getInstance().addUiObserver(observer);refresh();}
    @Override public void onStop(){PlayerHolder.getInstance().removeUiObserver(observer);super.onStop();}
    private void refresh(){
        if(list==null)return;
        PlayQueue queue=PlayerHolder.getInstance().getPlayQueue();
        empty.setVisibility(queue==null || queue.isEmpty()?View.VISIBLE:View.GONE);
        if(queue==shown && adapter!=null)return;
        if(adapter!=null)adapter.dispose();adapter=null;shown=null;
        if(queue==null || queue.getBroadcastReceiver()==null){list.setAdapter(null);return;}
        shown=queue;adapter=new PlayQueueAdapter(requireContext(),queue);
        adapter.setSelectedListener(new PlayQueueItemBuilder.OnSelectedListener(){
            @Override public void selected(PlayQueueItem item,View view){PlayerHolder.getInstance().selectQueueItem(item);}
            @Override public void held(PlayQueueItem item,View view){
                new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                    .setTitle(item.getTitle()).setNegativeButton(R.string.cancel,null)
                    .setPositiveButton(R.string.delete,(d,w)->queue.remove(queue.indexOf(item))).show();
            }
            @Override public void onStartDrag(PlayQueueItemHolder holder){}
        });list.setAdapter(adapter);
    }
    @Override public void onDestroyView(){
        if(adapter!=null)adapter.dispose();adapter=null;shown=null;
        list.removeCallbacks(observer);list=null;empty=null;super.onDestroyView();
    }
}
