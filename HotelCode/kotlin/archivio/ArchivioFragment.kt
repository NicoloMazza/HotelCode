package com.hotelcode.archivio

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
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.databinding.ArchivioFragmentBinding
import com.hotelcode.entity.PrenotazioneENT
import com.hotelcode.entity.Riciclabile

class ArchivioFragment : Fragment() {
    private lateinit var adapterPassate: Adapter
    private lateinit var adapterPresenti: Adapter
    private lateinit var adapterFuture: Adapter
    private lateinit var prenotazioniPassate: ArrayList<Riciclabile>
    private lateinit var prenotazioniPresenti: ArrayList<Riciclabile>
    private lateinit var prenotazioniFuture: ArrayList<Riciclabile>
    private lateinit var binding: ArchivioFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ArchivioFragmentBinding.inflate(inflater, container, false)

        prenotazioniPresenti = ArrayList()
        aggiungiPrenotazioni(2)
        adapterPresenti = Adapter(prenotazioniPresenti)
        adapterPresenti.setOnClickListener(object:
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                val intent = Intent(context, VisualizzaPrenotazioneActivity::class.java)
                intent.putExtra("tipoPrenotazione", 2) // Presente
                intent.putExtra("PrenotazioneENT", model as PrenotazioneENT)
                startActivity(intent)
                // Log.i("Prova", "Index ${position+1} - Text ${(model as PrenotazioneENT).getNomeCamera()}")
            }
        })
        val recyclerView1 = binding.recyclerView1
        recyclerView1.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView1.adapter = adapterPresenti

        prenotazioniFuture = ArrayList()
        aggiungiPrenotazioni(3)
        adapterFuture = Adapter(prenotazioniFuture)
        adapterFuture.setOnClickListener(object:
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                val intent = Intent(context, VisualizzaPrenotazioneActivity::class.java)
                intent.putExtra("tipoPrenotazione", 3) // Futura
                intent.putExtra("PrenotazioneENT", model as PrenotazioneENT)
                startActivity(intent)
                // Log.i("Prova", "Index ${position+1} - Text ${(model as PrenotazioneENT).getNomeCamera()}")
            }
        })
        val recyclerView2 = binding.recyclerView2
        recyclerView2.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView2.adapter = adapterFuture

        prenotazioniPassate = ArrayList()
        aggiungiPrenotazioni(1)
        adapterPassate = Adapter(prenotazioniPassate)
        adapterPassate.setOnClickListener(object:
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                val intent = Intent(context, VisualizzaPrenotazioneActivity::class.java)
                intent.putExtra("tipoPrenotazione", 1) // Passata
                intent.putExtra("PrenotazioneENT", model as PrenotazioneENT)
                startActivity(intent)
                // Log.i("Prova", "Index ${position+1} - Text ${(model as PrenotazioneENT).getNomeCamera()}")
            }
        })
        val recyclerView3 = binding.recyclerView3
        recyclerView3.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView3.adapter = adapterPassate

        return binding.root
    }

    private fun aggiungiPrenotazioni(scelta: Int) { // 1 Passate 2 Presenti 3 Future
        val utente = MetodiUtili.recuperaUtenteShared(context)
        if (utente != null) {
            val query =
                "SELECT ref_utente, prenotazione.ref_camera, prenotazione.ref_albergo, data_inizio, data_fine, check_in_effettivo, check_out_effettivo, testo_recensione, punteggio_recensione, path_foto, nome_camera FROM prenotazione, camera, foto_camera WHERE prenotazione.ref_camera = camera.num_camera AND prenotazione.ref_albergo = camera.ref_albergo AND camera.ref_albergo = foto_camera.ref_albergo AND camera.num_camera = foto_camera.ref_camera AND predefinita = 1 AND ref_utente='${utente.getUsername()}'"
            val filtro = when (scelta) {
                1 -> " AND data_fine < CURDATE() "
                2 -> " AND data_inizio <= CURDATE() AND data_fine >= CURDATE() "
                3 -> " AND data_inizio > CURDATE() "
                else -> " "
            }
            MetodiDatabase.eseguiSelect(query + filtro + "ORDER BY data_inizio", MetodiDatabase.SELECT_RECUPERO_PRENOTAZIONI) { resultSet, messaggio ->
                Log.v("Messaggio", messaggio)
                if (resultSet != null) {
                    for (i in 0 until resultSet.size()) {
                        val prenotazione = PrenotazioneENT(resultSet.get(i) as JsonObject)
                        when (scelta) {
                            1 -> {
                                prenotazioniPassate.add(prenotazione)
                                adapterPassate.notifyItemInserted(prenotazioniPassate.size - 1)
                            }
                            2 -> {
                                prenotazioniPresenti.add(prenotazione)
                                adapterPresenti.notifyItemInserted(prenotazioniPresenti.size - 1)
                            }
                            3 -> {
                                prenotazioniFuture.add(prenotazione)
                                adapterFuture.notifyItemInserted(prenotazioniFuture.size - 1)
                            }
                        }
                    }
                }
            }
        }
    }
}