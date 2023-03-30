package com.example.myapplication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AlertDialog
import com.example.myapplication.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =  ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        setup(email ?: "", provider ?: "")

        // Guardado de datos

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()



    }

    private fun setup (email: String, provider: String) {
        title = "Inicio"

        binding.emailTextView.text = email
        binding.providerTextView.text = provider

        binding.logOutButton.setOnClickListener {
            logOut()
        }

    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage("Quieres cerrar sesion y salir?")
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, whichButton ->
               logOut() //Sale de Activity.
            }
            .setNegativeButton("Cancelar") { dialog, whichButton ->

            }
            .show()
    }

    private fun logOut(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()
        FirebaseAuth.getInstance().signOut()
        onBackPressedDispatcher.onBackPressed()
    }
}