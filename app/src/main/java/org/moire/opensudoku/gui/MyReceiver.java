package org.moire.opensudoku.gui;

/**
 * Created by Master on 28/02/2017.
 */
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;

public class MyReceiver extends BroadcastReceiver  {
    @Override
        public void onReceive(Context context, Intent intent) {
            Intent startServiceIntent = new Intent(context, MyIntentService.class);
            context.startService(startServiceIntent);
        }

}
