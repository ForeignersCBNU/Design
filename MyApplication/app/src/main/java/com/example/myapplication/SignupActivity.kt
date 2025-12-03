package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.RegBtn.setOnClickListener {
            val email = binding.Email.text.toString().trim()
            val pass = binding.Pass.text.toString().trim()
            val passConfirm = binding.PassConfirm.text.toString().trim()

            if(email.isEmpty() || pass.isEmpty() || passConfirm.isEmpty()){
                Toast.makeText(this, "Empty Field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if(pass != passConfirm){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if(pass.length < 7){
                Toast.makeText(this, "Passwords has to be at least 7 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if(task.isSuccessful){
                            val user = firebaseAuth.currentUser
                            user?.sendEmailVerification()
                                ?.addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        Toast.makeText(this, "Registration Complete", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }else{
                                        Toast.makeText(this, "Email Verification Failed", Toast.LENGTH_SHORT).show()
                                        return@addOnCompleteListener
                                    }

                                }
                        }else{
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }
                    }
            }
        }

        binding.BackToTitleBTN.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.CancelToTitle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}