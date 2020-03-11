package ds.vuongquocthanh.gogo.mvp.model.googledirection

data class GeocodedWaypoint(
    val geocoder_status: String,
    val place_id: String,
    val types: List<String>
)