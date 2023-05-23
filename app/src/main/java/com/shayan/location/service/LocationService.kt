package com.shayan.location.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.shayan.location.even.LocationEvent
import com.shayan.location.R
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {


    companion object {
         const val CHANNEL_NAME =  "CHANNEL_NAME"
         const val CHANNEL_ID =  "1234"
    }


    private var fusedLocationProviderClient: FusedLocationProviderClient?= null
    private var locationRequest: LocationRequest?= null
    private var locationCallBack: LocationCallback?= null
    private var notificationManager: NotificationManager?= null


    override fun onCreate() {
        super.onCreate()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()

        locationCallBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                showLocationInNotification(locationResult.lastLocation)
                EventBus.getDefault().post(
                    LocationEvent(
                        locationResult.lastLocation?.latitude,
                        locationResult.lastLocation?.longitude
                    )
                )
            }
        }
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showLocationInNotification(location: Location?) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Location ...")
            .setContentText("Location is (${location?.latitude}, ${location?.longitude})")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setSilent(true)
            .build()

        startForeground(1234, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
         super.onStartCommand(intent, flags, startId)
        requestLocation()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(locationRequest!!, locationCallBack!!,
                Looper.getMainLooper()
            )
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removePermission() {
        locationCallBack?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        stopForeground(true)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        removePermission()

    }

}