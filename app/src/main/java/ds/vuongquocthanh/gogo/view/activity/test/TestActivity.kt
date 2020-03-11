package ds.vuongquocthanh.gogo.view.activity.test

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import ds.vuongquocthanh.gogo.R
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection
import ds.vuongquocthanh.gogo.mvp.model.googledirection.Step
import ds.vuongquocthanh.gogo.mvp.presenter.DirectionPresenter
import ds.vuongquocthanh.gogo.mvp.view.DirectionViewPresenter
import ds.vuongquocthanh.gogo.util.LatLngInterpolator
import ds.vuongquocthanh.gogo.util.MarkerAnimation
import kotlinx.android.synthetic.main.activity_test.*
import java.util.*


class TestActivity : AppCompatActivity(), OnMapReadyCallback, DirectionViewPresenter,
    LocationListener {
    private var mPositionMarker: Marker? = null
    private var mMap: GoogleMap? = null
    private var keyAPI = ""
    private lateinit var presenter: DirectionPresenter
    private lateinit var fusedLocation: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var placesClient: PlacesClient
    private var mLastLocation: Location? = null

    private var mMarker: Marker? = null
    private var placeFields = Arrays.asList(
        Place.Field.LAT_LNG,
        Place.Field.NAME,
        Place.Field.ADDRESS,
        Place.Field.ID
    )
    private val listMarker = ArrayList<MarkerOptions>()

    private val PERMISSION_REQUEST = 121
    private var lat = 0.0
    private var lon = 0.0
    private lateinit var startLocation: LatLng
    private lateinit var endLocation: LatLng
    private var sensormanager: SensorManager? = null
    private var sensor: Sensor? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        presenter = DirectionPresenter()
        presenter.attachView(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

      initSensor()
        initView()
        eventOnClick()

    }


    override fun onResume() {
        super.onResume()
        sensormanager!!.registerListener(mEventListener,
            sensormanager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL);
    }

    override fun onPause() {
        super.onPause()
        sensormanager!!.unregisterListener(mEventListener);
    }




    private val mValuesMagnet = FloatArray(3)
    private val mValuesAccel = FloatArray(3)
    private val mValuesOrientation = FloatArray(3)
    private val mRotationMatrix = FloatArray(9)

    private fun initSensor(){
        sensormanager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensormanager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensormanager!!.registerListener(
            mEventListener, sensor,
            SensorManager.SENSOR_DELAY_GAME
        )

        SensorManager.getRotationMatrix(mRotationMatrix, null, mValuesAccel, mValuesMagnet)
        var rotateFloat =  SensorManager.getOrientation(mRotationMatrix, mValuesOrientation)
        Log.d("rotateFloat",rotateFloat[0].toString())
    }

    val mEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
            // Handle the events for which we registered
            when (event.sensor.getType()) {
                Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, mValuesAccel, 0, 3)
                Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, mValuesMagnet, 0, 3)
            }
        }
    }


    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
        mMap!!.isMyLocationEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = true


    }

    override fun onLocationChanged(location: Location?) {
        if (location == null)
            return
        if (mPositionMarker == null) {
            mPositionMarker = mMap!!.addMarker(
                MarkerOptions()
                    .flat(true)
                    .icon(
                        BitmapDescriptorFactory
                            .fromResource(R.drawable.ic_launcher_foreground)
                    )
                    .anchor(0.5f, 0.5f)
                    .position(
                        LatLng(
                            location.latitude, location
                                .longitude
                        )
                    )
            )

        }
        //rotateMarker(mMarker!!,0)
        mMarker!!.rotation =
            getBearing(LatLng(location.latitude, location.longitude), LatLng(21.031256, 105.850840))
        animateMarker(mPositionMarker!!, location) // Helper method for smooth
        mMap!!.animateCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    location
                        .latitude, location.longitude
                )
            )
        )

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .bearing(
                getBearing(
                    LatLng(location.latitude, location.longitude),
                    LatLng(21.031256, 105.850840)
                )
            )
            .zoom(mMap!!.cameraPosition.zoom)
            .build()
        mMap!!.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            2000,
            null
        )

    }


    override fun getDirectionResponse(direction: GoogleDirection) {
        val steps = ArrayList<Step>()
        val lineOption = PolylineOptions()
        steps.addAll(direction.routes[0].legs[0].steps)

        for (i in steps.indices) {
            val points: ArrayList<LatLng> = ArrayList()
            points.addAll(decodePolyline(steps[i].polyline.points))
            lineOption.addAll(points)
            lineOption.width(8f)
            lineOption.startCap(SquareCap())
            lineOption.endCap(SquareCap())
            lineOption.jointType(JointType.ROUND)
            lineOption.color(Color.GRAY)
            lineOption.geodesic(true)
        }
        mMap!!.addPolyline(lineOption)

        if (mMarker == null) {
            mMarker = mMap!!.addMarker(
                MarkerOptions().position(endLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
            )
            MarkerAnimation.animateMarkerToGB(
                mMarker!!,
                endLocation,
                LatLngInterpolator.Spherical()
            )
            mMarker!!.rotation = getBearing(endLocation, startLocation)

        } else {
            MarkerAnimation.animateMarkerToICS(
                mMarker!!,
                endLocation,
                LatLngInterpolator.Spherical()
            )
            mMarker!!.rotation = getBearing(endLocation, startLocation)

        }

    }

    override fun showError(error: String) {

    }


    //initalize

    private fun initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun initView() {
        keyAPI = getString(R.string.google_maps_key)
        initPlaces()
        setupAutoComplete()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallback()

                fusedLocation =
                    LocationServices.getFusedLocationProviderClient(this)
                fusedLocation.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.myLooper()
                )
            }
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocation = LocationServices.getFusedLocationProviderClient(this)
            fusedLocation.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)
                mLastLocation = result!!.locations[result.locations.size - 1]
                if (mMarker != null) {
                    mMarker!!.remove()
                }
                lat = mLastLocation!!.latitude
                lon = mLastLocation!!.longitude
                startLocation = LatLng(lat, lon)

                val currentLocation = startLocation
                val markerCurrent = MarkerOptions()
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            getBitmapFromVectorDrawable(
                                this@TestActivity,
                                R.drawable.ic_pig
                            )
                        )
                    )

                    .position(currentLocation).title("vị trí hiện tại")
                listMarker.add(markerCurrent)
                mMarker = mMap!!.addMarker(markerCurrent)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 10f
    }

    private fun setupAutoComplete() {
        val autoComplete =
            supportFragmentManager.findFragmentById(R.id.autocomplete) as AutocompleteSupportFragment
        autoComplete.setPlaceFields(placeFields)
        autoComplete.setOnPlaceSelectedListener(object :
            com.google.android.libraries.places.widget.listener.PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Toast.makeText(applicationContext, "" + place.latLng, Toast.LENGTH_LONG)
                    .show()
                endLocation = place.latLng!!
                val markerOptions =
                    MarkerOptions().position(endLocation).title(place.address)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = mMap!!.addMarker(markerOptions)
                mMap!!.moveCamera(CameraUpdateFactory.newLatLng(endLocation))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12f))

            }

            override fun onError(place: Status) {
                Toast.makeText(applicationContext, "" + place.statusMessage, Toast.LENGTH_LONG)
                    .show()

            }

        })
    }


    //function

    private fun eventOnClick() {
        btnDirection.setOnClickListener {
            presenter.getDirection(
                "${startLocation.latitude}, ${startLocation.longitude}",
                "${endLocation.latitude}, ${endLocation.longitude}",
                "false",
                "driving",
                keyAPI
            )

        }
    }

    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable!!)).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_REQUEST
                )
            }
            return false

        } else
            return true
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }


    /////animation

    fun rotateMarker(marker: Marker, toRotation: Float) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val startRotation = marker.rotation
        val duration: Long = 1000
        val interpolator = LinearInterpolator()
        Log.d("bearing", "Bearing: " + toRotation)
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = interpolator.getInterpolation(elapsed.toFloat() / duration)
                val rot = t * toRotation + (1 - t) * startRotation
                marker.rotation = if (-rot > 180) rot / 2 else rot
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10)
                }
            }
        })
    }

    private fun animateMarker(marker: Marker, location: Location) {
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val startLatLng = marker.position
        val startRotation = marker.rotation
        val duration: Long = 500
        val interpolator = LinearInterpolator()
        handler.post(object : Runnable {
            override fun run() {
                val elapsed = SystemClock.uptimeMillis() - start
                val t = interpolator.getInterpolation((elapsed.toFloat() / duration))
                val lng = (t * location.longitude + ((1 - t) * startLatLng.longitude))
                val lat = (t * location.latitude + ((1 - t) * startLatLng.latitude))
                val rotation = ((t * location.bearing + ((1 - t) * startRotation)))
                marker.position = LatLng(lat, lng)
                marker.rotation = rotation
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }

    private fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)
        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat))).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270).toFloat()
        return -1f
    }

}
