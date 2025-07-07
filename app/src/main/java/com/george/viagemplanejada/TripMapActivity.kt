package com.george.viagemplanejada

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.george.viagemplanejada.databinding.ActivityTripMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class TripMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityTripMapBinding
    private lateinit var googleMap: GoogleMap
    private var tripName: String? = null
    private var tripDestination: String? = null

    private val waypoints = mutableListOf<TripWaypoint>()
    private val routePolylines = mutableListOf<Polyline>()
    private val waypointMarkers = mutableListOf<Marker>()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obter dados da viagem
        tripName = intent.getStringExtra("trip_name")
        tripDestination = intent.getStringExtra("trip_destination")

        setupUI()
        setupMap()
    }

    private fun setupUI() {
        binding.textTripName.text = "🗺️ ${tripName ?: "Mapa da Viagem"}"
        binding.textTripInfo.text = "Destino: ${tripDestination ?: "Não especificado"}"

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonAddWaypoint.setOnClickListener {
            showAddWaypointDialog()
        }

        binding.buttonShowRoute.setOnClickListener {
            if (waypoints.size >= 2) {
                calculateAndShowRoute()
            } else {
                Toast.makeText(this, "Adicione pelo menos 2 pontos para mostrar a rota", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonMapType.setOnClickListener {
            toggleMapType()
        }

        binding.buttonMapOptions.setOnClickListener {
            showMapOptionsDialog()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Configurar mapa
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true

        // Solicitar permissão de localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Configurar câmera inicial
        setupInitialCamera()

        // Carregar pontos existentes da viagem
        loadTripWaypoints()

        // Configurar clique no mapa
        googleMap.setOnMapClickListener { latLng ->
            showQuickAddWaypointDialog(latLng)
        }

        // Configurar clique nos marcadores
        googleMap.setOnMarkerClickListener { marker ->
            showWaypointDetailsDialog(marker)
            true
        }
    }

    private fun setupInitialCamera() {
        // Se tem destino, tentar centralizar nele
        tripDestination?.let { destination ->
            // Aqui você pode usar a API de Geocoding para obter coordenadas
            // Por enquanto, vamos usar coordenadas padrão do Brasil
            val brazil = LatLng(-14.2350, -51.9253)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(brazil, 4f))
        } ?: run {
            // Centralizar no mundo
            val world = LatLng(0.0, 0.0)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(world, 2f))
        }
    }

    private fun loadTripWaypoints() {
        // Carregar pontos salvos da viagem
        // Por enquanto, vamos criar alguns pontos de exemplo
        if (tripName != null && tripDestination != null) {
            loadSampleWaypoints()
        }
    }

    private fun loadSampleWaypoints() {
        // Pontos de exemplo baseados no destino
        when {
            tripDestination?.contains("São Paulo", ignoreCase = true) == true -> {
                waypoints.addAll(listOf(
                    TripWaypoint(
                        id = "1",
                        name = "Aeroporto de Guarulhos",
                        address = "Guarulhos, SP",
                        latLng = LatLng(-23.4356, -46.4731),
                        type = WaypointType.TRANSPORT,
                        order = 1
                    ),
                    TripWaypoint(
                        id = "2",
                        name = "Centro de São Paulo",
                        address = "São Paulo, SP",
                        latLng = LatLng(-23.5505, -46.6333),
                        type = WaypointType.ATTRACTION,
                        order = 2
                    ),
                    TripWaypoint(
                        id = "3",
                        name = "Museu do Ipiranga",
                        address = "Ipiranga, São Paulo, SP",
                        latLng = LatLng(-23.5856, -46.6103),
                        type = WaypointType.ATTRACTION,
                        order = 3
                    )
                ))
            }
            tripDestination?.contains("Rio de Janeiro", ignoreCase = true) == true -> {
                waypoints.addAll(listOf(
                    TripWaypoint(
                        id = "1",
                        name = "Aeroporto do Galeão",
                        address = "Rio de Janeiro, RJ",
                        latLng = LatLng(-22.8099, -43.2505),
                        type = WaypointType.TRANSPORT,
                        order = 1
                    ),
                    TripWaypoint(
                        id = "2",
                        name = "Cristo Redentor",
                        address = "Corcovado, Rio de Janeiro, RJ",
                        latLng = LatLng(-22.9519, -43.2105),
                        type = WaypointType.ATTRACTION,
                        order = 2
                    ),
                    TripWaypoint(
                        id = "3",
                        name = "Copacabana",
                        address = "Copacabana, Rio de Janeiro, RJ",
                        latLng = LatLng(-22.9711, -43.1822),
                        type = WaypointType.ATTRACTION,
                        order = 3
                    )
                ))
            }
            else -> {
                // Pontos genéricos mundiais
                waypoints.addAll(listOf(
                    TripWaypoint(
                        id = "1",
                        name = "Ponto de Partida",
                        address = "Origem da viagem",
                        latLng = LatLng(-23.5505, -46.6333),
                        type = WaypointType.ORIGIN,
                        order = 1
                    ),
                    TripWaypoint(
                        id = "2",
                        name = tripDestination ?: "Destino",
                        address = tripDestination ?: "Destino da viagem",
                        latLng = LatLng(-22.9068, -43.1729),
                        type = WaypointType.DESTINATION,
                        order = 2
                    )
                ))
            }
        }

        // Mostrar pontos no mapa
        showWaypointsOnMap()
    }

    private fun showWaypointsOnMap() {
        // Limpar marcadores existentes
        waypointMarkers.forEach { it.remove() }
        waypointMarkers.clear()

        // Adicionar marcadores para cada ponto
        waypoints.forEach { waypoint ->
            val markerOptions = MarkerOptions()
                .position(waypoint.latLng)
                .title(waypoint.name)
                .snippet(waypoint.address)
                .icon(getMarkerIcon(waypoint.type))

            val marker = googleMap.addMarker(markerOptions)
            marker?.tag = waypoint
            marker?.let { waypointMarkers.add(it) }
        }

        // Ajustar câmera para mostrar todos os pontos
        if (waypoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.Builder()
            waypoints.forEach { boundsBuilder.include(it.latLng) }
            val bounds = boundsBuilder.build()
            val padding = 100
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    private fun getMarkerIcon(type: WaypointType): BitmapDescriptor {
        return when (type) {
            WaypointType.ORIGIN -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            WaypointType.DESTINATION -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            WaypointType.HOTEL -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            WaypointType.RESTAURANT -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            WaypointType.ATTRACTION -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)
            WaypointType.TRANSPORT -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)
            WaypointType.SHOPPING -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
            else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
        }
    }

    private fun calculateAndShowRoute() {
        // Limpar rotas existentes
        routePolylines.forEach { it.remove() }
        routePolylines.clear()

        // Ordenar pontos por ordem
        val sortedWaypoints = waypoints.sortedBy { it.order }

        // Criar rota simples conectando os pontos
        if (sortedWaypoints.size >= 2) {
            for (i in 0 until sortedWaypoints.size - 1) {
                val from = sortedWaypoints[i]
                val to = sortedWaypoints[i + 1]

                val polylineOptions = PolylineOptions()
                    .add(from.latLng, to.latLng)
                    .width(8f)
                    .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
                    .geodesic(true)

                val polyline = googleMap.addPolyline(polylineOptions)
                routePolylines.add(polyline)
            }

            // Mostrar informações da rota
            showRouteInfo(sortedWaypoints)
        }
    }

    private fun showRouteInfo(waypoints: List<TripWaypoint>) {
        binding.layoutRouteInfo.visibility = android.view.View.VISIBLE
        binding.textWaypointCount.text = "📍 Pontos: ${waypoints.size}"
        binding.textTotalDistance.text = "📏 Distância: ~${calculateApproximateDistance(waypoints)} km"
        binding.textTotalDuration.text = "⏱️ Tempo: ~${calculateApproximateDuration(waypoints)}"
    }

    private fun calculateApproximateDistance(waypoints: List<TripWaypoint>): Int {
        // Cálculo aproximado baseado na distância entre pontos
        var totalDistance = 0.0
        for (i in 0 until waypoints.size - 1) {
            val from = waypoints[i].latLng
            val to = waypoints[i + 1].latLng
            totalDistance += calculateDistance(from, to)
        }
        return totalDistance.toInt()
    }

    private fun calculateDistance(from: LatLng, to: LatLng): Double {
        // Fórmula de Haversine para calcular distância
        val earthRadius = 6371.0 // Raio da Terra em km

        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    private fun calculateApproximateDuration(waypoints: List<TripWaypoint>): String {
        val distance = calculateApproximateDistance(waypoints)
        val hours = distance / 60 // Assumindo 60 km/h de velocidade média
        return if (hours < 1) {
            "${(hours * 60).toInt()} min"
        } else {
            "${hours.toInt()}h ${((hours % 1) * 60).toInt()}min"
        }
    }

    private fun showAddWaypointDialog() {
        // Por enquanto, mostrar toast
        Toast.makeText(this, "Clique no mapa para adicionar um ponto", Toast.LENGTH_SHORT).show()
    }

    private fun showQuickAddWaypointDialog(latLng: LatLng) {
        // Implementar dialog para adicionar ponto rapidamente
        val newWaypoint = TripWaypoint(
            id = System.currentTimeMillis().toString(),
            name = "Novo Ponto",
            address = "Lat: ${String.format("%.4f", latLng.latitude)}, Lng: ${String.format("%.4f", latLng.longitude)}",
            latLng = latLng,
            type = WaypointType.OTHER,
            order = waypoints.size + 1
        )

        waypoints.add(newWaypoint)
        showWaypointsOnMap()

        Toast.makeText(this, "Ponto adicionado: ${newWaypoint.name}", Toast.LENGTH_SHORT).show()
    }

    private fun showWaypointDetailsDialog(marker: Marker): Boolean {
        val waypoint = marker.tag as? TripWaypoint
        waypoint?.let {
            Toast.makeText(this, "📍 ${it.name}\n📍 ${it.address}", Toast.LENGTH_LONG).show()
        }
        return true
    }

    private fun toggleMapType() {
        when (googleMap.mapType) {
            GoogleMap.MAP_TYPE_NORMAL -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                Toast.makeText(this, "Mapa: Satélite", Toast.LENGTH_SHORT).show()
            }
            GoogleMap.MAP_TYPE_SATELLITE -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                Toast.makeText(this, "Mapa: Híbrido", Toast.LENGTH_SHORT).show()
            }
            GoogleMap.MAP_TYPE_HYBRID -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                Toast.makeText(this, "Mapa: Terreno", Toast.LENGTH_SHORT).show()
            }
            else -> {
                googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                Toast.makeText(this, "Mapa: Normal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMapOptionsDialog() {
        Toast.makeText(this, "Opções do mapa em desenvolvimento", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    googleMap.isMyLocationEnabled = true
                }
            }
        }
    }
}