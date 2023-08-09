package com.hotelcode.database

import android.graphics.Bitmap
import com.google.gson.JsonObject
import retrofit2.Response
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import com.google.gson.JsonArray
import org.jsoup.Jsoup

class MetodiDatabase {
    companion object {
        const val SELECT_LOGIN = 1
        const val SELECT_RECUPERA = 2
        const val SELECT_RECUPERO_FOTO_UTENTI = 3
        const val SELECT_RECUPERO_DATI_ALBERGO = 4
        const val SELECT_RECUPERO_DATI_CAMERE_HOME_RICERCA = 5
        const val SELECT_RECUPERO_PRENOTAZIONI = 6
        const val SELECT_RECUPERO_PROMOZIONI = 7
        const val SELECT_PROVA_CONNESSIONE = 8
        const val SELECT_RECUPERO_FOTO_CAMERA_VISUALIZZA = 9
        const val SELECT_RECUPERA_PROMOZIONE = 10
        const val SELECT_RECUPERA_RECENSIONI = 11
        const val SELECT_RECUPERA_PROMOZIONI = 12
        const val SELECT_MEDIA_RECENSIONI = 13
        const val SELECT_CONTA_RECENSIONI = 14
        const val SELECT_VERIFICA_GIORNI = 15

        const val INSERT_SEGNALAZIONE = 1
        const val INSERT_REGISTRAZIONE = 2
        const val INSERT_PRENOTAZIONE = 3

        const val UPDATE_RECUPERA_PASSWORD = 1
        const val UPDATE_RISCATTA_PROMOZIONE = 2
        const val UPDATE_PUNTI_CORRENTI = 3
        const val UPDATE_FOTO_UTENTE = 4
        const val UPDATE_DATI_UTENTE = 5
        const val UPDATE_CHECK_IN = 6
        const val UPDATE_CHECK_OUT = 7
        const val UPDATE_RECENSIONE = 8
        const val UPDATE_PAGAMENTO_UTENTE = 9
        const val UPDATE_PAGAMENTO_PROMOZIONE = 10
        const val UPDATE_MEDIA_RECENSIONI = 11

        const val DELETE_ELIMINA_PRENOTAZIONE = 1

        fun eseguiSelect(
            query: String,
            scelta: Int,
            callback: (JsonArray?, messaggio: String) -> Unit
        ) {
            ClientNetwork.retrofit.select(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    if (response.isSuccessful) {
                        val resultSet = response.body()?.get("queryset") as? JsonArray
                        if (resultSet != null) {
                            when (scelta) {
                                SELECT_LOGIN -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Credenziali errate!")
                                        1 -> callback(resultSet, "Credenziali corrette!")
                                        else -> callback(null, "Errore vincolo chiave primaria!")
                                    }
                                }
                                SELECT_RECUPERA -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Utente non trovato!")
                                        1 -> callback(resultSet, "Utente trovato!")
                                        else -> callback(null, "Errore vincolo chiave primaria!")
                                    }
                                }
                                SELECT_RECUPERO_FOTO_UTENTI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Foto non trovate!")
                                        else -> callback(resultSet, "Foto recuperate con successo!")
                                    }
                                }
                                SELECT_RECUPERO_DATI_ALBERGO -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Albergo non trovato!")
                                        else -> callback(resultSet, "Albergo trovato!")
                                    }
                                }
                                SELECT_RECUPERO_DATI_CAMERE_HOME_RICERCA -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Camere non trovate!")
                                        else -> callback(resultSet, "Camere trovate!")
                                    }
                                }
                                SELECT_RECUPERO_PRENOTAZIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Prenotazioni non trovate!")
                                        else -> callback(resultSet, "Prenotazioni trovate!")
                                    }
                                }
                                SELECT_RECUPERO_PROMOZIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Promozioni non trovate!")
                                        else -> callback(resultSet, "Promozioni trovate!")
                                    }
                                }
                                SELECT_PROVA_CONNESSIONE -> callback(resultSet, "Query eseguita!")
                                SELECT_RECUPERO_FOTO_CAMERA_VISUALIZZA -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Foto non trovate!")
                                        else -> callback(resultSet, "Foto trovate!")
                                    }
                                }
                                SELECT_RECUPERA_PROMOZIONE -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Promozione non trovata!")
                                        1 -> callback(resultSet, "Promozione trovata!")
                                        else -> callback(null, "Errore vincolo chiave primaria!")
                                    }
                                }
                                SELECT_RECUPERA_RECENSIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Recensioni non trovate!")
                                        else -> callback(resultSet, "Recensioni trovate!")
                                    }
                                }
                                SELECT_RECUPERA_PROMOZIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Promozioni non trovate!")
                                        else -> callback(resultSet, "Promozioni trovate!")
                                    }
                                }
                                SELECT_MEDIA_RECENSIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Media non trovata!")
                                        1 -> callback(resultSet, "Media trovata!")
                                        else -> callback(null, "Errore vincolo chiave primaria!")
                                    }
                                }
                                SELECT_CONTA_RECENSIONI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Conteggio non trovato!")
                                        1 -> callback(resultSet, "Conteggio trovato!")
                                        else -> callback(null, "Errore vincolo chiave primaria!")
                                    }
                                }
                                SELECT_VERIFICA_GIORNI -> {
                                    when (resultSet.size()) {
                                        0 -> callback(null, "Possiamo procedere con la prenotazione!")
                                        else -> callback(resultSet, "Non possiamo procedere con la prenotazione!")
                                    }
                                }
                            }
                        }
                    } else {
                        callback(null, "Query sbagliata!")
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    callback(null, "Impossibile collegarsi al server!")
                }
            })
        }

        fun messaggioErroreSQL(response: Response<JsonObject>): String {
            val html = response.errorBody()?.string().toString()
            val doc = Jsoup.parse(html)
            val exceptionValueElement = doc.select("pre.exception_value")
            val errorMessage = exceptionValueElement?.text()
            val regexp = "'.*'"
            val matchResult = regexp.toRegex().find(errorMessage ?: "")
            val attribute = matchResult?.value?.removeSurrounding("'", "'")
            val attribute2 = attribute?.replace("_", " ")
            return when {
                (errorMessage?.contains("Data too long for column") == true || (errorMessage?.contains(
                    "Check constraint"
                ) == true && errorMessage.contains("is violated"))) && attribute2 != null -> {
                    "Il valore del campo $attribute2 non è valido!"
                }
                errorMessage?.contains("Incorrect date value") == true && attribute2 != null -> {
                    "Il valore del campo ${
                        attribute2.substringAfter("'").substringAfter("'")
                    } non è valido!"
                }
                errorMessage?.contains("Duplicate entry") == true && attribute2 != null -> {
                    "Il valore ${attribute2.substringBefore("'")} è già in uso!"
                }
                else -> errorMessage.toString()
            }
        }

        fun eseguiInsert(query: String, scelta: Int, callback: (String) -> Unit) {
            ClientNetwork.retrofit.insert(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.v("eseguiInsert", response.body().toString())
                    if (response.isSuccessful) {
                        when (scelta) {
                            INSERT_SEGNALAZIONE -> callback("Segnalazione effettuata!")
                            INSERT_REGISTRAZIONE -> callback("Registrazione effettuata!")
                            INSERT_PRENOTAZIONE -> callback("Prenotazione effettuata!")
                        }
                    } else {
                        callback(messaggioErroreSQL(response))
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    callback("Impossibile collegarsi al server!")
                }
            })
        }

        fun eseguiUpdate(query: String, scelta: Int, callback: (String) -> Unit) {
            ClientNetwork.retrofit.update(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.v("eseguiUpdate", response.body().toString())
                    if (response.isSuccessful) {
                        when (scelta) {
                            UPDATE_RECUPERA_PASSWORD -> callback("Password aggiornata correttamente!")
                            UPDATE_RISCATTA_PROMOZIONE -> callback("Promozione riscattata correttamente!")
                            UPDATE_PUNTI_CORRENTI -> callback("Punti correnti aggiornati correttamente!")
                            UPDATE_FOTO_UTENTE -> callback("Foto aggiornata correttamente!")
                            UPDATE_DATI_UTENTE -> callback("Dati aggiornati correttamente!")
                            UPDATE_CHECK_IN -> callback("Check-in effettuato correttamente!")
                            UPDATE_CHECK_OUT -> callback("Check-out effettuato correttamente!")
                            UPDATE_RECENSIONE-> callback("Recensione registrata correttamente!")
                            UPDATE_PAGAMENTO_UTENTE -> callback("Pagamento utente aggiornato correttamente!")
                            UPDATE_PAGAMENTO_PROMOZIONE -> callback("Pagamento promozione aggiornato correttamente!")
                            UPDATE_MEDIA_RECENSIONI -> callback("Media aggiornata correttamente!")
                        }
                    } else {
                        callback(messaggioErroreSQL(response))
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    callback("Impossibile collegarsi al server!")
                }
            })
        }

        fun eseguiDelete(query: String, scelta: Int, callback: (String) -> Unit) {
            ClientNetwork.retrofit.delete(query).enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                    Log.v("eseguiDelete", response.body().toString())
                    if (response.isSuccessful) {
                        when (scelta) {
                            DELETE_ELIMINA_PRENOTAZIONE -> callback("Prenotazione eliminata correttamente!")
                        }
                    } else {
                        callback(messaggioErroreSQL(response))
                    }
                }
                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    callback("Impossibile collegarsi al server!")
                }
            })
        }

        fun recuperaImmagine(path: String, callback: (Bitmap?, messaggio: String) -> Unit) {
            ClientNetwork.retrofit.getFoto(path).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val avatar: Bitmap?
                        if (response.body() != null) {
                            avatar = BitmapFactory.decodeStream(response.body()?.byteStream())
                            callback(avatar, "Immagine recuperata con successo!")
                        } else {
                            callback(null, "Errore BitmapFactory!")
                        }
                    } else {
                        callback(null, "Query sbagliata!")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    callback(null, "Impossibile collegarsi al server!")
                }
            })
        }
    }
}