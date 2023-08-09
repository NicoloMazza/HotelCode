package com.hotelcode.entity

import android.util.Log
import com.google.gson.JsonObject
import java.time.LocalDate
import android.os.Parcelable
import android.util.Patterns
import androidx.recyclerview.widget.RecyclerView
import com.hotelcode.comuni.Adapter
import com.hotelcode.database.MetodiDatabase
import kotlinx.parcelize.Parcelize

@Parcelize
data class UtenteENT(
    private var username: String,
    private var nome: String,
    private var cognome: String,
    private var email: String,
    private var pass: String,
    private var domanda: String,
    private var risposta: String,
    private var refFoto: String,
    private var codFiscale: String,
    private var puntiCorrenti: Int,
    private var numCarta: String,
    private var scadenzaCarta: LocalDate,
    private var codSicurezzaCarta: String,
    private var importoCarta: Double
) : Parcelable, Riciclabile {
    init {
        Log.v("Creazione Entity", "$this creato!")
    }

    companion object ValidazioneDati {
        fun isValidUsername(value: String): Boolean = value.length in 1..50
        fun isValidNome(value: String): Boolean = value.matches(Regex("^[A-Za-z \']+$"))
        fun isValidCognome(value: String): Boolean = value.matches(Regex("^[A-Za-z \']+$"))
        fun isValidEmail(email: CharSequence?): Boolean = !email.isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
        fun isValidPassword(value: String): Boolean = value.isNotEmpty()
        fun isValidDomanda(value: String): Boolean = value.length in 1..400
        fun isValidRisposta(value: String): Boolean = value.length in 1..400
        fun isValidCodFiscale(value: String): Boolean = value.matches(Regex("^[A-Z]{6}[0-9]{2}[A-Z]{1}[0-9]{2}[A-Z]{1}[0-9]{3}[A-Z]{1}$"))
        fun isValidNumCarta(value: String): Boolean = value.matches(Regex("^[0-9]{16}$"))
        fun isValidScadenzaCarta(value: LocalDate): Boolean = value >= LocalDate.now()
        fun isValidScadenzaCarta(value: String): Boolean = value.matches(Regex("^[0-9]{2}/[0-9]{2}$"))
        fun isValidCodSicurezzaCarta(value: String): Boolean = value.matches(Regex("^[0-9]{3}$"))
        fun isValidPuntiCorrenti(value: Int): Boolean = value >= 0
        fun isValidImportoCarta(value: Double): Boolean = value >= 0.0
    }

    fun getUsername(): String = username

    fun setUsername(value: String) {
        if (!isValidUsername(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        username = value
    }

    fun getNome(): String = nome

    fun setNome(value: String) {
        if (!isValidNome(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        nome = value
    }

    fun getCognome(): String = cognome

    fun setCognome(value: String) {
        if (!isValidCognome(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        cognome = value
    }

    fun getEmail(): String = email

    fun setEmail(value: String) {
        if (!isValidEmail(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        email = value
    }

    fun getPass(): String = pass

    fun setPass(value: String) {
        if (!isValidPassword(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        pass = value
    }

    fun getDomanda(): String = domanda

    fun setDomanda(value: String) {
        if (!isValidDomanda(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        domanda = value
    }

    fun getRisposta(): String = risposta

    fun setRisposta(value: String) {
        if (!isValidRisposta(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        risposta = value
    }

    fun getRefFoto(): String = refFoto

    fun setRefFoto(value: String) {
        refFoto = value
    }

    fun getCodFiscale(): String = codFiscale

    fun setCodFiscale(value: String) {
        if (!isValidCodFiscale(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        codFiscale = value
    }

    fun getPuntiCorrenti(): Int = puntiCorrenti

    fun setPuntiCorrenti(value: Int) {
        if (!isValidPuntiCorrenti(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        puntiCorrenti = value
    }

    fun getNumCarta(): String = numCarta

    fun setNumCarta(value: String) {
        if (!isValidNumCarta(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        numCarta = value
    }

    fun getScadenzaCarta(): LocalDate = scadenzaCarta

    fun setScadenzaCarta(value: LocalDate) {
        if (!isValidScadenzaCarta(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        scadenzaCarta = value
    }

    fun getCodSicurezzaCarta(): String = codSicurezzaCarta

    fun setCodSicurezzaCarta(value: String) {
        if (!isValidCodSicurezzaCarta(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        codSicurezzaCarta = value
    }

    fun getImportoCarta(): Double = importoCarta

    fun setImportoCarta(value: Double) {
        if (!isValidImportoCarta(value)) {
            throw IllegalArgumentException("Formato non valido!")
        }
        importoCarta = value
    }

    constructor(jsonObject: JsonObject) : this(
        if (jsonObject.has("username")) jsonObject.get("username").asString else "",
        if (jsonObject.has("nome")) jsonObject.get("nome").asString else "",
        if (jsonObject.has("cognome")) jsonObject.get("cognome").asString else "",
        if (jsonObject.has("email")) jsonObject.get("email").asString else "",
        if (jsonObject.has("pass")) jsonObject.get("pass").asString else "",
        if (jsonObject.has("domanda")) jsonObject.get("domanda").asString else "",
        if (jsonObject.has("risposta")) jsonObject.get("risposta").asString else "",
        if (jsonObject.has("path_foto")) jsonObject.get("path_foto").asString else "",
        if (jsonObject.has("cod_fiscale")) jsonObject.get("cod_fiscale").asString else "",
        if (jsonObject.has("punti_correnti")) jsonObject.get("punti_correnti").asInt else 0,
        if (jsonObject.has("num_carta")) jsonObject.get("num_carta").asString else "",
        if (jsonObject.has("scadenza_carta")) LocalDate.parse(jsonObject.get("scadenza_carta").asString) else LocalDate.now(),
        if (jsonObject.has("cod_sicurezza_carta")) jsonObject.get("cod_sicurezza_carta").asString else "",
        if (jsonObject.has("importo_carta")) jsonObject.get("importo_carta").asDouble else 0.0
    )

    override fun toString(): String = "Utente(username=$username, nome=$nome, cognome=$cognome, scadenza=$scadenzaCarta)"

    override fun popolaCarta(holder: RecyclerView.ViewHolder) {
        MetodiDatabase.recuperaImmagine(this.getRefFoto()) { bitmap, messaggio ->
            Log.v("Messaggio", messaggio.toString())
            if (bitmap != null) {
                if (holder is Adapter.FotoUtenteViewHolder) {
                    holder.avatar.setImageBitmap(bitmap)
                }
            }
        }
    }
}