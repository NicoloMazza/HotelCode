package com.hotelcode.entity

import android.util.Log
import com.google.gson.JsonObject
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.hotelcode.comuni.Adapter
import com.hotelcode.database.MetodiDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
data class CameraENT(
    private var numCamera: Int,
    private var refAlbergo: String,
    private var nomeCamera: String,
    private var descrizione: String,
    private var numLetti: Int,
    private var accessoDisabili: Boolean,
    private var accessoAnimali: Boolean,
    private var inOfferta: Boolean,
    private var mediaRecensioni: Double,
    private var piscinaPrivata: Boolean,
    private var balcone: Boolean,
    private var bagno: Boolean,
    private var tv: Boolean,
    private var wifi: Boolean,
    private var prezzo: Double,
    private var pathFoto: String
) : Parcelable, Riciclabile {
    init {
        Log.v("Creazione Entity", "$this creata!")
    }

    fun getNumCamera(): Int = numCamera

    fun setNumCamera(value: Int) {
        numCamera = value
    }

    fun getRefAlbergo(): String = refAlbergo

    fun setRefAlbergo(value: String) {
        refAlbergo = value
    }

    fun getNomeCamera(): String = nomeCamera

    fun setNomeCamera(value: String) {
        nomeCamera = value
    }

    fun getDescrizione(): String = descrizione

    fun setDescrizione(value: String) {
        descrizione = value
    }

    fun getNumLetti(): Int = numLetti

    fun setNumLetti(value: Int) {
        if (value < 1 || value > 5) {
            throw IllegalArgumentException("Il numero di letti deve essere compreso tra 1 e 5")
        }
        numLetti = value
    }

    fun hasAccessoDisabili(): Boolean = accessoDisabili

    fun setAccessoDisabili(value: Boolean) {
        accessoDisabili = value
    }

    fun hasAccessoAnimali(): Boolean = accessoAnimali

    fun setAccessoAnimali(value: Boolean) {
        accessoAnimali = value
    }

    fun isInOfferta(): Boolean = inOfferta

    fun setInOfferta(value: Boolean) {
        inOfferta = value
    }

    fun getMediaRecensioni(): Double = mediaRecensioni

    fun setMediaRecensioni(value: Double) {
        if (value < 1.0 || value > 5.0) {
            throw IllegalArgumentException("Il valore di media recensioni deve essere compreso tra 1.0 e 5.0")
        }
        mediaRecensioni = value
    }

    fun hasPiscinaPrivata(): Boolean = piscinaPrivata

    fun setPiscinaPrivata(value: Boolean) {
        piscinaPrivata = value
    }

    fun hasBalcone(): Boolean = balcone

    fun setBalcone(value: Boolean) {
        balcone = value
    }

    fun hasBagno(): Boolean = bagno

    fun setBagno(value: Boolean) {
        bagno = value
    }

    fun hasTv(): Boolean = tv

    fun setTv(value: Boolean) {
        tv = value
    }

    fun hasWifi(): Boolean = wifi

    fun setWifi(value: Boolean) {
        wifi = value
    }

    fun getPrezzo(): Double = prezzo

    fun setPrezzo(value: Double) {
        if (value <= 0.0) {
            throw IllegalArgumentException("Il prezzo deve essere maggiore di 0.0")
        }
        prezzo = value
    }

    fun getPathFoto(): String = pathFoto

    fun setPathFoto(value: String) {
        pathFoto = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("num_camera")) jsonObject.get("num_camera").asInt else 0,
        if (jsonObject.has("ref_albergo")) jsonObject.get("ref_albergo").asString else "",
        if (jsonObject.has("nome_camera")) jsonObject.get("nome_camera").asString else "",
        if (jsonObject.has("descrizione")) jsonObject.get("descrizione").asString else "",
        if (jsonObject.has("num_letti")) jsonObject.get("num_letti").asInt else 0,
        if (jsonObject.has("accesso_disabili")) jsonObject.get("accesso_disabili").asInt == 1 else false,
        if (jsonObject.has("accesso_animali")) jsonObject.get("accesso_animali").asInt == 1 else false,
        if (jsonObject.has("in_offerta")) jsonObject.get("in_offerta").asInt == 1 else false,
        if (jsonObject.has("media_recensioni")) jsonObject.get("media_recensioni").asDouble else 0.0,
        if (jsonObject.has("piscina")) jsonObject.get("piscina").asInt == 1 else false,
        if (jsonObject.has("balcone")) jsonObject.get("balcone").asInt == 1 else false,
        if (jsonObject.has("bagno")) jsonObject.get("bagno").asInt == 1 else false,
        if (jsonObject.has("tv")) jsonObject.get("tv").asInt == 1 else false,
        if (jsonObject.has("wifi")) jsonObject.get("wifi").asInt == 1 else false,
        if (jsonObject.has("prezzo")) jsonObject.get("prezzo").asDouble else 0.0,
        if (jsonObject.has("path_foto")) jsonObject.get("path_foto").asString else ""
    )

    override fun toString(): String = "Camera(numCamera=$numCamera, refAlbergo=$refAlbergo, nomeCamera=$nomeCamera, wifi=$wifi)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        MetodiDatabase.recuperaImmagine(this.getPathFoto()) { bitmap, messaggio ->
            Log.v("Messaggio", messaggio)
            if (bitmap != null) {
                if (holder is Adapter.CameraDettagliViewHolder) {
                    holder.immagine.setImageBitmap(bitmap)
                    holder.punteggio.text = this.getMediaRecensioni().toString().substring(0, 3).plus(" ⭐️")
                    holder.prezzo.text = this.getPrezzo().toString().plus("€")
                    holder.nome.text = this.getNomeCamera()
                }
            }
        }
    }
}