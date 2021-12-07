package com.example.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import java.util.*

class MainActivity : AppCompatActivity() {

    // Declaracion de variables para la locacion
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    val idPermiso = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Ahora inicializamos la variable para la para la locacion
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        var btnActualizar = findViewById<Button>(R.id.btnAct)
        btnActualizar.setOnClickListener(){
            solicitarPermisos()
            obtenerUltimaUbicacion()
        }

    }

    // 1. Funcion para validar que los permisos esten dando correctamente
    private fun verificarPermisos():Boolean{
        if(
            ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }
        return false
    }

    // 2. En caso de no tener permisos necesitamos solicitarlos al usuario
    private fun solicitarPermisos(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION),
            idPermiso
        )
    }

    // 3. Validar si la localizacion del dispositivo esta habilitada
    private fun hablilitarLocacion(): Boolean{
        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

    // 4. Funcion que nos permitira obtener la ultima localizacion
    private fun obtenerUltimaUbicacion(){
        // Primero chequeamos los pemidos

        if(verificarPermisos()){
            // Verificamos si la localizacion esta habilitada
            if(hablilitarLocacion()){
                // Obtencion de la locaizacion
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener{ task ->
                    var location = task.result
                    if (location == null){
                        // Si la locacion es nula obtendremos la locacion de usuario
                        //actualizaUbicacion()
                    }else{
                        var txtLocation = findViewById<TextView>(R.id.txtLocation)
                        txtLocation.text = "Tus coordenadas actuales son:\nLatutid: " + location.latitude + "\nLongitud: " + location.longitude + "\n" + obtenerCiudad(location.latitude,location.longitude)
                    }
                }
            }else{
                Toast.makeText(this, "Porfavor habilite la localizacion", Toast.LENGTH_SHORT).show()
            }
        }else{
            solicitarPermisos()
        }

    }

    // 5. Debemos crear una funcion para poder ir actualizando la ubicacion.
    /*fun actualizaUbicacion(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient!!.requestLocationUpdates(
            locationRequest,agregarLocacion, Looper.myLooper()
        )
    }*/

    // 5.1 obtenermos los datos de latitud y longitud para agregarlo a nuestra pantalla
    private val agregarLocacion = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            var txtLocation = findViewById<TextView>(R.id.txtLocation)
            println("Nueva Ubicacion")
            txtLocation.text = "Tu ultima ubicación es:\nLongitud: "+ lastLocation.longitude + "\nLatitud: " + lastLocation.latitude + "\n" + obtenerCiudad(lastLocation.latitude,lastLocation.longitude)
        }
    }

    // 6. Tenemos la latitud y longitud, gracais a decoder transformamos estos valores a ciudad.
    private fun obtenerCiudad(lat: Double,long: Double):String{
        var cityName:String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Adress = geoCoder.getFromLocation(lat,long,3)

        cityName = Adress.get(0).locality
        countryName = Adress.get(0).countryName
        return "Ciudad: " + cityName + " Pais: " + countryName
    }
}