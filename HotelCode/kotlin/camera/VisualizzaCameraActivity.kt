package com.hotelcode.camera

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.hotelcode.R
import com.hotelcode.comuni.Adapter
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.VisualizzaCameraActivityBinding
import com.hotelcode.entity.*
import java.time.LocalDate
import java.util.Calendar

class VisualizzaCameraActivity : AppCompatActivity() {
    private lateinit var binding: VisualizzaCameraActivityBinding
    private var dataInizio = LocalDate.now().toString()
    private var dataFine = LocalDate.now().toString()
    private val currentDate = Calendar.getInstance().timeInMillis.plus(86400000)
    private lateinit var fotoCamera: ArrayList<Riciclabile>
    private lateinit var adapterCamera: Adapter
    private lateinit var recensioni: ArrayList<Riciclabile>
    private lateinit var adapterRecensioni: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = VisualizzaCameraActivityBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)

        val camera = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.v("Par", "siamo qua")
            intent.getParcelableExtra("CameraENT", CameraENT::class.java)
        } else {
            intent.getParcelableExtra<CameraENT>("CameraENT")
        }
        if (camera != null) {
            binding.campoNomeCamera.text = camera.getNomeCamera()
            binding.campoDescrizione.text = camera.getDescrizione()
            binding.campoNumeroLetti.text = "Numero letti: ${camera.getNumLetti()}"
            binding.textView17.text = "Accesso disabili: ${if (camera.hasAccessoDisabili()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView18.text = "Accesso animali: ${if (camera.hasAccessoAnimali()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView19.text = "Balcone: ${if (camera.hasBalcone()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView20.text = "Bagno: ${if (camera.hasBagno()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView21.text = "Televisione: ${if (camera.hasTv()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView22.text = "Wi-Fi: ${if (camera.hasWifi()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            binding.textView23.text = "Piscina: ${if (camera.hasPiscinaPrivata()) getString(R.string.emoticon_pallino_true) else getString(R.string.emoticon_pallino_false)}"
            fotoCamera = ArrayList()
            recuperaImmagini(camera)
            adapterCamera = Adapter(fotoCamera)
            val recyclerView1 = binding.recyclerFotoCamera
            recyclerView1.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView1.adapter = adapterCamera
            recensioni = ArrayList()
            recuperaRecensioni(camera)
            adapterRecensioni = Adapter(recensioni)
            val recyclerView2 = binding.recyclerRecensioni
            recyclerView2.layoutManager = LinearLayoutManager(this)
            recyclerView2.adapter = adapterRecensioni

            binding.button.setOnClickListener {
                val query = "SELECT * FROM prenotazione WHERE ref_camera = '${camera.getNumCamera()}' AND ((data_inizio <= '${dataInizio}' AND data_fine >= '${dataInizio}') OR (data_inizio >= '${dataInizio}' AND data_fine <= '${dataFine}') OR (data_inizio >= '${dataInizio}' AND data_inizio <= '${dataFine}' AND data_fine >= '${dataFine}'));"
                MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_VERIFICA_GIORNI) { resultSet, _ ->
                    if (resultSet == null) {
                        if (dataInizio <= dataFine) {
                            val intent = Intent(this, PagamentoActivity::class.java)
                            intent.putExtra("CameraENT", camera)
                            intent.putExtra("dataInizio", dataInizio)
                            intent.putExtra("dataFine", dataFine)
                            startActivity(intent)
                        }
                        else {
                            MetodiUtili.creaToast(this, "La data di inizio deve essere minore (o uguale) della data fine!")
                        }
                    }
                    else {
                        val prenotazioneIncriminata = PrenotazioneENT(resultSet.get(0) as JsonObject)
                        MetodiUtili.creaToast(this, "La camera è prenotata (almeno) dal ${prenotazioneIncriminata.getDataInizio()} al ${prenotazioneIncriminata.getDataFine()}")
                    }
                }
            }
        }

        val calendarView1 = binding.calendarView1
        calendarView1.minDate = currentDate

        calendarView1.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val a = if (month+1<=9) "0" else ""
            val b = if (dayOfMonth<=9) "0" else ""
            dataInizio = year.toString() + "-" + a + (month+1).toString() + "-" + b + dayOfMonth.toString()
            println(dataInizio)
        }

        val calendarView2 = binding.calendarView2
        calendarView2.minDate = currentDate

        calendarView2.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val a = if (month+1<=9) "0" else ""
            val b = if (dayOfMonth<=9) "0" else ""
            dataFine = year.toString() + "-" + a + (month+1).toString() + "-" + b + dayOfMonth.toString()
            println(dataFine)
        }
    }

    private fun recuperaImmagini(camera: CameraENT) {
        val query = "SELECT path_foto FROM foto_camera WHERE ref_camera = '${camera.getNumCamera()}' AND ref_albergo = '${camera.getRefAlbergo()}';"
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERO_FOTO_CAMERA_VISUALIZZA) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val foto = FotoCamera(resultSet.get(i) as JsonObject)
                    fotoCamera.add(foto)
                    adapterCamera.notifyItemInserted(fotoCamera.size - 1)
                }
            }
        }
    }

    private fun recuperaRecensioni(camera: CameraENT) {
        val query = "SELECT * FROM prenotazione WHERE ref_camera = '${camera.getNumCamera()}' AND ref_albergo = '${camera.getRefAlbergo()}' AND testo_recensione IS NOT NULL AND punteggio_recensione IS NOT NULL ORDER BY punteggio_recensione DESC;"
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERA_RECENSIONI) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val recensione = RecensioneENT(resultSet.get(i) as JsonObject)
                    recensioni.add(recensione)
                    adapterRecensioni.notifyItemInserted(recensioni.size - 1)
                }
            }
        }
    }
}