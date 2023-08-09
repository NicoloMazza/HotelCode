package com.hotelcode.camera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.HomeFragmentBinding
import com.hotelcode.entity.CameraENT
import com.hotelcode.entity.Riciclabile

class HomeFragment : Fragment() {
    private lateinit var adapterOfferte: Adapter
    private lateinit var adapterConsigliate: Adapter
    private lateinit var adapterPiscina: Adapter
    private lateinit var adapterLussuose: Adapter
    private lateinit var camereOfferte: ArrayList<Riciclabile>
    private lateinit var camereConsigliate: ArrayList<Riciclabile>
    private lateinit var camerePiscina: ArrayList<Riciclabile>
    private lateinit var camereLussuose: ArrayList<Riciclabile>
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)

        camereOfferte = ArrayList()
        aggiungiCamere(1)
        adapterOfferte = Adapter(camereOfferte)
        adapterOfferte.setOnClickListener(object :
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                // Log.i("Prova", "Index ${position+1} - Text ${(model as CameraENT).getNomeCamera()}")
                val intent = Intent(context, VisualizzaCameraActivity::class.java)
                intent.putExtra("CameraENT", model as CameraENT)
                startActivity(intent)
            }
        })
        val recyclerView1 = binding.recyclerView1
        recyclerView1.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView1.adapter = adapterOfferte

        camereConsigliate = ArrayList()
        aggiungiCamere(2)
        adapterConsigliate = Adapter(camereConsigliate)
        adapterConsigliate.setOnClickListener(object :
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                // Log.i("Prova", "Index ${position+1} - Text ${(model as CameraENT).getNomeCamera()}")
                val intent = Intent(context, VisualizzaCameraActivity::class.java)
                intent.putExtra("CameraENT", model as CameraENT)
                startActivity(intent)
            }
        })
        val recyclerView2 = binding.recyclerView2
        recyclerView2.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView2.adapter = adapterConsigliate

        camerePiscina = ArrayList()
        aggiungiCamere(3)
        adapterPiscina = Adapter(camerePiscina)
        adapterPiscina.setOnClickListener(object :
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                // Log.i("Prova", "Index ${position+1} - Text ${(model as CameraENT).getNomeCamera()}")
                val intent = Intent(context, VisualizzaCameraActivity::class.java)
                intent.putExtra("CameraENT", model as CameraENT)
                startActivity(intent)
            }
        })
        val recyclerView6 = binding.recyclerView6
        recyclerView6.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView6.adapter = adapterPiscina

        camereLussuose = ArrayList()
        aggiungiCamere(4)
        adapterLussuose = Adapter(camereLussuose)
        adapterLussuose.setOnClickListener(object :
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                // Log.i("Prova", "Index ${position+1} - Text ${(model as CameraENT).getNomeCamera()}")
                val intent = Intent(context, VisualizzaCameraActivity::class.java)
                intent.putExtra("CameraENT", model as CameraENT)
                startActivity(intent)
            }
        })
        val recyclerView7 = binding.recyclerView7
        recyclerView7.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView7.adapter = adapterLussuose

        return binding.root
    }

    private fun aggiungiCamere(scelta: Int) { // 1 Offerte 2 Consigliate 3 Con Piscina 4 Lussuose
        val parte1 = "SELECT num_camera, camera.ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, piscina, balcone, bagno, tv, wifi, prezzo, path_foto, AVG(punteggio_recensione) as media_recensioni FROM camera, foto_camera, prenotazione WHERE camera.ref_albergo = foto_camera.ref_albergo AND camera.num_camera = foto_camera.ref_camera AND camera.ref_albergo = prenotazione.ref_albergo AND camera.num_camera = prenotazione.ref_camera AND predefinita = 1"
        val parte2 = when (scelta) {
            1 -> " AND in_offerta = 1"
            2 -> ""
            3 -> " AND piscina = 1"
            4 -> " AND prezzo >= 70.0"
            else -> ";"
        }
        val parte3 = " GROUP BY num_camera, camera.ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, piscina, balcone, bagno, tv, wifi, prezzo, path_foto "
        val parte4 = when (scelta) {
            1 -> " ORDER BY prezzo ASC;"
            2 -> " ORDER BY AVG(punteggio_recensione) DESC;"
            3 -> " ORDER BY num_camera;"
            4 -> " ORDER BY prezzo ASC;"
            else -> ";"
        }
        val query = parte1.plus(parte2).plus(parte3).plus(parte4)
        Log.v("query", query)
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERO_DATI_CAMERE_HOME_RICERCA) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val camera = CameraENT(resultSet.get(i) as JsonObject)
                    when (scelta) {
                        1 -> {
                            camereOfferte.add(camera)
                            adapterOfferte.notifyItemInserted(camereOfferte.size - 1)
                        }
                        2 -> {
                            if (camera.getMediaRecensioni() >= 4.6) {
                                camereConsigliate.add(camera)
                                adapterConsigliate.notifyItemInserted(camereOfferte.size - 1)
                            }
                        }
                        3 -> {
                            camerePiscina.add(camera)
                            adapterPiscina.notifyItemInserted(camereOfferte.size - 1)
                        }
                        4 -> {
                            camereLussuose.add(camera)
                            adapterLussuose.notifyItemInserted(camereOfferte.size - 1)
                        }
                    }
                }
            }
        }
    }
}