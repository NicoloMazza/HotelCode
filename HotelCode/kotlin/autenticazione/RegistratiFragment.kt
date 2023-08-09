package com.hotelcode.autenticazione

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.hotelcode.R
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.databinding.RegistratiFragmentBinding
import com.hotelcode.entity.UtenteENT

class RegistratiFragment : Fragment() {

    private lateinit var binding: RegistratiFragmentBinding
    private var statoCampi = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = RegistratiFragmentBinding.inflate(inflater, container, false)
        binding.textViewUsername.text = getString(R.string.registrati_fragment_username).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidUsername(p0.toString())) {
                    binding.textViewUsername.text = getString(R.string.registrati_fragment_username).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[0] = 1
                }
                else {
                    binding.textViewUsername.text = getString(R.string.registrati_fragment_username).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[0] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewNome.text = getString(R.string.registrati_fragment_nome).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidNome(p0.toString())) {
                    binding.textViewNome.text = getString(R.string.registrati_fragment_nome).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[1] = 1
                }
                else {
                    binding.textViewNome.text = getString(R.string.registrati_fragment_nome).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[1] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewCognome.text = getString(R.string.registrati_fragment_cognome).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextCognome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidCognome(p0.toString())) {
                    binding.textViewCognome.text = getString(R.string.registrati_fragment_cognome).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[2] = 1
                }
                else {
                    binding.textViewCognome.text = getString(R.string.registrati_fragment_cognome).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[2] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewEmail.text = getString(R.string.registrati_fragment_email).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidEmail(p0)) {
                    binding.textViewEmail.text = getString(R.string.registrati_fragment_email).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[3] = 1
                }
                else {
                    binding.textViewEmail.text = getString(R.string.registrati_fragment_email).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[3] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewPassword.text = getString(R.string.registrati_fragment_password).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidPassword(p0.toString())) {
                    binding.textViewPassword.text = getString(R.string.registrati_fragment_password).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[4] = 1
                }
                else {
                    binding.textViewPassword.text = getString(R.string.registrati_fragment_password).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[4] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewDomanda.text = getString(R.string.registrati_fragment_domanda).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextDomanda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidDomanda(p0.toString())) {
                    binding.textViewDomanda.text = getString(R.string.registrati_fragment_domanda).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[5] = 1
                }
                else {
                    binding.textViewDomanda.text = getString(R.string.registrati_fragment_domanda).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[5] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewRisposta.text = getString(R.string.registrati_fragment_risposta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextRisposta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidRisposta(p0.toString())) {
                    binding.textViewRisposta.text = getString(R.string.registrati_fragment_risposta).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[6] = 1
                }
                else {
                    binding.textViewRisposta.text = getString(R.string.registrati_fragment_risposta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[6] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewCodiceFiscale.text = getString(R.string.registrati_fragment_cod_fiscale).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextCodiceFiscale.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidCodFiscale(p0.toString())) {
                    binding.textViewCodiceFiscale.text = getString(R.string.registrati_fragment_cod_fiscale).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[7] = 1
                }
                else {
                    binding.textViewCodiceFiscale.text = getString(R.string.registrati_fragment_cod_fiscale).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[7] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewNumeroCarta.text = getString(R.string.registrati_fragment_num_carta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextNumeroCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidNumCarta(p0.toString())) {
                    binding.textViewNumeroCarta.text = getString(R.string.registrati_fragment_num_carta).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[8] = 1
                }
                else {
                    binding.textViewNumeroCarta.text = getString(R.string.registrati_fragment_num_carta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[8] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewScadenzaCarta.text = getString(R.string.registrati_fragment_scadenza_carta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.scadenzaCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidScadenzaCarta(p0.toString())) {
                    binding.textViewScadenzaCarta.text = getString(R.string.registrati_fragment_scadenza_carta).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[9] = 1
                }
                else {
                    binding.textViewScadenzaCarta.text = getString(R.string.registrati_fragment_scadenza_carta).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[9] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.textViewCodiceDiSicurezza.text = getString(R.string.registrati_fragment_cod_sicurezza).plus(" ").plus(getString(R.string.emoticon_pallino_false))
        binding.editTextCodiceSicurezzaCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (UtenteENT.isValidCodSicurezzaCarta(p0.toString())) {
                    binding.textViewCodiceDiSicurezza.text = getString(R.string.registrati_fragment_cod_sicurezza).plus(" ").plus(getString(R.string.emoticon_pallino_true))
                    statoCampi[10] = 1
                }
                else {
                    binding.textViewCodiceDiSicurezza.text = getString(R.string.registrati_fragment_cod_sicurezza).plus(" ").plus(getString(R.string.emoticon_pallino_false))
                    statoCampi[10] = 0
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        return binding.root
    }

    fun registraUtente() {
        val username = binding.editTextUsername.text
        val nome = binding.editTextNome.text
        val cognome = binding.editTextCognome.text
        val email = binding.editTextEmail.text
        val password = binding.editTextPassword.text
        val domanda = binding.editTextDomanda.text
        val risposta = binding.editTextRisposta.text
        val codiceFiscale = binding.editTextCodiceFiscale.text
        val numeroCarta = binding.editTextNumeroCarta.text
        val scadenza = binding.scadenzaCarta.text
        val codiceSicurezzaCarta = binding.editTextCodiceSicurezzaCarta.text
        if (statoCampi.any { it == 0 }) {
            Toast.makeText(activity, getString(R.string.campi_non_validi), Toast.LENGTH_LONG).show()
        }
        else {
            val scadenzaCarta = MetodiUtili.formatoScadenzaCarta(scadenza.toString())
            val query =
                "INSERT INTO utente(username, nome, cognome, email, pass, domanda, risposta, cod_fiscale, num_carta, scadenza_carta, cod_sicurezza_carta) VALUES ('${username}', '${nome}', '${cognome}', '${email}', '${
                    MetodiUtili.generaHash(
                        password.toString()
                    )
                }', '${domanda}', '${risposta}', '${codiceFiscale}', '${numeroCarta}', '${scadenzaCarta}', '${codiceSicurezzaCarta}');"
            MetodiDatabase.eseguiInsert(query, 2) { messaggio ->
                MetodiUtili.creaToast(context, messaggio)
                (activity as AutenticazioneActivity?)?.apriFragment(0)
            }
        }
    }
}