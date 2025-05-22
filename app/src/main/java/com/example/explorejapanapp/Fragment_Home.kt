package com.example.explorejapanapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class Fragment_Home : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим блоки по их ID
        val tokyoCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.tokyo_card)
        val osakaCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.osaka_card)
        val kyotoCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.kyoto_card)
        val hiroshimaCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.hiroshima_card)
        val sapporoCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.sapporo_card)
        val fukuokaCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.fukuoka_card)
        val nagoyaCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.nagoya_card)
        val niigataCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.niigata_card)
        val sendaiCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.sendai_card)
        val nahaCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.nara_card)
        val allCard = view.findViewById<androidx.cardview.widget.CardView>(R.id.all_card)

        // Обработка нажатий на блоки
        tokyoCard.setOnClickListener { replaceFragment(City_Tokyo()) }
        osakaCard.setOnClickListener { replaceFragment(City_Osaka()) }
        kyotoCard.setOnClickListener { replaceFragment(City_Kyoto()) }
        hiroshimaCard.setOnClickListener { replaceFragment(City_Hiroshima()) }
        sapporoCard.setOnClickListener { replaceFragment(City_Sapporo()) }
        fukuokaCard.setOnClickListener { replaceFragment(City_Fukuoka()) }
        nagoyaCard.setOnClickListener { replaceFragment(City_Nagoya()) }
        niigataCard.setOnClickListener { replaceFragment(City_Niigata()) }
        sendaiCard.setOnClickListener { replaceFragment(City_Sendai()) }
        nahaCard.setOnClickListener { replaceFragment(City_Naha()) }
        allCard.setOnClickListener { replaceFragment(City_All()) }
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

}