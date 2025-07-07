package com.george.viagemplanejada

import com.google.android.gms.maps.model.LatLng

data class TripWaypoint(
    val id: String,
    val name: String,
    val address: String,
    val latLng: LatLng,
    val type: WaypointType,
    val order: Int,
    val notes: String = "",
    val visitDate: String = "",
    val visitTime: String = ""
)

enum class WaypointType {
    ORIGIN,      // Ponto de partida
    DESTINATION, // Destino final
    ATTRACTION,  // Atração turística
    HOTEL,       // Hotel/hospedagem
    RESTAURANT,  // Restaurante
    TRANSPORT,   // Transporte (aeroporto, rodoviária)
    SHOPPING,    // Compras
    OTHER        // Outros
}

data class TripRoute(
    val waypoints: List<TripWaypoint>,
    val totalDistance: String = "",
    val totalDuration: String = "",
    val routePolyline: String = ""
)

data class RouteSegment(
    val from: TripWaypoint,
    val to: TripWaypoint,
    val distance: String,
    val duration: String,
    val polyline: String
)