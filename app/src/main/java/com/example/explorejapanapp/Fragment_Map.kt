package com.example.explorejapanapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.util.Log

class Fragment_Map : Fragment(), OnMapReadyCallback {

    internal var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Ініціалізація макета
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Отримуємо MapView
        mapView = view.findViewById(R.id.map_view)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)

        // Отримуємо кнопку для вибору типу мапи
        val mapTypeButton = view.findViewById<Button>(R.id.map_type_button)

        // Обробка натискання на кнопку вибору типу мапи
        mapTypeButton.setOnClickListener { button ->
            Log.d("Fragment_Map", "Натискання на кнопку вибору типу мапи")
            val popupMenu = PopupMenu(requireContext(), button)
            popupMenu.menu.add("Звичайна")
            popupMenu.menu.add("Супутник")
            popupMenu.menu.add("Ландшафт")
            popupMenu.menu.add("Гібрид")

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Звичайна" -> googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
                    "Супутник" -> googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    "Ландшафт" -> googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    "Гібрид" -> googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
                true
            }
            popupMenu.show()
        }

        // Логування розміру мапи після ініціалізації
        mapView?.post {
            val width = mapView?.width ?: 0
            val height = mapView?.height ?: 0
            Log.d("Fragment_Map", "Map Size After Init: Width=$width, Height=$height")
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Налаштування початкової позиції мапи (центр Японії)
        val japanCenter = LatLng(36.2048, 138.2529) // Координати центру Японії
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(japanCenter, 5f)) // Масштаб 5 для відображення всієї Японії

        // Налаштування UI-елементів
        googleMap?.uiSettings?.apply {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false
            isRotateGesturesEnabled = true
            isScrollGesturesEnabled = true
            isTiltGesturesEnabled = true
            isZoomGesturesEnabled = true
        }

        // Встановлюємо тип мапи за замовчуванням (звичайна)
        googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL

        // Обробка натискання на мапу
        googleMap?.setOnMapClickListener { latLng ->
            Log.d("Fragment_Map", "Натискання на мапу: $latLng")
            // Очищаємо попередні маркери
            googleMap?.clear()

            // Додаємо новий маркер без зміни масштабу
            googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Ви натиснули тут")
            )
        }

        // Логування для перевірки
        Log.d("Fragment_Map", "Map Toolbar Enabled: ${googleMap?.uiSettings?.isMapToolbarEnabled}")
    }

    // Обробка життєвого циклу MapView
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}