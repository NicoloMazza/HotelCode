package com.hotelcode.autenticazione

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hotelcode.databinding.LoginFragmentBinding
import com.google.gson.JsonObject
import com.hotelcode.R
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.entity.UtenteENT

class LoginFragment : Fragment() {
    private lateinit var binding: LoginFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun loginUtente() {
        val username = binding.editTextUsername.text
        val password = binding.editTextPassword.text
        if (username.isEmpty() || password.isEmpty()) {
            MetodiUtili.creaToast(context, getString(R.string.campi_non_validi))
        } else {
            val query =
                "SELECT * FROM utente WHERE utente.username = CONVERT('${username}' USING BINARY) AND utente.pass = '${
                    MetodiUtili.generaHash(
                        password.toString()
                    )
                }';"
            MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_LOGIN) { resultSet, messaggio ->
                Toast.makeText(requireContext().applicationContext, messaggio, Toast.LENGTH_LONG).show()
                if (resultSet != null) {
                    val utente = UtenteENT((resultSet.get(0)) as JsonObject)
                    MetodiUtili.inserisciUtenteShared(context, utente)
                    val intent = Intent(context, PrincipaleActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}