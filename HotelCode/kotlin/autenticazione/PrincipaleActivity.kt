package com.hotelcode.autenticazione

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hotelcode.databinding.PrincipaleActivityBinding
import androidx.activity.OnBackPressedCallback
import com.hotelcode.account.AccountFragment
import com.hotelcode.R
import com.hotelcode.camera.RicercaFragment
import com.hotelcode.archivio.ArchivioFragment
import com.hotelcode.camera.HomeFragment
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.reception.ReceptionFragment

class PrincipaleActivity : AppCompatActivity() {
    private lateinit var binding: PrincipaleActivityBinding
    private var fragmentCorrente: Int = 2 // 0 Reception, 1 Archivio, 2 Home, 3 Ricerca, 4 Account

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PrincipaleActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        if (savedInstanceState == null) {
            apriFragment(fragmentCorrente)
        }

        binding.naviView.selectedItemId = R.id.bottone_home
        binding.naviView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottone_reception -> {
                    if (fragmentCorrente != 0)
                        apriFragment(0)
                }
                R.id.bottone_archivio -> {
                    if (fragmentCorrente != 1)
                        apriFragment(1)
                }
                R.id.bottone_home -> {
                    if (fragmentCorrente != 2)
                        apriFragment(2)
                }
                R.id.bottone_ricerca -> {
                    if (fragmentCorrente != 3)
                        apriFragment(3)
                }
                R.id.bottone_account -> {
                    if (fragmentCorrente != 4)
                        apriFragment(4)
                }
            }
            true
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MetodiUtili.eseguiLogout(this@PrincipaleActivity)
            }
        })
    }

    private fun apriFragment(fragmentDaAprire: Int) {
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        if (MetodiUtili.recuperaUtenteShared(this) != null) {
            when (fragmentDaAprire) {
                0 -> transaction.replace(R.id.fragment_container, ReceptionFragment())
                1 -> transaction.replace(R.id.fragment_container, ArchivioFragment())
                2 -> transaction.replace(R.id.fragment_container, HomeFragment())
                3 -> transaction.replace(R.id.fragment_container, RicercaFragment())
                4 -> transaction.replace(R.id.fragment_container, AccountFragment())
                else -> Log.i("PrincipaleActivity", "fragmentDaAprire non valido!")
            }
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