package com.hotelcode.entity

import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter

@Parcelize
data class PromozioneENT (
    private var codicePromozione: Int,
    private var importo: Double,
    private var puntiRichiesti: Int,
    private var speciale: Boolean,
    private var spesa: Boolean,
    private var refUtente: String?
) : Parcelable, Riciclabile {
    init {
        Log.v("Creazione Entity", "$this creata!")
    }

    fun getCodicePromozione() = codicePromozione

    fun setCodicePromozione(value: Int) {
        codicePromozione = value
    }

    fun getImporto() = importo

    fun setImporto(value: Double) {
        importo = value
    }

    fun getPuntiRichiesti() = puntiRichiesti

    fun setPuntiRichiesti(value: Int) {
        puntiRichiesti = value
    }

    fun isSpeciale() = speciale

    fun setSpeciale(value: Boolean) {
        speciale = value
    }

    fun isSpesa() = spesa

    fun setSpesa(value: Boolean) {
        spesa = value
    }

    fun getRefUtente() = refUtente

    fun setRefUtente(value: String?) {
        refUtente = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("cod_promozione")) jsonObject.get("cod_promozione").asInt else 0,
        if (jsonObject.has("importo")) jsonObject.get("importo").asDouble else 0.0,
        if (jsonObject.has("punti_richiesti")) jsonObject.get("punti_richiesti").asInt else 0,
        if (jsonObject.has("tipo")) jsonObject.get("tipo").asInt == 1 else false,
        if (jsonObject.has("spesa")) jsonObject.get("spesa").asInt == 1 else false,
        if (jsonObject.has("ref_utente") && !jsonObject.get("ref_utente").isJsonNull) jsonObject.get("ref_utente").asString else "NULL",
    )

    override fun toString(): String = "Promozione(codice=$codicePromozione, importo=$importo speciale=$speciale)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        if (holder is Adapter.PromozioneViewHolder) {
            holder.sconto.text = "Sconto: ${this.getPuntiRichiesti()}€"
            holder.puntiRichiesti.text = "Punti richiesti: ${this.getPuntiRichiesti()}"
        }
    }
}