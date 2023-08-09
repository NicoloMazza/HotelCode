package com.hotelcode.comuni

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hotelcode.databinding.*
import com.hotelcode.entity.*

class Adapter(private val oggettiList: ArrayList<Riciclabile>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null
    private val TYPE_CAMERA_CON_DETTAGLI = 0 // RV di Home, Ricerca -> CameraENT
    private val TYPE_PRENOTAZIONE = 1 // RV di Archivio -> PrenotazioneENT
    private val TYPE_PROMOZIONE = 2 // RV di Promozioni ordinarie -> PromozioneENT
    private val TYPE_FOTO_UTENTE = 3 // RV di Modifica Dati -> UtenteENT
    private val TYPE_RECENSIONE = 4 // RV di Visualizza Camera -> RecensioneENT
    private val TYPE_CAMERA_SENZA_DETTAGLI = 5 // RV di Visualizza Camera ->

    // crea nuove view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // fa l'inflate del design della card
        return when (viewType) {
            TYPE_CAMERA_CON_DETTAGLI -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = CameraCardBinding.inflate(inflater, parent, false)
                CameraDettagliViewHolder(binding)
            }
            TYPE_PRENOTAZIONE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PrenotazioneCardBinding.inflate(inflater, parent, false)
                PrenotazioneViewHolder(binding)
            }
            TYPE_PROMOZIONE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PromozioneCardBinding.inflate(inflater, parent, false)
                PromozioneViewHolder(binding)
            }
            TYPE_FOTO_UTENTE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = FotoCardBinding.inflate(inflater, parent, false)
                FotoUtenteViewHolder(binding)
            }
            TYPE_RECENSIONE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RecensioneCardBinding.inflate(inflater, parent, false)
                RecensioneViewHolder(binding)
            }
            TYPE_CAMERA_SENZA_DETTAGLI -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = FotoGenericaCardBinding.inflate(inflater, parent, false)
                CameraSenzaDettagliViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Tipo di view holder non gestito")
        }
    }

    // lega gli elementi alla view
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val oggetto = oggettiList[position]
        oggetto.popolaCarta(holder)
        holder.itemView.setOnClickListener {
            onClickListener?.onClick(position, oggetto)
        }
    }

    override fun getItemCount(): Int {
        return oggettiList.size
    }

    // ritorna il tipo dell'elemento selezionato
    override fun getItemViewType(position: Int): Int {
        return when (oggettiList[position]) {
            is CameraENT -> TYPE_CAMERA_CON_DETTAGLI
            is PrenotazioneENT -> TYPE_PRENOTAZIONE
            is PromozioneENT -> TYPE_PROMOZIONE
            is FotoCamera -> TYPE_CAMERA_SENZA_DETTAGLI
            is RecensioneENT -> TYPE_RECENSIONE
            is UtenteENT -> TYPE_FOTO_UTENTE
            else -> throw IllegalArgumentException("Tipo di elemento non valido")
        }
    }

    class CameraDettagliViewHolder(private val binding: CameraCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val immagine: ImageView = binding.immagineCameraRecyclerHome
        val punteggio: TextView = binding.punteggioCameraRecyclerHome
        val prezzo: TextView = binding.prezzoCameraRecyclerHome
        val nome: TextView = binding.nomeCameraRecyclerHome
        init {
            Log.i("RV", "Camera con dettagli ViewHolder created")
        }
    }

    class PrenotazioneViewHolder(private val binding: PrenotazioneCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val immagine: ImageView = binding.immaginePrenotazione
        val dataInizio: TextView = binding.dataInizioPrenotazione
        val dataFine: TextView = binding.dataFinePrenotazione
        val nomeCamera: TextView = binding.nomeCameraPrenotazione
        init {
            Log.i("RV", "Prenotazione ViewHolder created")
        }
    }

    class PromozioneViewHolder(private val binding: PromozioneCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val puntiRichiesti: TextView = binding.puntiRichiesti
        val sconto: TextView = binding.puntiInterrogativi
        init {
            Log.i("RV", "Promozione ViewHolder created")
        }
    }

    class FotoUtenteViewHolder(private val binding: FotoCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val avatar: ImageView = binding.immagineCameraRecyclerHome
        init {
            Log.i("RV", "Foto utente ViewHolder created")
        }
    }

    class RecensioneViewHolder(private val binding: RecensioneCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val autore: TextView = binding.campoUsername
        val punteggio: TextView = binding.campoPunteggioRecensione
        val testo: TextView = binding.campoTestoRecensione
        init {
            Log.i("RV", "Recensione ViewHolder created")
        }
    }

    class CameraSenzaDettagliViewHolder(private val binding: FotoGenericaCardBinding) : RecyclerView.ViewHolder(binding.root) {
        val immagine: ImageView = binding.immagine
        init {
            Log.i("RV", "Camera senza dettagli ViewHolder created")
        }
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Riciclabile)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}