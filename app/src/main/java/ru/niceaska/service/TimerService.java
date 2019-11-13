package ru.niceaska.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class TimerService extends Service {


    private CountDownTimer countDownTimer;
    private int notificationId;


    private static final String CHANNEL_ID = "1";
    private static final String ACTION_CLOSE = "action close";
    private final int COUNT_TIME = 100000;
    private final int COUNT_PERIOD = 1000;
    private final String SERVICE_NAME = "Timer Service";
    private final String NOTIFICATION_TEXT = "Осталось ";


    private Notification buildNotification(String time, long progress) {

        Intent intent = new Intent(this, MainActivity.class);
        Intent close = new Intent(this, TimerService.class);
        intent.setAction(ACTION_CLOSE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        PendingIntent pendingIntentClose = PendingIntent.getActivity(this, 0, close, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(SERVICE_NAME)
                .setContentIntent(pendingIntent)
                .addAction(0, "Stop service", pendingIntentClose)
                .setProgress(COUNT_TIME / COUNT_PERIOD, (int) progress, false)
                .setContentText(NOTIFICATION_TEXT + time);
        return builder.build();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "1", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        notificationId = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createCountDownTimer(COUNT_TIME, COUNT_PERIOD);
        createChannel();
        startForeground(++notificationId, buildNotification(String.valueOf(COUNT_TIME / COUNT_PERIOD), 0));
        if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
            if (ACTION_CLOSE.equals(intent.getAction())) {
                this.countDownTimer.cancel();
                countDownTimer = null;

                stopSelf();

                return START_NOT_STICKY;
            }
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(String time, long progress) {
        Notification notification = buildNotification(time, progress);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        for (int i = 1; i <= notificationId; i++) {
            notificationManager.notify(i, notification);
        }
    }

    private void createCountDownTimer(final long time, long period) {
        countDownTimer = new CountDownTimer(time, period) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateNotification(String.valueOf(millisUntilFinished / COUNT_PERIOD),
                        (time / COUNT_PERIOD - millisUntilFinished / COUNT_PERIOD));

            }

            @Override
            public void onFinish() {
                stopForeground(true);
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
