package com.example.tracktogether.checkinviews

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.example.tracktogether.R
import com.example.tracktogether.databinding.ActivityMapsBinding
import com.example.tracktogether.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.tracktogether.utils.PermissionUtils.isPermissionGranted
import com.example.tracktogether.utils.PermissionUtils.requestPermission
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for Google Maps, using Geofire to determine if user is near any 'keys'
 * Author: Ong Ze Quan
 * Updated: 6 Mar 2022
 */
class MapsActivity : AppCompatActivity(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener,
    OnMapReadyCallback, OnRequestPermissionsResultCallback, GeoQueryEventListener {

    private var permissionDenied = false
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var myLocationRef: DatabaseReference
    private lateinit var offices: HashMap<String, GeoLocation>
    private lateinit var myOffice: DatabaseReference
    private var geoQuery: GeoQuery? = null
    private lateinit var geoFire: GeoFire


    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null

    // A default location (Upper Pierce Reservoir Park) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(1.3680309395551657, 103.80400692159104)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!isLocationEnabled(applicationContext)) {
            Toast.makeText(this, "Please enable location service", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Set up Geofire
            setGeoFire()
            /**
             * TODO Commented out. Allow admin to add Offices
             */
            //addOfficeToFirebase()

            // Initialize location request
            buildLocationRequest()
            buildLocationCallBack()

            // Construct a FusedLocationProviderClient.
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }


    }


    /**
     * Request quality of service for location updates*
     * */
    private fun buildLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            isWaitForAccurateLocation = true
            interval = 5000
            fastestInterval = 3000
            smallestDisplacement = 10f
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val task = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())
        task.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states != null) {
                if (states.isLocationPresent) {
                    //Toast.makeText(this, "Yeah its on", Toast.LENGTH_LONG).show()
                } else {
                    finish()
                }
            }
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    // Handle result in onActivityResult()
                    e.startResolutionForResult(
                        this,
                        LOCATION_SETTING_REQUEST
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }
    }

    /**
     * Used for receiving notification from FusedLocationProviderAPI when device location has changed
     * */
    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    // Update UI
                    lastKnownLocation = location
                    updateUserLocation()
                    // Do we move the camera?
