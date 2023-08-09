package com.hotelcode.autenticazione

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.hotelcode.R
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.databinding.RecuperaFragmentBinding
import com.hotelcode.entity.UtenteENT

class RecuperaFragment : Fragment() {

    private lateinit var binding: RecuperaFragmentBinding
    private lateinit var entity: UtenteENT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = RecuperaFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun isGruppoDomandaMostrato(): Boolean {
        if (binding.gruppo.isShown) {
            return true
        }
        return false
    }

    fun isGruppoPasswordMostrato(): Boolean {
        if (binding.gruppo2.isShown) {
            return true
        }
        return false
    }

    fun verificaUsername() {
        val username = binding.editTextUsername.text
        if (username.isEmpty()) {
            MetodiUtili.creaToast(context, getString(R.string.campi_non_validi))
        } else {
            val query =
                "SELECT * FROM utente WHERE utente.username = CONVERT('${username}' USING BINARY);"
            MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERA) { resultSet, messaggio ->
                MetodiUtili.creaToast(activity, messaggio)
                if (resultSet != null) {
                    entity = UtenteENT(resultSet.get(0) as JsonObject)
                    binding.textViewDomanda.text = entity.getDomanda()
                    binding.gruppo.visibility = View.VISIBLE
                } else {
                    binding.gruppo.visibility = View.INVISIBLE
                }
            }
        }
    }

    fun verificaRisposta() {
        val rispostaInserita = binding.editTextRisposta.text
        if (rispostaInserita.isEmpty()) {
            MetodiUtili.creaToast(context, getString(R.string.campi_non_validi))
        } else {
            if (rispostaInserita.toString() == entity.getRisposta()) {
                MetodiUtili.creaToast(context, "Risposta verificata!")
                binding.gruppo2.visibility = View.VISIBLE
            } else {
                MetodiUtili.creaToast(context, "Risposta errata!")
                binding.gruppo2.visibility = View.INVISIBLE
            }
        }
    }

    fun recuperaPassword() {
        val username = entity.getUsername()
        val password1 = binding.editTextPassword1.text
        val password2 = binding.editTextPassword2.text
        if (password1.isEmpty() || password2.isEmpty()) {
            MetodiUtili.creaToast(context, getString(R.string.campi_non_validi))
        } else {
            if (password1.toString() == password2.toString()) {
                val query =
                    "UPDATE utente SET utente.pass = '${MetodiUtili.generaHash(password1.toString())}' WHERE utente.username = '${username}';"
                MetodiDatabase.eseguiUpdate(query, 1) { messaggio ->
                    MetodiUtili.creaToast(context, messaggio)
                }
            } else {
                Toast.makeText(activity, "Le password inserite non coincidono!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}