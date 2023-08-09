package com.hotelcode.camera

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import com.google.gson.JsonObject
import com.hotelcode.autenticazione.PrincipaleActivity
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.PagamentoActivityBinding
import com.hotelcode.entity.CameraENT
import com.hotelcode.entity.PromozioneENT
import com.hotelcode.entity.UtenteENT
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class PagamentoActivity : AppCompatActivity() {
    private var codice = MetodiUtili.generaHash(System.currentTimeMillis().toString()).substring(0, 8)
    private var promozioniApplicabili = ArrayList<PromozioneENT>()
    private var gruppoMostrato = 0  // Resoconto
    private lateinit var utente: UtenteENT
    private lateinit var camera: CameraENT
    private lateinit var binding: PagamentoActivityBinding
    private var totaleDaPagare = 0.0
    private var sconto = 0.0
    private var promozioneApplicata: PromozioneENT = PromozioneENT(0, 0.0, 0, speciale = false, false, null)

    init {
        Log.v("Codice generato:", codice)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PagamentoActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val a = MetodiUtili.recuperaUtenteShared(this)
        if (a != null) {
            utente = a
            binding.finaleCarta.text = "**** **** **** **${utente.getNumCarta().takeLast(2)}"

            val b = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.v("Par", "siamo qua")
                intent.getParcelableExtra("CameraENT", CameraENT::class.java)
            } else {
                intent.getParcelableExtra<CameraENT>("CameraENT")
            }
            if (b != null) {
                camera = b
                val dataInizio = LocalDate.parse(intent.getStringExtra("dataInizio").toString())
                val dataFine = LocalDate.parse(intent.getStringExtra("dataFine").toString())
                binding.giorni.text =
                    if (dataInizio == dataFine) "Giorno: $dataInizio" else "Da: ${dataInizio}\nA: $dataFine"

                totaleDaPagare = camera.getPrezzo() * (ChronoUnit.DAYS.between(dataInizio, dataFine) + 1)
                binding.totale.text = "${totaleDaPagare}€"

                binding.invia.setOnClickListener {
                    when (gruppoMostrato) {
                        0 -> procediPagamento()
                        1 -> verificaCodice()
                    }
                }
            }
        }

        val radioGroup = binding.gruppo
        val query = "SELECT * FROM promozione WHERE ref_utente = '${utente.getUsername()}' AND usata = 0;"
        MetodiDatabase.eseguiSelect(
            query,
            MetodiDatabase.SELECT_RECUPERA_PROMOZIONI
        ) { resultSet, _ ->
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val promozione = PromozioneENT(resultSet.get(i) as JsonObject)
                    promozioniApplicabili.add(promozione)
                    val radioButton = RadioButton(this)
                    radioButton.tag = "${promozione.getCodicePromozione()}"
                    radioButton.text =
                        "${i + 1}) Codice: ${promozione.getCodicePromozione()} - Sconto: ${promozione.getImporto()}€"
                    radioButton.setTextColor(Color.parseColor("#F9EFE1"))
                    radioGroup.addView(radioButton)
                }
            }
        }

        radioGroup.setOnCheckedChangeListener { _, selezionato ->
            val selectedRadioButton = findViewById<RadioButton>(selezionato)
            val selectedOption = promozioniApplicabili.find { promozione ->
                promozione.getCodicePromozione() == selectedRadioButton.tag?.toString()?.toIntOrNull()
            }
            Log.w("è stata cliccata", selectedOption.toString())
            if (selectedOption != null) {
                promozioneApplicata = selectedOption
                sconto = selectedOption.getImporto()
                Log.v("lo sconto da applicare è", sconto.toString())
                Log.v("il nuovo totale è", (totaleDaPagare - sconto).toString())
                binding.totale.text = (totaleDaPagare - sconto).toString() + "€"
            }
        }
    }

    private fun procediPagamento() {
        if (utente.getImportoCarta() < totaleDaPagare - sconto) {
            MetodiUtili.creaToast(this, "Saldo insufficiente!")
            val i = Intent(this, VisualizzaCameraActivity::class.java)
            i.putExtra("CameraENT", camera)
            startActivity(i)
        } else {
            gruppoMostrato = 1
            binding.gruppoResoconto.visibility = View.INVISIBLE
            binding.gruppoConferma.visibility = View.VISIBLE
            MetodiUtili.creaNotifica(this, "Autorizza Pagamento", "Il codice da inserire è: ${codice}.")
            Log.w("CODICE:", codice)
        }
    }

    private fun verificaCodice() {
        if (binding.codiceSicurezza.text.toString() == codice) {
            MetodiUtili.creaToast(this, "Pagamento effettuato con successo!")
            val puntiCorrenti = utente.getPuntiCorrenti() + ((totaleDaPagare - sconto)/ 4).roundToInt()
            var query = "UPDATE utente SET punti_correnti = '${puntiCorrenti}', importo_carta = '${utente.getImportoCarta() - totaleDaPagare + sconto}' WHERE username = '${utente.getUsername()}';"
            MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_PAGAMENTO_UTENTE) { messaggio ->
                MetodiUtili.creaToast(this@PagamentoActivity, messaggio)
            }
            utente.setImportoCarta(utente.getImportoCarta() - totaleDaPagare + sconto)
            utente.setPuntiCorrenti(puntiCorrenti)
            MetodiUtili.inserisciUtenteShared(this, utente)
            if (promozioneApplicata.getCodicePromozione() != 0) {
                query =
                    "UPDATE promozione SET usata = 1 WHERE cod_promozione = '${promozioneApplicata.getCodicePromozione()}';"
                MetodiDatabase.eseguiUpdate(
                    query,
                    MetodiDatabase.UPDATE_PAGAMENTO_PROMOZIONE
                ) { messaggio ->
                    MetodiUtili.creaToast(this@PagamentoActivity, messaggio)
                }
                promozioneApplicata.setSpesa(true)
            }
            query = "INSERT INTO prenotazione (ref_utente, ref_camera, ref_albergo, data_inizio, data_fine) VALUES ('${utente.getUsername()}', '${camera.getNumCamera()}', '${camera.getRefAlbergo()}', '${LocalDate.parse(intent.getStringExtra("dataInizio").toString())}', '${LocalDate.parse(intent.getStringExtra("dataFine").toString())}');"
            MetodiDatabase.eseguiInsert(query, MetodiDatabase.INSERT_PRENOTAZIONE) { messaggio ->
                MetodiUtili.creaToast(this, messaggio)
            }
            val i = Intent(this, PrincipaleActivity::class.java)
            startActivity(i)
        } else {
            MetodiUtili.creaToast(this, "Codice di sicurezza non valido!")
            codice = MetodiUtili.generaHash(System.currentTimeMillis().toString()).substring(0, 8)
            MetodiUtili.creaNotifica(this, "Autorizza Pagamento", "Il codice da inserire è: ${codice}.")
            Log.w("CODICE:", codice)
        }
    }
}