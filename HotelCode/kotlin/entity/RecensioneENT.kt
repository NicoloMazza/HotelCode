package com.hotelcode.entity

import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecensioneENT(
    private var refUtente: String,
    private var refCamera: Int,
    private var refAlbergo: String,
    private var testoRecensione: String?,
    private var punteggioRecensione: Double?,
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

    fun getRefTestoRecensione(): String? = testoRecensione

    fun setRefTestoRecensione(value: String) {
        testoRecensione = value
    }

    fun getRefPunteggioRecensione(): Double? = punteggioRecensione

    fun setRefPunteggioRecensione(value: Double) {
        punteggioRecensione = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("ref_utente")) jsonObject.get("ref_utente").asString else "",
        if (jsonObject.has("ref_camera")) jsonObject.get("ref_camera").asInt else 0,
        if (jsonObject.has("ref_albergo")) jsonObject.get("ref_albergo").asString else "",
        if (jsonObject.has("testo_recensione") && !jsonObject.get("testo_recensione").isJsonNull)
            jsonObject.get("testo_recensione").asString
        else "",
        if (jsonObject.has("punteggio_recensione") && !jsonObject.get("punteggio_recensione").isJsonNull)
            jsonObject.get("punteggio_recensione").asDouble
        else 0.0
    )

    override fun toString(): String =
        "Prenotazione(refUtente='$refUtente', refCamera=$refCamera, refAlbergo='$refAlbergo', " +
                "testo=$testoRecensione, punteggio=$punteggioRecensione)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        if (holder is Adapter.RecensioneViewHolder) {
            holder.autore.text = refUtente
            holder.testo.text = testoRecensione
            holder.punteggio.text = punteggioRecensione.toString().plus(" ⭐")
        }
    }
}