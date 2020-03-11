package ds.vuongquocthanh.gogo.api

import com.google.android.gms.maps.model.LatLng
import ds.vuongquocthanh.gogo.mvp.model.googledirection.GoogleDirection
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    //Google map
    @GET("maps/api/directions/json")
    fun getDirection(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("sensor") sensor: String,
        @Query("mode") mode: String,
        @Query("key") key: String
    ): Observable<GoogleDirection>
}