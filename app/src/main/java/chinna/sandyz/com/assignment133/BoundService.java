package chinna.sandyz.com.assignment133;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;



public class BoundService extends Service {

    /*the interface object that receives interactions from clients
    it's given to the client to access the Service's public methods*/
    private final IBinder myBinder = new MyLocalBinder();
    private Thread backgroundThread;
    private MediaPlayer player;
    private int NOTIFICATION_ID = 102;

    private String TAG = "bound";

    /**
     * A client is binding to the service with bindService()
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called...");
        return myBinder;
    }

    /**
     * Called when the service is being created.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
//          do the work in a separate thread so main thread is not blocked
                Log.i(TAG, "Thread running");
                playMusic();
            }
        });
        backgroundThread.start();
    }

    //    called when the service starts from a call to startService()
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started by startService()");
        return START_STICKY;
    }

    //    play the sound clip
    private void playMusic() {
        if (player != null) {
            player.release();
        }
//        sound clip in res/raw folder
        player = MediaPlayer.create(this, R.raw.crash_burn);
        player.setLooping(true);
    }

    /***********the public methods******************>>>>>>>*/

//    start play
    public void startPlay() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    //    stop play
    public void stopPlay() {
        if (player.isPlaying()) {
            player.pause();
        }
    }
    /*<<<<<<<<<*********the public methods*******************/


    //    the class used for the client Binder
    public class MyLocalBinder extends Binder {
        BoundService getService() {
           /* return this instance of the BoundService
            so the client can access the public methods*/
            return BoundService.this;
        }
    }

    /**
     * Called when The service is no longer used and is being destroyed
     */
    @Override
    public void onDestroy() {
//        release player and thread
        Log.i(TAG, "Destroying Service");
        Toast.makeText(this, "Destroying Service...", Toast.LENGTH_SHORT).show();
        player.release();
        player = null;
        Thread dummy = backgroundThread;
        backgroundThread = null;
        dummy.interrupt();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "Cancelling notification");
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
