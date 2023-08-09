package com.hotelcode.autenticazione

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.hotelcode.R
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.database.MetodiDatabase
import com.hotelcode.database.NoConnessioneActivity
import com.hotelcode.databinding.AutenticazioneActivityBinding

class AutenticazioneActivity : AppCompatActivity() {
    private lateinit var binding: AutenticazioneActivityBinding
    private var fragmentCorrente: Int = 0 // 0 Login, 1 Registrati, 2 Recupera

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AutenticazioneActivityBinding.inflate(layoutInflater)
        supportActionBar?.hide()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MetodiUtili.creaAlertDialog(this@AutenticazioneActivity,
                    "Uscita", "Sei sicuro di voler uscire dall'applicazione?",
                    {
                        finishAffinity()
                    }, {})
                // finishAffinity() chiude tutte le activity presenti attualmente nello Stack
                // finish() chiude solo l'activity aperta
            }
        })

        if (MetodiUtili.recuperaUtenteShared(this) != null) {
            val query = "SELECT * FROM albergo LIMIT 1"
            MetodiDatabase.eseguiSelect(
                query,
                MetodiDatabase.SELECT_PROVA_CONNESSIONE
            ) { resultSet, _ ->
                if (resultSet != null) {
                    Log.v("Result prova", resultSet.toString())
                    val intent = Intent(this, PrincipaleActivity::class.java)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this, NoConnessioneActivity::class.java)
                    startActivity(intent)
                }
            }
        } else {
            setContentView(binding.root)

            if (savedInstanceState == null) {
                Log.v("siamo dentro", " if (savedInstanceState == null)")
                apriFragment(fragmentCorrente)
            }

            binding.login.setOnClickListener {
                if (fragmentCorrente != 0)
                    apriFragment(0)
            }

            binding.registrati.setOnClickListener {
                if (fragmentCorrente != 1)
                    apriFragment(1)
            }

            binding.recupera.setOnClickListener {
                if (fragmentCorrente != 2)
                    apriFragment(2)
            }

            binding.enter.setOnClickListener {
                val fragmentManager = supportFragmentManager
                when (fragmentCorrente) {
                    0 -> {
                        val fragment =
                            fragmentManager.findFragmentById(R.id.fragment_container) as? LoginFragment
                        fragment?.loginUtente()
                    }
                    1 -> {
                        val fragment =
                            fragmentManager.findFragmentById(R.id.fragment_container) as? RegistratiFragment
                        fragment?.registraUtente()
                    }
                    2 -> {
                        val fragment =
                            fragmentManager.findFragmentById(R.id.fragment_container) as? RecuperaFragment
                        if (fragment != null) {
                            if (!fragment.isGruppoDomandaMostrato()) {
                                fragment.verificaUsername()
                            } else {
                                if (!fragment.isGruppoPasswordMostrato()) {
                                    fragment.verificaRisposta()
                                } else {
                                    fragment.recuperaPassword()
                                }
                            }
                        }
                    }
                    else -> Log.i("AutenticazioneActivity", "fragmentCorrente non valido!")
                }
            }
        }
    }

    fun apriFragment(fragmentDaAprire: Int) {
            val manager = supportFragmentManager
            val transaction = manager.beginTransaction()
            Log.v("siamo dentro", "apriFragment")
            when (fragmentDaAprire) {
                0 -> transaction.replace(R.id.fragment_container, LoginFragment())
                1 -> transaction.replace(R.id.fragment_container, RegistratiFragment())
                2 -> transaction.replace(R.id.fragment_container, RecuperaFragment())
                else -> Log.i("AutenticazioneActivity", "fragmentDaAprire non valido!")
            }
            fragmentCorrente = fragmentDaAprire
            transaction.commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v("Salvataggio Contesto", "Siamo dentro onSaveInstanceState()")
        outState.putInt("FragmentCorrente", fragmentCorrente)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.v("Salvataggio Contesto", "Siamo dentro onRestoreInstanceState()")
        val fragmentCorrentePrimaDiRuotare = savedInstanceState.getInt("FragmentCorrente")
        fragmentCorrente = fragmentCorrentePrimaDiRuotare
    }
}