package com.example.stepcalculator

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Button

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var running = false
    private var currentSteps=0f
    private lateinit var tvstepsTaken:TextView
    private var totalSteps = 0f
    private lateinit var start:Button
    private var previousTotalSteps = 0f
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: Notification.Builder
    private  var backPressedTime: Long = 0
    private val channelId = "12345"
    private val description = "Test Notification"
    @SuppressLint("InlinedApi")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadData()
        resetsteps()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                1)
        }

        val timer = object: CountDownTimer(6000,1000) {
            override fun onTick(millisUntilFinished: Long) {
            }
            override fun onFinish() {
                btnNotify()
            }
        }
        timer.start()
        start=findViewById(R.id.start)
        start.setOnClickListener{resetsteps()}
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun btnNotify() {
        val pendingIntent = PendingIntent.getActivity(this, 0, getIntent(), PendingIntent.FLAG_UPDATE_CURRENT)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notificationChannel = NotificationChannel(channelId, description, NotificationManager .IMPORTANCE_LOW)
            notificationChannel.lightColor = Color.BLUE
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification.Builder(this, channelId)
                .setContentTitle(tvstepsTaken.text.toString()+ "  Steps")
                .setContentText("Tap for details")
                .setSmallIcon(R.drawable .ic_baseline_directions_run_24)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent)
                .setOngoing(true)

        }
        notificationManager.notify(12345, builder.build())
    }

    override fun onResume() {
        super.onResume()
        running = true

        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

         tvstepsTaken = findViewById(R.id.tv_stepsTaken)

        if (running) {
            totalSteps = event!!.values[0]

             currentSteps = (totalSteps.toInt() - previousTotalSteps.toInt()).toFloat()

            tvstepsTaken.text = ("${currentSteps.toInt()}")
            btnNotify()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("CommitPrefEdits")
    private fun resetsteps(){
            val tvstepsTaken = findViewById<TextView>(R.id.tv_stepsTaken)

                previousTotalSteps = totalSteps

                tvstepsTaken.text = 0.toString()

                saveData()
                //btnNotify()
        }
    private fun saveData() {

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putFloat("key1", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)

        Log.d("MainActivity", "$savedNumber")

        previousTotalSteps = savedNumber
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, "Press back again to leave the app.", Toast.LENGTH_LONG).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    }
