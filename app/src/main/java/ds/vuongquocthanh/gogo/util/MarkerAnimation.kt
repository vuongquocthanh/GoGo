package ds.vuongquocthanh.gogo.util

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.Property
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

object MarkerAnimation {
    fun animateMarkerToGB(marker: Marker, finalPosition: LatLng, latLngInterpolator:LatLngInterpolator) {
        val startPosition = marker.getPosition()
        val handler = Handler()
        val start = SystemClock.uptimeMillis()
        val interpolator = AccelerateDecelerateInterpolator()
        val durationInMs = 3000f
        handler.post(object:Runnable {
            internal var elapsed:Long = 0
            internal var t:Float = 0.toFloat()
            internal var v:Float = 0.toFloat()
            public override fun run() {
                // Calculate progress using interpolator
                elapsed = SystemClock.uptimeMillis() - start
                t = elapsed / durationInMs
                v = interpolator.getInterpolation(t)
                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition))
                // Repeat till progress is complete.
                if (t < 1)
                {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16)
                }
            }
        })
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    internal fun animateMarkerToHC(marker:Marker, finalPosition:LatLng, latLngInterpolator:LatLngInterpolator) {
        val startPosition = marker.getPosition()
        val valueAnimator = ValueAnimator()
        valueAnimator.addUpdateListener { animation ->
            val v = animation.getAnimatedFraction()
            val newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition)
            marker.setPosition(newPosition)
        }
        valueAnimator.setFloatValues(0F, 1f) // Ignored.
        valueAnimator.setDuration(3000)
        valueAnimator.start()
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    fun animateMarkerToICS(marker:Marker, finalPosition:LatLng, latLngInterpolator:LatLngInterpolator) {
        val typeEvaluator =
            TypeEvaluator<LatLng> { fraction, startValue, endValue -> latLngInterpolator.interpolate(fraction, startValue, endValue) }
        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition)
        animator.setDuration(3000)
        animator.start()
    }
}