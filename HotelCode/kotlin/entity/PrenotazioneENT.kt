package com.hotelcode.entity

import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import com.hotelcode.database.MetodiDatabase
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Parcelize
data class PrenotazioneENT(
    private var refUtente: String,
    private var refCamera: Int,
    private var refAlbergo: String,
    private var dataInizio: LocalDate,
    private var dataFine: LocalDate,
    private var checkInEffettivo: LocalTime?,
    private var checkOutEffettivo: LocalTime?,
    private var testoRecensione: String?,
    private var punteggioRecensione: Double?,
    private var pathFotoPredefinita: String,
    private var nomeCamera: String
) : Parcelable, Riciclabile {
    init {
        Log.v("Creazione Entity", "$this creata!")
    }

    fun getRefUtente(): String = refUtente

    fun setRefUtente(value: String) {
        refUtente = value
    }

    fun getRefCamera(): Int = refCamera

    fun setRefCamera(value: Int) {
        refCamera = value
    }

    fun getRefAlbergo(): String = refAlbergo

    fun setRefAlbergo(value: String) {
        refAlbergo = value
    }

    fun getDataInizio(): LocalDate = dataInizio

    fun setDataInizio(value: LocalDate) {
        dataInizio = value
    }

    fun getDataFine(): LocalDate = dataFine

    fun setDataFine(value: LocalDate) {
        dataFine = value
    }

    fun getCheckInEffettivo(): LocalTime? = checkInEffettivo

    fun setCheckInEffettivo(value: LocalTime?) {
        checkInEffettivo = value
    }

    fun getCheckOutEffettivo(): LocalTime? = checkOutEffettivo

    fun setCheckOutEffettivo(value: LocalTime?) {
        checkOutEffettivo = value
    }

    fun getTestoRecensione(): String? = testoRecensione

    fun setTestoRecensione(value: String) {
        testoRecensione = value
    }

    fun getPunteggioRecensione(): Double? = punteggioRecensione

    fun setPunteggioRecensione(value: Double) {
        punteggioRecensione = value
    }


    // Elementi non presenti nella tabella Prenotazione:
    fun getPathFotoPredefinita(): String = pathFotoPredefinita

    fun setPathFotoPredefinita(value: String) {
        pathFotoPredefinita = value
    }

    fun getNomeCamera(): String = nomeCamera

    fun setNomeCamera(value: String) {
        nomeCamera = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("ref_utente")) jsonObject.get("ref_utente").asString else "",
        if (jsonObject.has("ref_camera")) jsonObject.get("ref_camera").asInt else 0,
        if (jsonObject.has("ref_albergo")) jsonObject.get("ref_albergo").asString else "",
        if (jsonObject.has("data_inizio")) LocalDate.parse(jsonObject.get("data_inizio").asString) else LocalDate.now(),
        if (jsonObject.has("data_fine")) LocalDate.parse(jsonObject.get("data_fine").asString) else LocalDate.now(),
        if (jsonObject.has("check_in_effettivo") && !jsonObject.get("check_in_effettivo").isJsonNull)
            LocalTime.parse(jsonObject.get("check_in_effettivo").asString) else null,
        if (jsonObject.has("check_out_effettivo") && !jsonObject.get("check_out_effettivo").isJsonNull)
            LocalTime.parse(jsonObject.get("check_out_effettivo").asString) else null,
        if (jsonObject.has("testo_recensione") && !jsonObject.get("testo_recensione").isJsonNull)
            jsonObject.get("testo_recensione").asString
        else null,
        if (jsonObject.has("punteggio_recensione") && !jsonObject.get("punteggio_recensione").isJsonNull)
            jsonObject.get("punteggio_recensione").asDouble
        else null,
        if (jsonObject.has("path_foto")) jsonObject.get("path_foto").asString else "",
        if (jsonObject.has("nome_camera")) jsonObject.get("nome_camera").asString else ""
    )

    override fun toString(): String = "Prenotazione(refUtente='$refUtente', refCamera=$refCamera, refAlbergo='$refAlbergo', " +
                "dataInizio=$dataInizio, dataFine=$dataFine, check-in=$checkInEffettivo, check-out=$checkOutEffettivo)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        MetodiDatabase.recuperaImmagine(this.getPathFotoPredefinita()) { bitmap, messaggio ->
            Log.v("Messaggio", messaggio)
            if (bitmap != null) {
                if (holder is Adapter.PrenotazioneViewHolder) {
                    holder.immagine.setImageBitmap(bitmap)
                    holder.dataInizio.text = this.getDataInizio().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    holder.dataFine.text = this.getDataFine().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    holder.nomeCamera.text = this.getNomeCamera()
                }
            }
        }
    }
}