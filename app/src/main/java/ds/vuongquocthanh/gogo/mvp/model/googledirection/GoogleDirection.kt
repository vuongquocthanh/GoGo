package ds.vuongquocthanh.gogo.mvp.model.googledirection

data class GoogleDirection(
    val geocoded_waypoints: List<GeocodedWaypoint>,
    val routes: List<Route>,
    val status: String
)