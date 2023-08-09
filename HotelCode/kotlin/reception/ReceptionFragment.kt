package com.hotelcode.reception

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.databinding.ReceptionFragmentBinding
import com.hotelcode.entity.AlbergoENT
import java.time.format.DateTimeFormatter

class ReceptionFragment : Fragment() {
    private lateinit var binding: ReceptionFragmentBinding
    private var statoCampo: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ReceptionFragmentBinding.inflate(inflater, container, false)
        recuperaDatiAlbergo()
        binding.textViewCaratteriInseriti.text = "Caratteri: 0/400"
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.textViewCaratteriInseriti.text = "Caratteri: ${binding.editText.text.length}/400"
                if (binding.editText.text.length in 1.. 400) {
                    statoCampo = true
                    binding.textViewCaratteriInseriti.setTextColor(Color.parseColor("#003566"))
                }
                else {
                    binding.textViewCaratteriInseriti.setTextColor(Color.RED)
                    statoCampo = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        return binding.root
    }

    private fun recuperaDatiAlbergo() {
        val query =
            "SELECT * FROM albergo, foto_albergo WHERE albergo.indirizzo = foto_albergo.ref_albergo AND indirizzo = 'Via del tutto eccezionale, 42 - Pietrammare 81009, Italia';"
        MetodiDatabase.eseguiSelect(query, 4) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                val albergo = AlbergoENT((resultSet.get(0)) as JsonObject)
                binding.nomeHotel.text = albergo.getNome()
                binding.viaHotel.text = albergo.getIndirizzo()
                binding.viaHotel.setOnClickListener {
                    val loc = albergo.getCoordinate()
                    val myUri = Uri.parse("https://www.google.it/maps/search/$loc")
                    val intent = Intent(Intent.ACTION_VIEW, myUri)
                    startActivity(intent)
                }
                binding.telefono.text = albergo.getTelefono()
                binding.email.text = albergo.getEmail()
                binding.sitoWeb.text = albergo.getSito()
                binding.sitoWeb.setOnClickListener {
                    val myUri = Uri.parse(albergo.getSito())
                    val intent = Intent(Intent.ACTION_VIEW, myUri)
                    startActivity(intent)
                }
                binding.orariCheck.text = "Check-in: ${albergo.getInizioCheck().format(DateTimeFormatter.ofPattern("HH:mm"))} - ${albergo.getFineCheck().format(DateTimeFormatter.ofPattern("HH:mm"))}\nCheck-out: ${albergo.getInizioCheck().format(DateTimeFormatter.ofPattern("HH:mm"))} - ${albergo.getFineCheck().format(DateTimeFormatter.ofPattern("HH:mm"))}"
                val listaImmagini = HashMap<String, String>()
                for (i in 0 until resultSet.size()) {
                    val path = if ((resultSet.get(i) as JsonObject).has("path_foto")) {
                        (resultSet.get(i) as JsonObject).get("path_foto").asString
                    } else ""
                    val descrizione = if ((resultSet.get(i) as JsonObject).has("descrizione_foto")) {
                        (resultSet.get(i) as JsonObject).get("descrizione_foto").asString
                    } else ""
                    listaImmagini[path] = descrizione
                }
                binding.bottone.setOnClickListener {
                    if (binding.editText.text.isEmpty()) {
                        MetodiUtili.creaToast(context, "Scrivi prima la segnalazione!")
                    } else {
                        inviaSegnalazione(binding.editText.text.toString())
                    }
                }
                recuperaImmagini(listaImmagini)
            }
        }
    }

    private fun recuperaImmagini(immagini: Map<String, String>) {
        val chiavi =  immagini.keys
        for (chiave in chiavi) {
            MetodiDatabase.recuperaImmagine(chiave) { avatar, messaggio ->
                Log.v("Messaggio", messaggio)
                when (chiave) {
                    "media/images/hotelcode/foto_albergo/ingresso.png" -> {
                        binding.campoFotoAlbergo.setImageBitmap(avatar)
                        binding.descrizioneAlbergo.text = immagini[chiave]
                    } // ingresso
                    "media/images/hotelcode/foto_albergo/mappa_edificio.png" -> {
                        binding.fotoPianta.setImageBitmap(avatar)
                        binding.descrizionePianta.text = immagini[chiave]
                    } // mappa
                    "media/images/hotelcode/foto_albergo/palestra.png" -> {
                        binding.fotoPalestra.setImageBitmap(avatar)
                        binding.descrizionePalestra.text = immagini[chiave]
                    } // palestra
                    "media/images/hotelcode/foto_albergo/piscina.png" -> {
                        binding.fotoPiscina.setImageBitmap(avatar)
                        binding.descrizionePiscina.text = immagini[chiave]
                    } // piscina
                    "media/images/hotelcode/foto_albergo/reception.png" -> {
                        binding.fotoReception.setImageBitmap(avatar)
                        binding.infoContatti.text = immagini[chiave]
                    } // reception
                }
            }
        }
    }

    private fun inviaSegnalazione(segnalazione: String) {
        if (statoCampo) {
            val utente = MetodiUtili.recuperaUtenteShared(context)
            if (utente != null) {
                val query =
                    "INSERT INTO segnalazione(ref_utente, testo) VALUES (\"${utente.getUsername()}\", \"${
                        MetodiUtili.pulisciStrigna(segnalazione)
                    }\");"
                MetodiDatabase.eseguiInsert(query, 1) { messaggio ->
                    MetodiUtili.creaToast(context, messaggio)
                }
            }
        }
    }
}