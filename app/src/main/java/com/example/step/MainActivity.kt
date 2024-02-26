package com.example.step
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupWithNavController
import kotlin.math.pow

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var running = false
    private var previousAccelValue = 0f
    private var totalSteps = 0
    private lateinit var tv_stepsTaken: TextView
    private lateinit var progress_circular: CircularProgressBar
    private lateinit var navController: NavController
    private lateinit var tv_totalMax: TextView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var congratulated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        setupWithNavController(bottomNavigationView, navController)

        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val dreamValue = sharedPreferences.getString("dream", null)

        tv_totalMax = findViewById(R.id.tv_totalMax)
        tv_stepsTaken = findViewById(R.id.tv_stepsTaken)
        progress_circular = findViewById(R.id.progress_circular)

        if (!dreamValue.isNullOrEmpty()) {
            val maxSteps = dreamValue.toInt()
            tv_totalMax.text = dreamValue
            progress_circular.progressMax = maxSteps.toFloat()
            saveMaxSteps(maxSteps)
        }

        loadData()
        resetSteps()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.button_coor).setOnClickListener {
            fetchLocation()
        }
    }

    private val MIN_ACCEL_DELTA = 1.5f
    private val MOVEMENT_THRESHOLD = 8f

    override fun onResume() {
        super.onResume()
        running = true
        if (accelerometer == null) {
            Toast.makeText(this, "No accelerometer? poplach", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (running && event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val accelValue = calculateTotalAcceleration(event.values)
            val deltaAccel = accelValue - previousAccelValue

            if (!congratulated && deltaAccel > MIN_ACCEL_DELTA && accelValue > MOVEMENT_THRESHOLD) {
                if (totalSteps < getMaxSteps()) {
                    totalSteps++
                    tv_stepsTaken.text = totalSteps.toString()
                    progress_circular.apply {
                        setProgressWithAnimation(totalSteps.toFloat())
                    }
                    val tv_caloriesBurned = findViewById<TextView>(R.id.tv_caloriesBurned)
                    tv_caloriesBurned.text = "Сожжено калорий: ${totalSteps.toFloat() * 0.04}"
                    saveData()
                } else {
                    congratulated = true
                    Toast.makeText(this, "Поздравляем! Вы достигли максимального значения шагов.", Toast.LENGTH_SHORT).show()
                }
            }
            previousAccelValue = accelValue
        }
    }

    private fun calculateTotalAcceleration(accelValues: FloatArray): Float {
        return kotlin.math.sqrt(
            accelValues[0].toDouble().pow(2.0) +
                    accelValues[1].toDouble().pow(2.0) +
                    accelValues[2].toDouble().pow(2.0)
        ).toFloat()
    }

    private fun resetSteps() {
        tv_stepsTaken.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }
        tv_stepsTaken.setOnLongClickListener {
            totalSteps = 0
            tv_stepsTaken.text = totalSteps.toString()
            saveData()
            congratulated = false
            true
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("key1", totalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        totalSteps = sharedPreferences.getInt("key1", 0)
        tv_stepsTaken.text = totalSteps.toString()
    }

    private fun saveMaxSteps(maxSteps: Int) {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("key_max", maxSteps)
        editor.apply()
    }

    private fun getMaxSteps(): Int {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("key_max", 0)
    }

    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),101)
            return
        }
        task.addOnSuccessListener {
            if(it != null){
                Toast.makeText(applicationContext, "${it.latitude} ${it.longitude}", Toast.LENGTH_SHORT).show()

                val uri = Uri.parse("geo:${it.latitude},${it.longitude}?q=${it.latitude},${it.longitude}")

                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")

                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    val playStoreIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps")
                    )
                    startActivity(playStoreIntent)
                }
            }
        }
    }
}

