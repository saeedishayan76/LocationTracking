package com.shayan.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shayan.location.databinding.ActivityMainBinding
import com.shayan.location.even.LocationEvent
import com.shayan.location.service.LocationService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val foreGroundLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {

                it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    checkBackgroundLocation()
                }
            }
        }


    private val postNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {

            if (it) {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, LocationService::class.java)
                )
            } else {

            }

        }

    private val backgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

                //access background

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {

                    ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
                }

            } else {
                //not access
            }
        }

    private fun checkBackgroundLocation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnStartService.setOnClickListener {

            checkForeGroundPermission()
        }

        binding.btnStopService.setOnClickListener {
            stopService(Intent(this, LocationService::class.java))
        }

    }



    private fun checkForeGroundPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            foreGroundLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )

        } else {
            ContextCompat.startForegroundService(this, Intent(this, LocationService::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe
    fun receiveLocationEvent(locationEvent: LocationEvent?) {
        binding.tvLatitude.text = "latitude is ${locationEvent?.latitude}"
        binding.tvLongitude.text = "longitude is ${locationEvent?.longitiude}"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}