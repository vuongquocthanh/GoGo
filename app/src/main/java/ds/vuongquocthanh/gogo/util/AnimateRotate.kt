package ds.vuongquocthanh.gogo.util

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.util.Property
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.SphericalUtil

class AnimateRotate {
    var _trips:ArrayList<LatLng> = ArrayList()
    lateinit var _marker:Marker
    var _latLngInterpolator:LatLngInterpolator = LatLngInterpolator.Spherical()
    fun animateLine(Trips:ArrayList<LatLng>, map:GoogleMap, marker:Marker, current: Context) {
        _trips.addAll(Trips)
        _marker = marker
        animateMarker()
    }

    fun animateMarker() {
        val typeEvaluator = object: TypeEvaluator<LatLng> {
            override fun evaluate(fraction:Float, startValue:LatLng, endValue:LatLng):LatLng {
                _marker.setRotation(SphericalUtil.computeHeading(startValue, endValue).toFloat() + 90f)//90 do chiều icon mình thêm vào marker tùy chỉnh cho phù hợp
                return _latLngInterpolator.interpolate(fraction, startValue, endValue)
            }
        }
        val property = Property.of(Marker::class.java, LatLng::class.java, "position")
        val animator = ObjectAnimator.ofObject(_marker, property, typeEvaluator, _trips.get(0))
        //ObjectAnimator animator = ObjectAnimator.o(view, "alpha", 0.0f);
        animator.addListener(object: Animator.AnimatorListener {
            override fun onAnimationCancel(animation:Animator) {
                // animDrawable.stop();
            }
            override fun onAnimationRepeat(animation:Animator) {
                // animDrawable.stop();
            }
            override fun onAnimationStart(animation:Animator) {
                // animDrawable.stop();
            }
            override fun onAnimationEnd(animation:Animator) {
                // animDrawable.stop();
                if (_trips.size > 1)
                {
                    _trips.removeAt(0)
                    animateMarker()
                }
            }
        })
        animator.setDuration(280)
        animator.start()
    }
}