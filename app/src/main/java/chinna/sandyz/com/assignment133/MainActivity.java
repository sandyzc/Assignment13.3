package chinna.sandyz.com.assignment133;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private BoundService serviceReference;
    private int REQUEST_CODE = 101;
    private int NOTIFICATION_ID = 102;
    private boolean isBound;

    private String TAG = "bound";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "in MainActivity onCreate");

//        start the service
        Log.i(TAG, "Service starting...");
        Intent intent = new Intent(this, BoundService.class);
        startService(intent);
        sendNotification();

//        starts playing the sound clip
        Button startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    serviceReference.startPlay();
                }
            }
        });

//        stops playing the sound clip
        Button stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBound) {
                    serviceReference.stopPlay();
                }
            }
        });
    }

    //    interface for monitoring the state of the service
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // called when the connection with the service has been
            // established. gives us the service object to use so we can
            // interact with the service.we have bound to a explicit
            // service that we know is running in our own process, so we can
            // cast its IBinder to a concrete class and directly access it.
            Log.i(TAG, "Bound service connected");
            serviceReference = ((BoundService.MyLocalBinder) service).getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // called when the connection with the service has been
            // unexpectedly disconnected -- its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            Log.i(TAG, "Problem: bound service disconnected");
            serviceReference = null;
            isBound = false;
        }
    };

    //    unbind from the service
    private void doUnbindService() {
        Toast.makeText(this, "Unbinding...", Toast.LENGTH_SHORT).show();
        unbindService(myConnection);
        isBound = false;
    }

    //    bind to the service
    private void doBindToService() {
        Toast.makeText(this, "Binding...", Toast.LENGTH_SHORT).show();
        if (!isBound) {
            Intent bindIntent = new Intent(this, BoundService.class);
            isBound = bindService(bindIntent, myConnection,
                    Context.BIND_AUTO_CREATE);
        }
    }

    //    activity starting
    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "MainActivity - onStart - binding...");
//        bind to the service
        doBindToService();
    }

    //    activity stopping
    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "MainActivity - onStop - unbinding...");
//        unbind from the service
        doUnbindService();
    }

    @Override
    public void onBackPressed() {
       /* we customise the back button so that the activity pauses
        instead of finishing*/
        moveTaskToBack(true);
    }

    //    the activity is being destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying activity...");
       /* it's not just being destroyed to rebuild due to orientation
        change but genuinely being destroyed...for ever*/
        if (isFinishing()) {
            Log.i(TAG, "activity is finishing");
//            stop service as activity being destroyed and we won't use it any more
            Intent intentStopService = new Intent(this, BoundService.class);
            stopService(intentStopService);
        }
    }

    /* sends an ongoing notification notifying that service is running.
             it's only dismissed when the service is destroyed*/
    private void sendNotification() {
//        we use the compatibility library
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Service Running")
                .setTicker("Music Playing")
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        Intent startIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                REQUEST_CODE, startIntent, 0);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
