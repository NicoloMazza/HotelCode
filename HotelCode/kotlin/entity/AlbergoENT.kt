package com.hotelcode.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalTime
import com.google.gson.JsonObject

@Parcelize
class AlbergoENT(
    private var indirizzo: String,
    private var coordinate: String,
    private var sito: String,
    private var nome: String,
    private var telefono: String,
    private var email: String,
    private var inizioCheck: LocalTime,
    private var fineCheck: LocalTime
) : Parcelable {
    fun getIndirizzo(): String = indirizzo

    fun setIndirizzo(value: String) {
        indirizzo = value
    }

    fun getCoordinate(): String = coordinate

    fun setCoordinate(value: String) {
        coordinate = value
    }

    fun getSito(): String = sito

    fun setSito(value: String) {
        sito = value
    }

    fun getNome(): String = nome

    fun setNome(value: String) {
        nome = value
    }

    fun getTelefono(): String = telefono

    fun setTelefono(value: String) {
        telefono = value
    }

    fun getEmail(): String = email

    fun setEmail(value: String) {
        email = value
    }

    fun getInizioCheck(): LocalTime = inizioCheck

    fun setInizioCheck(value: LocalTime) {
        inizioCheck = value
    }

    fun getFineCheck(): LocalTime = fineCheck

    fun setFineCheck(value: LocalTime) {
        fineCheck = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("indirizzo")) jsonObject.get("indirizzo").asString else "",
        if (jsonObject.has("coordinate")) jsonObject.get("coordinate").asString else "",
        if (jsonObject.has("sito")) jsonObject.get("sito").asString else "",
        if (jsonObject.has("nome")) jsonObject.get("nome").asString else "",
        if (jsonObject.has("telefono")) jsonObject.get("telefono").asString else "",
        if (jsonObject.has("email")) jsonObject.get("email").asString else "",
        if (jsonObject.has("inizio_check")) LocalTime.parse(jsonObject.get("inizio_check").asString) else LocalTime.now(),
        if (jsonObject.has("fine_check")) LocalTime.parse(jsonObject.get("fine_check").asString) else LocalTime.now()
    )

    override fun toString(): String = "Albergo(indirizzo=$indirizzo)"
}
