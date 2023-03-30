package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityAuthBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase


class AuthActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthBinding
    private lateinit var analytics: FirebaseAnalytics
//    private val GOOGLE_SIGN_IN = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtenemos el preferences Manager // It's deprecated

        /*
        CAMBIAR LA FUENTE
            binding.textView.typeface = Typeface.createFromAsset(assets,"fonts/Anton-Regular.ttf")
            binding.textView2.typeface = Typeface.createFromAsset(assets,"fonts/EduNSWACTFoundation-Regular.ttf")
         */



        // Obtain the FirebaseAnalytics instance.

        analytics = Firebase.analytics
        val bundle =  Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        //Set up
        setup()
        session()


    }

    override fun onStart() {
        super.onStart()

        binding.authLayout.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if( email != null && provider != null) {
            binding.authLayout.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        title = "Authentication"


        binding.singUpButton.setOnClickListener{
            if(binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()).addOnCompleteListener {

                            if (it.isSuccessful){

                                showHome(it.result.user?.email ?: "", ProviderType.BASIC)

                            }else{
                                showAlert("Error al registrar al usuario")
                            }
                    }
            }
        }

        binding.logInButton.setOnClickListener {
            if(binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(binding.emailEditText.text.toString(),
                        binding.passwordEditText.text.toString()).addOnCompleteListener {

                        if (it.isSuccessful){

                            showHome(it.result.user?.email ?: "", ProviderType.BASIC)

                        }else{
                            showAlert("Error al autenticar al usuario")
                        }
                    }
            }
        }
        val previewRequest =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val list = it.data
                    // do whatever with the data in the callback
                    val task = GoogleSignIn.getSignedInAccountFromIntent(list)

                    try {
                        val account = task.getResult(ApiException::class.java)

                        if ( account != null){
                            val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                                if (it.isSuccessful){
                                    showHome(account.email ?: "" , ProviderType.GOOGLE)
                                }else
                                    showAlert("Error al autenticarse con Google")
                            }
                        }
                    }catch (e: ApiException){
                        showAlert("Error al obtener la cuenta de Google")
                    }
                }
            }

        binding.googleButton.setOnClickListener {
            //Configuración

            val googleConf =  GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)

            //startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
            //startActivity(googleClient.signInIntent)
            previewRequest.launch(googleClient.signInIntent)


        }



    }



    private fun showHome( email : String, provider: ProviderType) {
        val homeIntent =  Intent(this, HomeActivity::class.java).apply{
            putExtra("email", email)
            putExtra("provider", provider.name)
        }

        startActivity(homeIntent)
    }

    private fun showAlert(message: String){
        val builder =  AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ( requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)

                if ( account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){
                            showHome(account.email ?: "" , ProviderType.GOOGLE)
                        }else
                            showAlert("Error al autenticarse con Google")
                    }
                }
            }catch (e: ApiException){
                showAlert("Error al obtener la cuenta de Google")
            }


        }
    }

     */
}