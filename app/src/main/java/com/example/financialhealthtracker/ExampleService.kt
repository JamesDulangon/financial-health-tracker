package com.example.financialhealthtracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ExampleService : Service( ){

    private var channelID = "Example Service"
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle: Bundle? = intent?.extras
        val x = bundle!!.getInt("input")

        // when notification is clicked, it goes to main activity
        val notificationIntent: Intent = Intent(this, MainActivity::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        createNotification()

        if (x == 0) {
            val notification: Notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle("Financial Health App")
                .setContentText("Application is Running!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
        } else if (x == 1) {
            val notification: Notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle("Financial Health App")
                .setContentText("Today is the deadline of your payments!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
        } else if (x == 2) {
            val notification: Notification = NotificationCompat.Builder(this, channelID)
                .setContentTitle("Financial Health App")
                .setContentText("Your Expenditure is off the charts!")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build()

            startForeground(1, notification)
        }

        return START_NOT_STICKY
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var serviceChannel: NotificationChannel = NotificationChannel(
                channelID,
                "Example Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            //requires context instead of NotificationManager.Class
            //https://developer.android.com/training/notify-user/build-notification#kotlin
            val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(serviceChannel)
        }
    }
}