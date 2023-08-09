package com.hotelcode.account

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.hotelcode.comuni.Adapter
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.databinding.AccountFragmentBinding
import com.hotelcode.entity.*
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class AccountFragment : Fragment() {
    private lateinit var binding: AccountFragmentBinding
    private var gruppoMostrato: Int = 1
    private var gruppoDaMostrare: Int = 1
    private var modificabile: Boolean = false
    private var statoCampiModificati = BooleanArray(12) { false }
    private var statoCampiValidi = BooleanArray(12) { true }
    private lateinit var utente: UtenteENT
    private lateinit var fotoUtenti: ArrayList<Riciclabile>
    private lateinit var adapterUtenti: Adapter
    private lateinit var fotoPromozioni: ArrayList<Riciclabile>
    private lateinit var adapterPromozioni: Adapter

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val dialogBuilder = AlertDialog.Builder(this.context)
                dialogBuilder.setTitle("Richiesta permesso fotocamera") // ok
                dialogBuilder.setMessage("Grazie per aver concesso l'accesso alla fotocamera. Nella prossima schermata potrai usare la scansione codice QR. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    run {
                        scansionaCodice()
                        dialogInterface.dismiss()
                    }
                }.show()
            } else {
                val dialogBuilder = AlertDialog.Builder(this.context)
                dialogBuilder.setTitle("Richiesta permesso fotocamera") // ok
                dialogBuilder.setMessage("Hai scelto di rifiutare l'accesso alla fotocamera. La scansione codice QR non funzionerà senza tale permesso, ma potrai comunque inserire manualmente i codici promozione speciale nel campo sotto il bottone. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.show()
            }
        }

    private val barLaucher = registerForActivityResult(ScanContract()) { result: ScanIntentResult ->
        riscattaCodice(result.contents, 1)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = AccountFragmentBinding.inflate(inflater, container, false)

        val u = MetodiUtili.recuperaUtenteShared(context)
        if (u != null) {
            utente = u
        }

        Log.v("Salvataggio Contesto", "Siamo dentro onRestoreInstanceState()")
        if (savedInstanceState != null) {
            gruppoMostrato = savedInstanceState.getInt("GruppoCorrente")
            vediGruppo(gruppoMostrato)
        }

        binding.editTextTextPersonName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.editTextTextPersonName.text.isNotEmpty()) {
                    binding.button.text = "INSERISCI CODICE QR"
                } else {
                    binding.button.text = "SCANSIONA CODICE QR"
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.button.setOnClickListener {
            if (binding.editTextTextPersonName.text.isNotEmpty()) {
                riscattaCodice(binding.editTextTextPersonName.text.toString(), 1)
            } else {
                setupPermission()
            }
        }

        if (savedInstanceState == null) {
            vediGruppo(gruppoDaMostrare) // Default 1 (Dati account)
        }

        binding.dati.setOnClickListener {
            gruppoDaMostrare = 1
            vediGruppo(gruppoDaMostrare)
        }

        binding.codiciOrdinari.setOnClickListener {
            gruppoDaMostrare = 2
            vediGruppo(gruppoDaMostrare)
        }

        binding.codiciSpeciali.setOnClickListener {
            gruppoDaMostrare = 3
            vediGruppo(gruppoDaMostrare)
        }

        binding.logout.setOnClickListener {
            MetodiUtili.eseguiLogout(context)
        }

        binding.modifica.setOnClickListener {
            if (!modificabile) {
                setCampiModificabili(true)
                fotoUtenti = ArrayList()
                recuperaImmaginiUtenti()
                adapterUtenti = Adapter(fotoUtenti)
                adapterUtenti.setOnClickListener(object :
                    Adapter.OnClickListener {
                    override fun onClick(position: Int, model: Riciclabile) {
                        val query = "UPDATE utente SET path_foto = '${(model as UtenteENT).getRefFoto()}' WHERE username = '${utente.getUsername()}'"
                        MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_FOTO_UTENTE) { messaggio ->
                            MetodiUtili.creaToast(context, messaggio)
                        }
                        utente.setRefFoto((model as UtenteENT).getRefFoto())
                        MetodiUtili.inserisciUtenteShared(context, utente)
                        MetodiDatabase.recuperaImmagine(utente.getRefFoto()) { bitmap, messaggio ->
                            binding.fotoAccount.setImageBitmap(bitmap)
                            MetodiUtili.creaToast(context, messaggio)
                        }
                    }
                })
                val recyclerView1 = binding.recyclerutente
                recyclerView1.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView1.adapter = adapterUtenti
            }
            else {
                modificaDatiAccount()
                setCampiModificabili(false)
            }
        }

        binding.nome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.nome.text.isNotEmpty()) {
                    statoCampiModificati[0] = true
                    statoCampiValidi[0] = UtenteENT.isValidNome(p0.toString())
                }
                else {
                    statoCampiModificati[0] = true
                    statoCampiValidi[0] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.cognome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.cognome.text.isNotEmpty()) {
                    statoCampiModificati[1] = true
                    statoCampiValidi[1] = UtenteENT.isValidCognome(p0.toString())
                }
                else {
                    statoCampiModificati[1] = true
                    statoCampiValidi[1] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.email.text.isNotEmpty()) {
                    statoCampiModificati[2] = true
                    statoCampiValidi[2] = UtenteENT.isValidEmail(p0)
                }
                else {
                    statoCampiModificati[2] = true
                    statoCampiValidi[2] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.password.text.isNotEmpty()) {
                    statoCampiModificati[3] = true
                    statoCampiValidi[3] = UtenteENT.isValidPassword(p0.toString())
                }
                else {
                    statoCampiModificati[3] = true
                    statoCampiValidi[3] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.domanda.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.domanda.text.isNotEmpty()) {
                    statoCampiModificati[4] = true
                    statoCampiValidi[4] = UtenteENT.isValidDomanda(p0.toString())
                }
                else {
                    statoCampiModificati[4] = true
                    statoCampiValidi[4] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.risposta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.risposta.text.isNotEmpty()) {
                    statoCampiModificati[5] = true
                    statoCampiValidi[5] = UtenteENT.isValidRisposta(p0.toString())
                }
                else {
                    statoCampiModificati[5] = true
                    statoCampiValidi[5] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.codFiscale.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.codFiscale.text.isNotEmpty()) {
                    statoCampiModificati[6] = true
                    statoCampiValidi[6] = UtenteENT.isValidCodFiscale(p0.toString())
                }
                else {
                    statoCampiModificati[6] = true
                    statoCampiValidi[6] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.puntiCorrenti.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.puntiCorrenti.text.isNotEmpty()) {
                    statoCampiModificati[7] = true
                    statoCampiValidi[7] = UtenteENT.isValidPuntiCorrenti(
                        binding.puntiCorrenti.text.toString().toInt()
                    )
                }
                else {
                    statoCampiModificati[7] = true
                    statoCampiValidi[7] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.numCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.numCarta.text.isNotEmpty()) {
                    statoCampiModificati[8] = true
                    statoCampiValidi[8] = UtenteENT.isValidNumCarta(p0.toString())
                }
                else {
                    statoCampiModificati[8] = true
                    statoCampiValidi[8] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.scadenzaCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.scadenzaCarta.text.isNotEmpty()) {
                    statoCampiModificati[9] = true
                    statoCampiValidi[9] = UtenteENT.isValidScadenzaCarta(p0.toString())
                }
                else {
                    statoCampiModificati[9] = true
                    statoCampiValidi[9] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.codSicurezzaCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.codSicurezzaCarta.text.isNotEmpty()) {
                    statoCampiModificati[10] = true
                    statoCampiValidi[10] = UtenteENT.isValidCodSicurezzaCarta(p0.toString())
                }
                else {
                    statoCampiModificati[10] = true
                    statoCampiValidi[10] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.importoCarta.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.importoCarta.text.isNotEmpty()) {
                    statoCampiModificati[11] = true
                    statoCampiValidi[11] = UtenteENT.isValidImportoCarta(
                        binding.importoCarta.text.toString().toDouble()
                    )
                }
                else {
                    statoCampiModificati[11] = true
                    statoCampiValidi[11] = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })

        return binding.root
    }

    private fun vediGruppo(gruppoDaMostrare: Int) {
        when (gruppoDaMostrare) {
            1 -> {
                nascondiGruppo(gruppoMostrato)
                MetodiDatabase.recuperaImmagine(utente.getRefFoto()) { bitmap, _ ->
                    binding.fotoAccount.setImageBitmap(bitmap)
                }
                popolaCampi()
                statoCampiModificati = BooleanArray(12) { false }
                statoCampiValidi = BooleanArray(12) { true }
                gruppoMostrato = gruppoDaMostrare
            }
            2 -> {
                nascondiGruppo(gruppoMostrato)
                binding.gruppoCodiciOrdinari.visibility = View.VISIBLE
                binding.textView16.text = utente.getPuntiCorrenti().toString()
                fotoPromozioni = ArrayList()
                recuperaPromozioni()
                adapterPromozioni = Adapter(fotoPromozioni)
                adapterPromozioni.setOnClickListener(object :
                    Adapter.OnClickListener {
                    override fun onClick(position: Int, model: Riciclabile) {
                        riscattaCodice((model as PromozioneENT).getCodicePromozione().toString(), 0)
                    }
                })
                val recyclerView2 = binding.recyclerPunti
                recyclerView2.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                recyclerView2.adapter = adapterPromozioni
                gruppoMostrato = gruppoDaMostrare
            }
            3 -> {
                nascondiGruppo(gruppoMostrato)
                binding.gruppoCodiciSpeciali.visibility = View.VISIBLE
                gruppoMostrato = gruppoDaMostrare
            }
        }
    }

    private fun nascondiGruppo(gruppoMostrato: Int) {
        when (gruppoMostrato) {
            1 -> binding.gruppoDati.visibility = View.INVISIBLE
            2 -> binding.gruppoCodiciOrdinari.visibility = View.INVISIBLE
            3 -> binding.gruppoCodiciSpeciali.visibility = View.INVISIBLE
        }
    }

    private fun setupPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setTitle("Richiesta permesso fotocamera") // ok
                dialogBuilder.setMessage("Hai già concesso in precedenza l'accesso alla fotocamera. Nella prossima schermata potrai usare la scansione codice QR. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    run {
                        scansionaCodice()
                        dialogInterface.dismiss()
                    }
                }.show()
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED -> {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setTitle("Richiesta permesso fotocamera")
                dialogBuilder.setMessage("1) Per usare la scansione codice QR è necessario fornire l'accesso alla fotocamera. Nella prossima schermata avrai la possibilità di scegliere se concedere il permesso o rifiutarlo. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    run {
                        requestPermission.launch(Manifest.permission.CAMERA)
                        dialogInterface.dismiss()
                    }
                }.show()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> { // Sembra non spuntare mai...
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setTitle("Richiesta permesso fotocamera") // ok
                dialogBuilder.setMessage("2) Per usare la scansione codice QR è necessario fornire l'accesso alla fotocamera. Nella prossima schermata avrai la possibilità di scegliere se concedere il permesso o rifiutarlo. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    run {
                        requestPermission.launch(Manifest.permission.CAMERA)
                        dialogInterface.dismiss()
                    }
                }.show()
            }
            else -> { // Sembra non spuntare mai...
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setTitle("Richiesta permesso fotocamera")
                dialogBuilder.setMessage("3) Per usare la scansione codice QR è necessario fornire l'accesso alla fotocamera. Nella prossima schermata avrai la possibilità di scegliere se concedere il permesso o rifiutarlo. In futuro potrai sempre modificare la tua scelta dalle impostazioni di sistema.")
                dialogBuilder.setPositiveButton("OK") { dialogInterface, _ ->
                    run {
                        requestPermission.launch(Manifest.permission.CAMERA)
                        dialogInterface.dismiss()
                    }
                }.show()
            }
        }
    }

    private fun scansionaCodice() {
        val options = ScanOptions()
        options.setPrompt("Premi VOLUME SU per attivare il flash o VOLUME GIU' per disattivarlo.")
        options.setBeepEnabled(false)
        options.setOrientationLocked(true)
        options.captureActivity = CatturaActivity::class.java
        barLaucher.launch(options)
    }

    private fun riscattaCodice(codice: String, tipo: Int) {
        var query = "SELECT * FROM promozione WHERE tipo = '${tipo}' AND ref_utente IS NULL AND cod_promozione = '${codice}';"
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERA_PROMOZIONE) { resultSet, messaggio ->
            MetodiUtili.creaToast(context, messaggio)
            if (resultSet != null) {
                val promozione = PromozioneENT(resultSet.get(0) as JsonObject)
                if (utente.getPuntiCorrenti() >= promozione.getPuntiRichiesti()) {
                    query = "UPDATE promozione SET ref_utente = '${utente.getUsername()}' WHERE cod_promozione = '${promozione.getCodicePromozione()}';"
                    MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_RISCATTA_PROMOZIONE) { messaggio1 ->
                        MetodiUtili.creaToast(context, messaggio1)
                    }
                    query = "UPDATE utente SET punti_correnti = '${utente.getPuntiCorrenti() - promozione.getPuntiRichiesti()}' WHERE username = '${utente.getUsername()}'"
                    MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_PUNTI_CORRENTI) { messaggio2 ->
                        MetodiUtili.creaToast(context, messaggio2)
                    }
                    utente.setPuntiCorrenti(utente.getPuntiCorrenti() - promozione.getPuntiRichiesti())
                    MetodiUtili.inserisciUtenteShared(context, utente)
                    if (tipo == 0) {
                        var position = 0
                        for (i in 0 until fotoPromozioni.size) {
                            if ((fotoPromozioni[i] as PromozioneENT).getCodicePromozione().toString() == codice) {
                                position = i
                                break
                            }
                        }
                        fotoPromozioni.removeAt(position)
                        adapterPromozioni.notifyItemRemoved(position)
                        binding.textView16.text = utente.getPuntiCorrenti().toString()
                    }
                }
                else {
                    MetodiUtili.creaToast(
                        context,
                        "Non hai abbastanza punti per riscattare la promozione!"
                    )
                }
            }
        }
    }

    private fun recuperaImmaginiUtenti() {
        val query = "SELECT path_foto FROM foto_utenti;"
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERO_FOTO_UTENTI) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val foto = UtenteENT(resultSet.get(i) as JsonObject)
                    fotoUtenti.add(foto)
                    adapterUtenti.notifyItemInserted(fotoUtenti.size - 1)
                }
            }
        }
    }

    private fun recuperaPromozioni() {
        val query = "SELECT * FROM promozione WHERE tipo = 0 AND ref_utente IS NULL ORDER BY punti_richiesti ASC;"
        MetodiDatabase.eseguiSelect(query, MetodiDatabase.SELECT_RECUPERO_PROMOZIONI) { resultSet, messaggio ->
            Log.v("Messaggio", messaggio)
            if (resultSet != null) {
                for (i in 0 until resultSet.size()) {
                    val promozione = PromozioneENT(resultSet.get(i) as JsonObject)
                    fotoPromozioni.add(promozione)
                    adapterPromozioni.notifyItemInserted(fotoPromozioni.size - 1)
                }
            }
        }
    }

    private fun popolaCampi() {
        binding.username.setText(utente.getUsername())
        binding.nome.setText(utente.getNome())
        binding.cognome.setText(utente.getCognome())
        binding.email.setText(utente.getEmail())
        binding.password.setText("(invariata)")
        binding.domanda.setText(utente.getDomanda())
        binding.risposta.setText(utente.getRisposta())
        binding.codFiscale.setText(utente.getCodFiscale())
        binding.puntiCorrenti.setText(utente.getPuntiCorrenti().toString())
        binding.numCarta.setText(utente.getNumCarta())
        binding.scadenzaCarta.setText(MetodiUtili.formatoScadenzaCarta(utente.getScadenzaCarta()))
        binding.codSicurezzaCarta.setText(utente.getCodSicurezzaCarta())
        binding.importoCarta.setText(utente.getImportoCarta().toString())
        binding.gruppoDati.visibility = View.VISIBLE
    }

    private fun setCampiModificabili(stato: Boolean) {
        modificabile = stato
        binding.modifica.text = if (stato) "Salva" else "Modifica"
        binding.recyclerutente.visibility = if (stato) View.VISIBLE else View.INVISIBLE
        binding.nome.isEnabled = stato
        binding.cognome.isEnabled = stato
        binding.email.isEnabled = stato
        binding.password.isEnabled = stato
        binding.domanda.isEnabled = stato
        binding.risposta.isEnabled = stato
        binding.codFiscale.isEnabled = stato
        binding.puntiCorrenti.isEnabled = stato
        binding.numCarta.isEnabled = stato
        binding.scadenzaCarta.isEnabled = stato
        binding.codSicurezzaCarta.isEnabled = stato
        binding.importoCarta.isEnabled = stato
        val colore = if (stato) "#77977B" else "#F9EFE1"
        binding.nome.setTextColor(Color.parseColor(colore))
        binding.cognome.setTextColor(Color.parseColor(colore))
        binding.email.setTextColor(Color.parseColor(colore))
        binding.password.setTextColor(Color.parseColor(colore))
        binding.domanda.setTextColor(Color.parseColor(colore))
        binding.risposta.setTextColor(Color.parseColor(colore))
        binding.codFiscale.setTextColor(Color.parseColor(colore))
        binding.puntiCorrenti.setTextColor(Color.parseColor(colore))
        binding.numCarta.setTextColor(Color.parseColor(colore))
        binding.scadenzaCarta.setTextColor(Color.parseColor(colore))
        binding.codSicurezzaCarta.setTextColor(Color.parseColor(colore))
        binding.importoCarta.setTextColor(Color.parseColor(colore))
    }

    private fun modificaDatiAccount() {
        if (statoCampiModificati.any { it }) {
            if (statoCampiValidi.all { it }) {
                var query = "UPDATE utente SET "
                for (i in 0 until statoCampiModificati.size) {
                    if (statoCampiModificati[i]) {
                        when (i) {
                            0 -> query += "nome = '${binding.nome.text}', "
                            1 -> query += "cognome = '${binding.cognome.text}', "
                            2 -> query += "email = '${binding.email.text}', "
                            3 -> query += "password = '${if (binding.password.text.toString() == "(invariata)") utente.getPass() else MetodiUtili.generaHash(binding.password.text.toString())}', "
                            4 -> query += "domanda = '${binding.domanda.text}', "
                            5 -> query += "risposta = '${binding.risposta.text}', "
                            6 -> query += "cod_fiscale = '${binding.codFiscale.text}', "
                            7 -> query += "punti_correnti = '${binding.puntiCorrenti.text}', "
                            8 -> query += "num_carta = '${binding.numCarta.text}', "
                            9 -> query += "scadenza_carta = '${MetodiUtili.formatoScadenzaCarta(binding.scadenzaCarta.text.toString())}', "
                            10 -> query += "cod_sicurezza_carta = '${binding.codSicurezzaCarta.text}', "
                            11 -> query += "importo_carta = '${binding.importoCarta.text}', "
                        }
                    }
                }
                query = query.substring(0, query.length - 2)
                query += " WHERE username = '${utente.getUsername()}';"
                Log.v("update utente", query)
                MetodiDatabase.eseguiUpdate(query, MetodiDatabase.UPDATE_DATI_UTENTE) { messaggio ->
                    MetodiUtili.creaToast(context, messaggio)
                    if (messaggio == "Dati aggiornati correttamente!") {
                        utente.setNome(binding.nome.text.toString())
                        utente.setCognome(binding.cognome.text.toString())
                        utente.setEmail(binding.email.text.toString())
                        utente.setPass(MetodiUtili.generaHash(binding.password.text.toString()))
                        utente.setDomanda(binding.domanda.text.toString())
                        utente.setRisposta(binding.risposta.text.toString())
                        utente.setCodFiscale(binding.codFiscale.text.toString())
                        utente.setPuntiCorrenti(binding.puntiCorrenti.text.toString().toInt())
                        utente.setNumCarta(binding.numCarta.text.toString())
                        utente.setScadenzaCarta(MetodiUtili.formatoScadenzaCarta(binding.scadenzaCarta.text.toString()))
                        utente.setCodSicurezzaCarta(binding.codSicurezzaCarta.text.toString())
                        utente.setImportoCarta(binding.importoCarta.text.toString().toDouble())
                        MetodiUtili.inserisciUtenteShared(context, utente)
                    }
                }
                statoCampiModificati = BooleanArray(12) { false }
                statoCampiValidi = BooleanArray(12) { true }
            }
            else {
                // MetodiUtili.creaToast(context, getString(R.string.campi_non_validi))
                var camposporco = 0
                for (i in 0..statoCampiValidi.size) {
                    if (!statoCampiValidi[i]) {
                        camposporco = i
                        break
                    }
                }
                val campo = when (camposporco) {
                    0 -> "nome"
                    1 -> "cognome"
                    2 -> "email"
                    3 -> "password"
                    4 -> "domanda"
                    5 -> "risposta"
                    6 -> "cod_fiscale"
                    7 -> "punti_correnti"
                    8 -> "num_carta"
                    9 -> "scadenza_carta"
                    10 -> "cod_sicurezza_carta"
                    11 -> "importo_carta"
                    else -> "sconosciuto"
                }
                MetodiUtili.creaToast(context, "Il campo $campo non è riempito correttamente!")
                statoCampiValidi.forEach {
                    Log.v("statoCampiValidi",  it.toString())
                }
                statoCampiModificati.forEach {
                    Log.v("statoCampiModificati",  it.toString())
                }
            }
        }
        else {
            MetodiUtili.creaToast(context, "Nessun campo è stato modificato.")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v("Salvataggio Contesto", "Siamo dentro onSaveInstanceState()")
        outState.putInt("GruppoCorrente", gruppoMostrato)
    }
}