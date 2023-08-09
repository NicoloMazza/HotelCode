package com.hotelcode.database

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.hotelcode.autenticazione.AutenticazioneActivity
import com.hotelcode.comuni.MetodiUtili
import com.hotelcode.databinding.NoConnessioneActivityBinding

class NoConnessioneActivity : AppCompatActivity() {
    private lateinit var binding: NoConnessioneActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = NoConnessioneActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                MetodiUtili.creaAlertDialog(this@NoConnessioneActivity,
                    "Uscita", "Sei sicuro di voler uscire dall'applicazione?",
                    {
                        finishAffinity()
                    }, {})
            }
        })

        binding.buttonRetry.setOnClickListener {
            val intent = Intent(this, AutenticazioneActivity::class.java)
            startActivity(intent)
        }
    }
}