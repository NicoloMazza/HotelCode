package com.hotelcode.comuni

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.gson.GsonBuilder
import java.security.MessageDigest
import com.hotelcode.autenticazione.AutenticazioneActivity
import com.hotelcode.entity.UtenteENT
import java.time.LocalDate
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.hotelcode.R

class MetodiUtili {
    companion object {
        fun generaHash(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(input.toByteArray())
            val hexString = StringBuilder()
            for (byte in bytes) {
                val hex = Integer.toHexString(0xff and byte.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        }

        fun formatoScadenzaCarta(stringa: String): LocalDate {
            return LocalDate.parse("20" + stringa.substring(3, 5) + "-" + stringa.substring(0, 2) + "-01")
        }

        fun formatoScadenzaCarta(data: LocalDate): String {
            return (if (data.monthValue <= 9) "0" else "") + data.monthValue.toString() + "/" + data.year.toString().substring(2, 4)
        }

        fun pulisciStrigna(stringa: String): String {
            return stringa.trim().replace(" +".toRegex(), " ").replace("\"", "''").replace(";", ",")
                .replace("\\", "").replace("--", "-")
        }

        fun creaAlertDialog(
            context: Context, titolo: String, messaggio: String,
            azioneConferma: () -> Unit, azioneAnnulla: () -> Unit
        ) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(titolo)
                .setMessage(messaggio)
                .setPositiveButton("Conferma") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    azioneConferma()
                }
                .setNegativeButton("Annulla") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    azioneAnnulla()
                }
                .setCancelable(true)
                .show()
        }

        fun creaToast(context: Context?, messaggio: String) {
            if (context != null) {
                Toast.makeText(context, messaggio, Toast.LENGTH_LONG).show()
            }
        }

        fun eseguiLogout(context: Context?) {
            if (context != null) {
                creaAlertDialog(context, "Logout", "Sei sicuro di voler effettuare il logout?",
                    {
                        val sharedPreferences =
                            context.getSharedPreferences("Preferenze", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.remove("loggato")
                        editor.apply()
                        val intent = Intent(context, AutenticazioneActivity::class.java)
                        context.startActivity(intent)
                    }, { })
            }
        }

        fun inserisciUtenteShared(context: Context?, utente: UtenteENT): Boolean {
            /*
            if (context != null) {
                val gson = GsonBuilder().registerTypeAdapter(
                    LocalDate::class.java,
                    LocalDateTypeAdapter()
                ).create()
                val utenteJson = gson.toJson(utente)
                val sharedPreferences = context.getSharedPreferences(
                    "Preferenze",
                    Context.MODE_PRIVATE
                )
                val editor = sharedPreferences.edit()
                editor.putString("UtenteENT", utenteJson)
                editor.apply()
                return true
            }
            return false
             */
            if (context != null) {
                val sharedPreferences = context.getSharedPreferences("Preferenze", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("loggato", "true")
                editor.putString("username", utente.getUsername())
                editor.putString("nome", utente.getNome())
                editor.putString("cognome", utente.getCognome())
                editor.putString("email", utente.getEmail())
                editor.putString("pass", utente.getPass())
                editor.putString("domanda", utente.getDomanda())
                editor.putString("risposta", utente.getRisposta())
                editor.putString("refFoto", utente.getRefFoto())
                editor.putString("codFiscale", utente.getCodFiscale())
                editor.putInt("puntiCorrenti", utente.getPuntiCorrenti())
                editor.putString("numCarta", utente.getNumCarta())
                editor.putString("scadenzaCarta", utente.getScadenzaCarta().toString())
                editor.putString("codSicurezzaCarta", utente.getCodSicurezzaCarta())
                editor.putString("importoCarta", utente.getImportoCarta().toString())
                editor.apply()
                return true
            }
            return false
        }

        fun recuperaUtenteShared(context: Context?): UtenteENT? {
            /*
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences("Preferenze", Context.MODE_PRIVATE)
                val utenteJson = sharedPreferences.getString("UtenteENT", null)
                if (utenteJson != null) {
                    val gson =
                        GsonBuilder().registerTypeAdapter(
                            LocalDate::class.java,
                            LocalDateTypeAdapter()
                        )
                            .create()
                    return gson.fromJson(utenteJson, UtenteENT::class.java)
                }
            }
            return null
             */
            if (context != null) {
                val sharedPreferences =
                    context.getSharedPreferences("Preferenze", Context.MODE_PRIVATE)
                val loggato = sharedPreferences.getString("loggato", "false")
                if (loggato == "true") {
                    val username = sharedPreferences.getString("username", null)
                    val nome = sharedPreferences.getString("nome", null)
                    val cognome = sharedPreferences.getString("cognome", null)
                    val email = sharedPreferences.getString("email", null)
                    val pass = sharedPreferences.getString("pass", null)
                    val domanda = sharedPreferences.getString("domanda", null)
                    val risposta = sharedPreferences.getString("risposta", null)
                    val refFoto = sharedPreferences.getString("refFoto", null)
                    val codFiscale = sharedPreferences.getString("codFiscale", null)
                    val puntiCorrenti = sharedPreferences.getInt("puntiCorrenti", 0)
                    val numCarta = sharedPreferences.getString("numCarta", null)
                    val scadenzaCarta =
                        LocalDate.parse(sharedPreferences.getString("scadenzaCarta", "0000-00-00"))
                    val codSicurezzaCarta = sharedPreferences.getString("codSicurezzaCarta", null)
                    val importoCarta = sharedPreferences.getString("importoCarta", "0.0").toString().toDouble()
                    return UtenteENT(
                        username.toString(),
                        nome.toString(),
                        cognome.toString(),
                        email.toString(),
                        pass.toString(),
                        domanda.toString(),
                        risposta.toString(),
                        refFoto.toString(),
                        codFiscale.toString(),
                        puntiCorrenti,
                        numCarta.toString(),
                        scadenzaCarta,
                        codSicurezzaCarta.toString(),
                        importoCarta
                    )
                }
            }
            return null
        }

        fun creaNotifica(context: Context?, titolo: String, messaggio: String) {
            if (context != null) {
                val builder: NotificationCompat.Builder =
                    NotificationCompat.Builder(context, "my_channel")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(titolo)
                        .setContentText(messaggio)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = NotificationChannel(
                    "my_channel",
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
                notificationManager.notify(1, builder.build())
            }
        }
    }
}

/*
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateTypeAdapter : TypeAdapter<LocalDate>() {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun write(out: JsonWriter, value: LocalDate?) {
        if (value == null) {
            out.nullValue()
        }
        else {
            out.value(formatter.format(value))
        }
    }

    override fun read(`in`: JsonReader): LocalDate? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val dateStr = `in`.nextString()
        return LocalDate.parse(dateStr, formatter)
    }

}

package com.hotelcode.comuni

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeTypeAdapter : TypeAdapter<LocalTime>() {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(formatter.format(value))
        }
    }

    override fun read(`in`: JsonReader): LocalTime? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }
        val timeStr = `in`.nextString()
        return LocalTime.parse(timeStr, formatter)
    }
}
 */