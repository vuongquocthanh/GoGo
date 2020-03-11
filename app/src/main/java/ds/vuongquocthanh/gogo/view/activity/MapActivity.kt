package ds.vuongquocthanh.gogo.view.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.annotation.DrawableRes
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
import ds.vuongquocthanh.gogo.mvp.model.Steps
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection
import ds.vuongquocthanh.gogo.mvp.model.googledirection.Leg
import ds.vuongquocthanh.gogo.mvp.model.googledirection.Step
import ds.vuongquocthanh.gogo.mvp.presenter.DirectionPresenter
import ds.vuongquocthanh.gogo.mvp.view.DirectionViewPresenter
import ds.vuongquocthanh.gogo.util.toast
import kotlinx.android.synthetic.main.activity_map.*
import java.util.*
import kotlin.collections.ArrayList

class MapActivity : AppCompatActivity(), OnMapReadyCallback, DirectionViewPresenter{
    private lateinit var presenter: DirectionPresenter
    private var mMap: GoogleMap? = null
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
    private var keyAPI = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        presenter = DirectionPresenter()
        presenter.attachView(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initView()
        eventOnClick()
    }

    private fun eventOnClick() {
        btnFind.setOnClickListener {

        }
    }

    override fun getDirectionResponse(direction: GoogleDirection) {
        toast(direction.status)
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


    }


    override fun showError(error: String) {
        toast(error)
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map
        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        mMap!!.isMyLocationEnabled = true

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap!!.isMyLocationEnabled = true
            } else {
                mMap!!.isMyLocationEnabled = true

            }
        }


//        mMap!!.setOnMapClickListener { latLng ->
//
//
//
//
//            endLocation = LatLng(latLng.latitude, latLng.longitude)
//            val diachi = getAddress(latLng.latitude, latLng.longitude)
//
//            val markStart =
//                MarkerOptions().position(startLocation).title(diachi)
//                    .icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(this@MapActivity,R.drawable.ic_automobile)))
//
//
//            val markEnd =
//                MarkerOptions().position(endLocation).title(diachi)
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
//            listMarker.add(markStart)
//            listMarker.add(markEnd)
//
//            mMap!!.addMarker(markStart)
//            mMap!!.addMarker(markEnd)
//
//
//        }
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
                                this@MapActivity,
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

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable!!)).mutate()
        }

        val bitmap = Bitmap.createBitmap(
            drawable!!.getIntrinsicWidth(),
            drawable!!.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 2000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 10f
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

    private fun setupAutoComplete() {
        val autoComplete =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
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

    private fun getAddress(lat: Double, long: Double): String {
        val geocoder = Geocoder(this@MapActivity, Locale.getDefault())
        val geoList = geocoder.getFromLocation(lat, long, 1)
//        Log.d("geoList",geoList.size.toString())
//        val addressList = ArrayList<Address>()
//        addressList.clear()
//        addressList.addAll(geoList)
//        if (addressList.size > 0) {
//            val address = addressList[0]
//            fullAddress = address.getAddressLine(0)
//            Log.d("address", fullAddress)
//        }
        val address = geoList[0].getAddressLine(0)

        return address
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


}