//                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                        LatLng(lastKnownLocation!!.latitude,
//                            lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                }
            }
        }
    }

    /**
     * Stop receiving location updates in background
     * */
    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
        enableMyLocation()
        map.uiSettings.isZoomControlsEnabled = true
        getDeviceLocation()
        addOfficeMarkers()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback,
            Looper.myLooper()!!
        )

    }

    /**
     * Get location of device
     */
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (!permissionDenied) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        addUserQuery()
                        if (lastKnownLocation != null) {
                            map.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
            requestPermission(
                this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_COARSE_LOCATION, true
            )
        }
    }

    /**
     * Allow user to click top right button to refersh location
     */
    override fun onMyLocationButtonClick(): Boolean {
        getDeviceLocation()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    /**
     * For debugging, testing when clicking on current location
     */
    override fun onMyLocationClick(location: Location) {
        // Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    /**
     * Helper to request for permissions
     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Check if location service is enabled
     */
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    /**
     * Set up geoFire
     */
    private fun setGeoFire() {
        myLocationRef = FirebaseDatabase.getInstance().getReference("geofire")
        geoFire = GeoFire(myLocationRef)
    }

    /**
     * Update geofire query location to latest device location
     */
    private fun updateUserLocation() {
        geoQuery?.setLocation(
            GeoLocation(
                lastKnownLocation!!.latitude,
                lastKnownLocation!!.longitude
            ), 0.3
        )
    }

    /**
     * Create geoFire query and add event listener
     */
    private fun addUserQuery() {
        geoQuery = geoFire.queryAtLocation(
            GeoLocation(
                lastKnownLocation!!.latitude,
                lastKnownLocation!!.longitude
            ), 0.3
        )
        geoQuery!!.addGeoQueryEventListener(this@MapsActivity)
    }

    /**
     * Add markers on map to show office locations
     */
    private fun addOfficeMarkers() {
        myOffice = FirebaseDatabase.getInstance().getReference("geofire")
        // Attach a listener to read the data at our posts reference
        // Attach a listener to read the data at our posts reference
        myOffice.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val officesReceived: Map<Double, Double> = HashMap()
                for (officeSnapshot in dataSnapshot.children) {
                    val officeName = officeSnapshot.key
                    val geoContainer = officeSnapshot.child("l")
                    val lat: ArrayList<*>? = geoContainer.value as ArrayList<*>?
                    val currLatlng = LatLng(lat?.get(0) as Double, lat.get(1) as Double)
                    // Add circle to indicate radius
                    map.addCircle(
                        CircleOptions().center(currLatlng).radius(300.0).strokeColor(Color.RED)
                            .fillColor(0x220000FF).strokeWidth(5.0f)
                    )
                    // Add marker for labelling
                    val marker = map.addMarker(
                        MarkerOptions().position(currLatlng).title(officeName).icon(
                            BitmapDescriptorFactory.defaultMarker((0..360).random().toFloat())
                        )
                    )
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MapsActivity,
                    "An error occurred when retrieving from Firebase",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Used when office is local
//        for (office in offices)
//        {
//            val currLatlng = LatLng(office.value.latitude,office.value.longitude)
//            map.addCircle(CircleOptions().center(currLatlng).radius(300.0).strokeColor(Color.RED).fillColor(0x220000FF).strokeWidth(5.0f))
//        }
    }


    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private val TAG = MapsActivity::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val DEFAULT_ZOOM = 15
        const val LOCATION_SETTING_REQUEST = 100

    }

    /**
     * Helper to add offices to Realtime DB
     * TODO Refactor to allow admins to add offices? Fetch locations from Firebase
     */
    private fun addOfficeToFirebase() {
        offices = HashMap()
        // NYP
        offices["SIT-NYP"] = GeoLocation(1.3774655762068986, 103.84880040873779)
        // DOVER
        offices["SIT-DOVER"] = GeoLocation(1.3005229563740133, 103.7808147427592)
        // SP
        offices["SIT-SP"] = GeoLocation(1.307987932632166, 103.77769716882584)
        // NP
        offices["SIT-NP"] = GeoLocation(1.3341081397689443, 103.77502654858803)
        // RP
        offices["SIT-RP"] = GeoLocation(1.444446653210142, 103.78363663628559)
        // TP
        offices["SIT-TP"] = GeoLocation(1.3436021355374412, 103.93213418417001)

        offices["ZQ-HOUSE"] = GeoLocation(1.31371726992174, 103.87304766137107)

        offices["M-HOUSE"] = GeoLocation(1.316421616156953, 103.8878443635319)

        offices["REU-HOUSE"] = GeoLocation(1.3586779785254226, 103.96284950340926)

        offices["JEV-HOUSE"] = GeoLocation(1.311082482664158, 103.76688054144427)

        offices["CH-HOUSE"] = GeoLocation(1.3070685730037106, 103.86105989558388)

        val db = FirebaseFirestore.getInstance()
        val myCard = hashMapOf("nfcId" to "d9926198")
        for (office in offices) {
            geoFire.setLocation(office.key, office.value)
            db.collection("nfc").document(office.key).set(myCard)
        }


    }


    /***
     * Geofire query events
     */

    override fun onKeyEntered(key: String?, location: GeoLocation?) {
        Toast.makeText(this@MapsActivity, "Entered office area $key", Toast.LENGTH_SHORT).show()
        val intent: Intent = Intent(this, NFCActivity::class.java)
        intent.putExtra("OFFICE", key.toString())
        startActivity(intent)
        finish()
    }

    override fun onKeyExited(key: String?) {
        Toast.makeText(this@MapsActivity, "Exited office area", Toast.LENGTH_SHORT).show()
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {
        Toast.makeText(this@MapsActivity, "moving within $key", Toast.LENGTH_SHORT).show()
    }

    override fun onGeoQueryReady() {
        //Toast.makeText(this@MapsActivity, "All initial data is loaded and events has been fired!", Toast.LENGTH_SHORT).show()
    }

    override fun onGeoQueryError(error: DatabaseError?) {
        Toast.makeText(this@MapsActivity, error!!.message, Toast.LENGTH_SHORT).show()
    }
}