package org.schabi.newpipe.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimerStopReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SleepTimerService.endsAtElapsedRealtime = 0;
        Intent serviceIntent = new Intent(context, SleepTimerService.class);
        context.stopService(serviceIntent);
    }
}
