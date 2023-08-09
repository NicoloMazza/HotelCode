package com.hotelcode.entity

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import com.hotelcode.database.MetodiDatabase

data class FotoCamera(
    private var pathFoto: String,
    private var descrizione: String
) : Riciclabile {
    init {
        Log.v("Creazione Entity", "$this creata!")
    }

    fun getPathFoto(): String = pathFoto

    fun setPathFoto(value: String) {
        pathFoto = value
    }

    fun getDescrizione(): String = descrizione

    fun setDescrizione(value: String) {
        descrizione = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("path_foto")) jsonObject.get("path_foto").asString else "",
        if (jsonObject.has("descrizione_foto")) jsonObject.get("descrizione_foto").asString else "",
    )

    override fun toString(): String = "Foto(path=$pathFoto)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        MetodiDatabase.recuperaImmagine(this.getPathFoto()) { bitmap, messaggio ->
            Log.v("Messaggio", messaggio)
            if (bitmap != null) {
                if (holder is Adapter.CameraSenzaDettagliViewHolder) {
                    holder.immagine.setImageBitmap(bitmap)
                }
            }
        }
    }
}