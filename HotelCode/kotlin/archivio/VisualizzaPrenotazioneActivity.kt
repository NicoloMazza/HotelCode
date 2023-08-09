package com.hotelcode.archivio

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.google.gson.JsonObject
import com.hotelcode.autenticazione.PrincipaleActivity
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.VisualizzaPrenotazioneActivityBinding
import com.hotelcode.entity.AlbergoENT
import com.hotelcode.entity.PrenotazioneENT
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class VisualizzaPrenotazioneActivity : AppCompatActivity() {
    private lateinit var binding: VisualizzaPrenotazioneActivityBinding
    private var statoCampo: Boolean = false
    private lateinit var prenotazione: PrenotazioneENT
    private lateinit var albergo: AlbergoENT
    private var statoSpinner = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VisualizzaPrenotazioneActivityBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)

        binding.caratteri.text = "Caratteri: 0/400"

        binding.recensione.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.caratteri.text = "Caratteri: ${binding.recensione.text.length}/400"
                if (binding.recensione.text.length in 1..400) {
                    statoCampo = true
                    binding.caratteri.setTextColor(Color.parseColor("#003566"))
                } else {
                    binding.caratteri.setTextColor(Color.RED)
                    statoCampo = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        val a = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PrenotazioneENT", PrenotazioneENT::class.java)
        } else {
            intent.getParcelableExtra<PrenotazioneENT>("PrenotazioneENT")
        }
        if (a != null) {
            prenotazione = a
            if(prenotazione.getTestoRecensione()!= null) {
                binding.recensione.setText(prenotazione.getTestoRecensione())
            }
        }

        binding.campoNomeCamera.text = prenotazione.getNomeCamera()
        MetodiDatabase.recuperaImmagine(prenotazione.getPathFotoPredefinita()) { bitmap, _ ->
            binding.campoFotoCamera.setImageBitmap(bitmap)
        }
        binding.campoDataInizio.setText(
            prenotazione.getDataInizio().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )
        binding.campoDataFine.setText(
            prenotazione.getDataFine().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        )

        val query = "SELECT * FROM albergo"
        MetodiDatabase.eseguiSelect(
            query,
            MetodiDatabase.SELECT_RECUPERO_DATI_ALBERGO
        ) { resultSet, _ ->
            if (resultSet != null) {
                albergo = AlbergoENT(resultSet.get(0) as JsonObject)
                binding.nota.text = "NOTA:\nSara' possibile effettuare il check-in/check-out a partire dalle ${albergo.getInizioCheck()} del giorno precedente fino alle ${albergo.getFineCheck()} del giorno di inizio/fine della prenotazione.\nE' inoltre necessario scrivere una breve recensione per effettuare il check-out."
            }
        }

        binding.bottoneCheckin.setOnClickListener {
            if (prenotazione.getCheckInEffettivo() == null) {
                 if (LocalDate.now() in prenotazione.getDataInizio().minusDays(1)..prenotazione.getDataInizio() &&
                         LocalTime.now() in albergo.getInizioCheck()..albergo.getFineCheck()) {
                     eseguiCheckIn()
                 }
                 else {
                     MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Non è l'orario giusto per fare il check-out!")
                 }
            } else {
                MetodiUtili.creaToast(
                    this@VisualizzaPrenotazioneActivity,
                    "Hai già effettuato il check-in alle ${prenotazione.getCheckInEffettivo()}!"
                )
            }
        }
        binding.bottoneCheckout.setOnClickListener {
            if (prenotazione.getCheckOutEffettivo() == null) {
                if (prenotazione.getTestoRecensione() != null && prenotazione.getPunteggioRecensione() != null) {
                    if (LocalDate.now() in prenotazione.getDataFine().minusDays(1)..prenotazione.getDataFine() &&
                        LocalTime.now() in albergo.getInizioCheck()..albergo.getFineCheck()) {
                        eseguiCheckOut()
                    }
                    else {
                        MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Non è l'orario giusto per fare il check-out!")
                    }
                }
                else {
                    MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Devi prima scrivere una recensione!")
                }
            }
            else {
                MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Hai già effettuato il check-out alle ${prenotazione.getCheckOutEffettivo()}!")
            }
        }
        binding.button2.setOnClickListener {
            if (prenotazione.getCheckOutEffettivo() == null) {
                inserisciRecensione()
            }
            else {
                MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Non puoi modificare la recensione dopo aver effettuato il check-out!")
            }
        }
        binding.bottoneElimina.setOnClickListener {
            MetodiUtili.creaAlertDialog(
                this,
                "Elimina prenotazione",
            "Sei sicuro di voler eliminare la prenotazione? Non avrai diritto ad alcun rimborso!",
                {eliminaPrenotazione()}, {}
            )
        }

        gestisciPrenotazione()

        val spinner = binding.punteggioRecensione
        val elements = if (prenotazione.getCheckOutEffettivo() == null) listOf("1⭐️", "1.5⭐️", "2⭐️", "2.5⭐️", "3⭐️", "3.5⭐️", "4⭐️", "4.5⭐️", "5⭐️") else listOf("${prenotazione.getPunteggioRecensione()}⭐️")
        val adapter = ArrayAdapter(
            this@VisualizzaPrenotazioneActivity,
            android.R.layout.simple_spinner_item,
            elements
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position)
                if (selectedItem is String) {
                    statoSpinner = (selectedItem.substring(0, selectedItem.length - 2)).toDouble()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun gestisciPrenotazione() {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            when (extras.getInt("tipoPrenotazione")) {
                1 -> {
                    Log.v("when", "E' una prenotazione passata")
                    binding.bottoneCheckin.isEnabled = false
                    binding.bottoneCheckout.isEnabled = false
                    binding.recensione.isEnabled = false
                    binding.punteggioRecensione.isEnabled = false
                    binding.button2.isEnabled = false
                    binding.recensione.setText(prenotazione.getTestoRecensione())
                    binding.bottoneElimina.isEnabled = false
                    binding.bottoneElimina.setBackgroundColor(Color.parseColor("#D0B6B3B3"))
                }
                2 -> {
                    Log.v("when", "E' una prenotazione presente")
                    binding.bottoneElimina.isEnabled = false
                    binding.bottoneElimina.setBackgroundColor(Color.parseColor("#D0B6B3B3"))
                }
                3 -> {
                    Log.v("when", "E' una prenotazione futura")
                }
                else -> Log.v("when", "E' una prenotazione sconosciuta")
            }
        }
    }

    private fun eseguiCheckIn() {
        val orarioCorrente = LocalTime.now()
        val query =
            "UPDATE prenotazione SET check_in_effettivo = '${orarioCorrente}' WHERE ref_utente = '${prenotazione.getRefUtente()}' AND ref_camera = '${prenotazione.getRefCamera()}' AND ref_albergo = '${prenotazione.getRefAlbergo()}' AND data_inizio = '${prenotazione.getDataInizio()}';"
        MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_CHECK_IN) { messaggio ->
            MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, messaggio)
        }
        prenotazione.setCheckInEffettivo(orarioCorrente)
        binding.bottoneCheckin.isEnabled = false
    }

    private fun eseguiCheckOut() {
        val orarioCorrente = LocalTime.now()
        val query =
            "UPDATE prenotazione SET check_out_effettivo = '${orarioCorrente}' WHERE ref_utente = '${prenotazione.getRefUtente()}' AND ref_camera = '${prenotazione.getRefCamera()}' AND ref_albergo = '${prenotazione.getRefAlbergo()}' AND data_inizio = '${prenotazione.getDataInizio()}';"
        MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_CHECK_OUT) { messaggio ->
            MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, messaggio)
        }
        prenotazione.setCheckOutEffettivo(orarioCorrente)
        binding.bottoneCheckout.isEnabled = false
    }

    private fun inserisciRecensione() {
        if (statoCampo) {
            aggiornaPrenotazione()
        }
        else {
            MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, "Devi prima scrivere il testo!")
        }
    }

    private fun aggiornaPrenotazione() {
        Log.v("1)", "aggiornaPrenotazione")
        val recensione = binding.recensione.text.toString()
        val punteggio = statoSpinner
        val query = "UPDATE prenotazione SET testo_recensione = '${MetodiUtili.pulisciStrigna(recensione)}', punteggio_recensione = '${punteggio}' WHERE ref_utente = '${prenotazione.getRefUtente()}' AND ref_camera = '${prenotazione.getRefCamera()}' AND ref_albergo = '${prenotazione.getRefAlbergo()}' AND data_inizio = '${prenotazione.getDataInizio()}';"
        MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_RECENSIONE) { messaggio ->
            MetodiUtili.creaToast(this@VisualizzaPrenotazioneActivity, messaggio)
        }
        prenotazione.setTestoRecensione(recensione)
        prenotazione.setPunteggioRecensione(punteggio)
    }

    private fun eliminaPrenotazione() {
        val query = "DELETE from prenotazione WHERE ref_utente = '${prenotazione.getRefUtente()}' AND ref_camera = '${prenotazione.getRefCamera()}' AND ref_albergo = '${prenotazione.getRefAlbergo()}' AND data_inizio = '${prenotazione.getDataInizio()}';"
        MetodiDatabase.eseguiDelete(query, 1) { messaggio ->
            MetodiUtili.creaToast(this, messaggio)
        }
        val intent = Intent(this, PrincipaleActivity::class.java)
        startActivity(intent)
    }
}