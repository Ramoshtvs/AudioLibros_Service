package com.example.andres.audiolibros;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;

public class MyServicio extends Service {

    //variable para el ID del canal
    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    //Metodo oncreate
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //Metodo para crear la notificacion
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    Uri obtenerDireccion;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String input = intent.getStringExtra("inputExtra");
        obtenerDireccion = Uri.parse(input);
        createNotificationChannel();
        Intent notificationIntent= new Intent(MyServicio.this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                MyServicio.this,
                0,
                notificationIntent,
                0
                );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Mi Audio Libros Service")
                .setContentText("Reproduciendo libro")
                .setSmallIcon(R.drawable.preview)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        try {
            StartAudio();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final IBinder binder = new MiBinder();

    public MyServicio() {}

    public class MiBinder extends Binder {
        public MyServicio getService() {
            return MyServicio.this;
        }
    }

    public IBinder getBinder() {
        return binder;
    }




    private static String TAG = "ForegroundService";
    MediaPlayer mediaPlayer;
    private boolean currentlySendingtAudio = false;

    public void StartAudio() throws IOException {
        Log.d("MENSAJEIMPORTANTE", "Comenzó a reproducirce audio");
        currentlySendingtAudio = true;

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(getApplicationContext(), obtenerDireccion);
        mediaPlayer.prepare();
        mediaPlayer.setLooping(false);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
    }

    public void StopAudio() {
        Log.d("MENSAJEIMPORTANTE", "Se detuvo la reproducción de audio");
        currentlySendingtAudio = false;
        mediaPlayer.stop();
        mediaPlayer.release();
    }


}
