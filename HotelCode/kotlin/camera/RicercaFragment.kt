package com.hotelcode.camera

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.RicercaFragmentBinding
import com.hotelcode.entity.CameraENT
import com.hotelcode.entity.Riciclabile

class RicercaFragment : Fragment() {
    private lateinit var binding: RicercaFragmentBinding
    private var statoSpinner = 1
    private var statoCheckBox = arrayOf(0, 0, 0, 0, 0, 0, 0)
    private lateinit var adapter: Adapter
    private lateinit var camere: ArrayList<Riciclabile>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = RicercaFragmentBinding.inflate(inflater, container, false)
        impostaListener()
        camere = ArrayList()
        adapter = Adapter(camere)
        adapter.setOnClickListener(object :
            Adapter.OnClickListener {
            override fun onClick(position: Int, model: Riciclabile) {
                val intent = Intent(context, VisualizzaCameraActivity::class.java)
                intent.putExtra("CameraENT", model as CameraENT)
                startActivity(intent)
            }
        })
        binding.recyclercamere.layoutManager = LinearLayoutManager(context)
        binding.recyclercamere.adapter = adapter
        return binding.root
    }

    private fun impostaListener() {
        val spinner = binding.spinner
        val elements = listOf("1", "2", "3", "4", "5")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, elements)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.checkDisabili.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[0] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkAnimali.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[1] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkBagno.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[2] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkBalcone.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[3] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkPiscina.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[4] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkTelevisore.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[5] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.checkWiFi.setOnCheckedChangeListener { _, isChecked ->
            statoCheckBox[6] = if (isChecked) 1 else 0
            recuperaCamere()
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position)
                if (selectedItem is String) {
                    statoSpinner = selectedItem.toInt()
                }
                recuperaCamere()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Questo metodo viene chiamato quando nessun elemento è selezionato.
            }
        }
    }

    private fun recuperaCamere() {
        var query = "SELECT num_camera, camera.ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, piscina, balcone, bagno, tv, wifi, prezzo, path_foto, AVG(punteggio_recensione) as media_recensioni FROM camera, foto_camera, prenotazione WHERE camera.ref_albergo = foto_camera.ref_albergo AND camera.num_camera = foto_camera.ref_camera AND camera.ref_albergo = prenotazione.ref_albergo AND camera.num_camera = prenotazione.ref_camera AND predefinita = 1 AND num_letti >= '${statoSpinner}' "
        if (statoCheckBox[0] == 1) {
            query += "AND accesso_disabili = 1 "
        }
        if (statoCheckBox[1] == 1) {
            query += "AND accesso_animali = 1 "
        }
        if (statoCheckBox[2] == 1) {
            query += "AND bagno = 1 "
        }
        if (statoCheckBox[3] == 1) {
            query += "AND balcone = 1 "
        }
        if (statoCheckBox[4] == 1) {
            query += "AND piscina = 1 "
        }
        if (statoCheckBox[5] == 1) {
            query += "AND tv = 1 "
        }
        if (statoCheckBox[6] == 1) {
            query += "AND wifi = 1 "
        }
        query += "GROUP BY num_camera, camera.ref_albergo, nome_camera, descrizione, num_letti, accesso_disabili, accesso_animali, in_offerta, piscina, balcone, bagno, tv, wifi, prezzo, path_foto ORDER BY prezzo ASC;"
        Log.v("query", query)
        camere.clear()
        MetodiDatabase.eseguiSelect(
            query,
            MetodiDatabase.SELECT_RECUPERO_DATI_CAMERE_HOME_RICERCA
        ) { resultSet, messaggio ->
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val camera = CameraENT(resultSet.get(i) as JsonObject)
                    camere.add(camera)
                    adapter.notifyDataSetChanged()
                }
            }
            else {
                MetodiUtili.creaToast(context, messaggio)
                adapter.notifyDataSetChanged()
            }
        }
    }
}