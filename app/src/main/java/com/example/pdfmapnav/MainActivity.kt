package com.example.pdfmapnav

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), LocationListener {
    
    private lateinit var imageView: ImageView
    private lateinit var btnLoad: Button
    private lateinit var tvCoords: TextView
    private lateinit var locationManager: LocationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        imageView = findViewById(R.id.imageView)
        btnLoad = findViewById(R.id.btnLoad)
        tvCoords = findViewById(R.id.tvCoords)
        
        btnLoad.setOnClickListener { loadMap() }
        
        // Проверяем разрешения
        if (checkPermissions()) {
            startGPS()
        } else {
            requestPermissions()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ), 1)
    }
    
    @android.annotation.SuppressLint("MissingPermission")
    private fun startGPS() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L, 1f, this)
        Toast.makeText(this, "GPS запущен", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadMap() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let {
                try {
                    val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                    imageView.setImageBitmap(bitmap)
                    Toast.makeText(this, "Карта загружена!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onLocationChanged(location: Location) {
        tvCoords.text = String.format(
            "📍 Координаты:\nШ: %.6f\nД: %.6f\nТочность: %.1fм",
            location.latitude,
            location.longitude,
            location.accuracy
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startGPS()
            Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }
    
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}
