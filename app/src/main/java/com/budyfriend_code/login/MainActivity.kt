package com.budyfriend_code.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.view.View
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var context: Context
    private lateinit var pref: preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        pref = preferences(context)

        btn_login.setOnClickListener(View.OnClickListener {
            val username: String = et_username.text.toString()
            val password: String = et_password.text.toString()
            if (username.isEmpty()) {
                et_username.error = "Data tidak boleh kosong"
                et_username.requestFocus()
            } else if (password.isEmpty()) {
                et_password.error = "Data tidak boleh kosong"
                et_password.requestFocus()
            } else {
                val query: Query = database.child("users").orderByChild("phone").equalTo(username)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (item in snapshot.children) {
                                val user = item.getValue<userData>()
                                if (user != null) {
                                    if (user.password.equals(password)) {
                                        pref.prefStatus = true
                                        pref.prefLevel = user.level
                                        var intent: Intent? = null
                                        intent = if (user.level.equals("admin")) {
                                            Intent(context, AdminActivity::class.java)
                                        } else {
                                            Intent(context, UserActivity::class.java)
                                        }
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(context,
                                            "Kata sandi belum sesuai",
                                            Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Username belum terdaftar", Toast.LENGTH_LONG)
                                .show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    }

                })
            }
        })

    }

    override fun onStart() {
        super.onStart()
        if (pref.prefStatus) {
            var intent: Intent? = null
            intent = if (pref.prefLevel.equals("admin")) {
                Intent(context, AdminActivity::class.java)
            } else {
                Intent(context, UserActivity::class.java)
            }
            startActivity(intent)
            finish()
        }
    }
}