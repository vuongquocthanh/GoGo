package ds.vuongquocthanh.gogo.view.activity.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
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
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import ds.vuongquocthanh.gogo.R
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection
import ds.vuongquocthanh.gogo.mvp.model.googledirection.Leg
import ds.vuongquocthanh.gogo.mvp.model.googledirection.Step
import ds.vuongquocthanh.gogo.mvp.presenter.DirectionPresenter
import ds.vuongquocthanh.gogo.mvp.view.DirectionViewPresenter
import ds.vuongquocthanh.gogo.util.AnimateRotate
import kotlinx.android.synthetic.main.activity_map_test.*
import java.util.*
import kotlin.collections.ArrayList

class MapTestActivity : AppCompatActivity(), OnMapReadyCallback, DirectionViewPresenter{

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
    private var duration = 0
    private  var listPoint = ArrayList<LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_test)
        presenter = DirectionPresenter()
        presenter.attachView(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapTest) as SupportMapFragment
        mapFragment.getMapAsync(this)
        initView()
        eventOnClick()
    }

    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0
        mMap!!.isMyLocationEnabled = false
        mMap!!.uiSettings.isMyLocationButtonEnabled = true

    }

    override fun getDirectionResponse(direction: GoogleDirection) {
        val steps = java.util.ArrayList<Step>()
        val lineOption = PolylineOptions()
        steps.addAll(direction.routes[0].legs[0].steps)

        val legs = ArrayList<Leg>()
        legs.addAll(direction.routes[0].legs)
        for ( j in legs.indices){
            duration +=legs[j].duration.value
        }

        for (i in steps.indices) {
            val points: java.util.ArrayList<LatLng> = java.util.ArrayList()
            points.addAll(decodePolyline(steps[i].polyline.points))
            lineOption.addAll(points)
            lineOption.width(8f)
            lineOption.startCap(SquareCap())
            lineOption.endCap(SquareCap())
            lineOption.jointType(JointType.ROUND)
            lineOption.color(Color.GRAY)
            lineOption.geodesic(true)
            listPoint.addAll(points)
        }

        val rotate = AnimateRotate()
        rotate.animateLine(listPoint,mMap!!,mMarker!!,this)

        mMap!!.addPolyline(lineOption)
    }

    override fun showError(error: String) {

    }


    private fun initView() {
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
//        Log.d("sensorTest", mSensor!!.type.toString())

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

    private fun initPlaces() {
        Places.initialize(this, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
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
                                this@MapTestActivity,
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
            supportFragmentManager.findFragmentById(R.id.autocompleteTest) as AutocompleteSupportFragment
        autoComplete.setPlaceFields(placeFields)
        autoComplete.setOnPlaceSelectedListener(object :
            PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Toast.makeText(applicationContext, "" + place.latLng, Toast.LENGTH_LONG)
                    .show()
                endLocation = place.latLng!!
                val markerOptions =
                    MarkerOptions().position(endLocation).title(place.address)
                        .icon(   BitmapDescriptorFactory.fromBitmap(
                            getBitmapFromVectorDrawable(
                                this@MapTestActivity,
                                R.drawable.ic_pig
                            )))
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


    private fun eventOnClick() {
        btnDirectionTest.setOnClickListener {
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

    fun decodePolyline(encoded:String):List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        var len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len)
        {
            var b:Int
            var shift = 0
            var result = 0
            do
            {
                b = encoded.get(index++).toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            }
            while (b >= 0x20)
            val dlat = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lat += dlat
            shift = 0
            result = 0
            do
            {
                b = encoded.get(index++).toInt() - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            }
            while (b >= 0x20)
            val dlng = (if ((result and 1) != 0) (result shr 1).inv() else (result shr 1))
            lng += dlng
            val p = LatLng(((lat.toDouble() / 1E5)),
                ((lng.toDouble() / 1E5)))
            poly.add(p)
        }
        return poly
    }



}
