package com.kotdroid.kotdroidtestfirebase

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.auth.FirebaseUser



class LoginActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var editForgotPassword: EditText? = null

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "GoogleActivity"
    private val RC_SIGN_IN = 7021

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        initEmail()
        initGoogle()

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth!!.getCurrentUser()
       if (currentUser != null )
           startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    }

    private fun initGoogle() {

        //تهيئة GoogleSignInOptions
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<View>(R.id.btnGoogleSignIn).setOnClickListener{view->
            signIn()
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // فحص requestCode لمعرفة رقم الطلب بحالتنا تم وضع الرقم 7021
        // اذا كان رقم الطلب متطابق يبداءتسجيل الدخول عن طريق ارسال  البيانات الى firebaseAuthWithGoogle
        // وبداخلها سوف يكون الخاص بتسجيل الدخول
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account, btnGoogleSignIn.rootView)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)

            }

        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.getSignInIntent()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, view: View) {
        showMessage(view, "Authentication... ") // اظهار رسالة للمستخدم عند بدء تسجيل الدخول

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth!!.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    // فحس task اذا تم الامر بنجاح يتم تحويل المستخدم الى واجهةmain activity
                    // اما اذا حدث خطأ في تسجيل الدخول يرمي استثناء
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        showMessage(view, "Sign In Success")
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showMessage(view, "Error ${task.exception?.message}")
                    }
                })
    }

    private fun initEmail() {
        val btnSignUp = findViewById<View>(R.id.btnSignUp) as Button
        val btnSignIn = findViewById<View>(R.id.btnSignIn) as Button
        val btnForgotPassword = findViewById<View>(R.id.btnForgotPassword) as Button
        val edtEmail = findViewById<View>(R.id.edt_email) as EditText
        val edtPassword = findViewById<View>(R.id.edt_password) as EditText
        editForgotPassword = findViewById<View>(R.id.EditForgotPassword) as EditText

        btnSignUp.setOnClickListener { view ->
            if (validateForm(edtEmail.text.toString(), edtPassword.text.toString(), view)) {
                signUp(view, edtEmail.text.toString(), edtPassword.text.toString())
            }
        }

        btnSignIn.setOnClickListener { view ->
            if (validateForm(edtEmail.text.toString(), edtPassword.text.toString(), view)) {
                signIn(view, edtEmail.text.toString(), edtPassword.text.toString())
            }
        }

        btnForgotPassword.setOnClickListener { view ->
            passwordForget(view)
        }
    }

    private fun passwordForget(view: View) {

        val email = editForgotPassword!!.text.toString()

        if (email.isBlank()) {
            showMessage(view, "Enter your email!")
            return
        }

        auth!!.sendPasswordResetEmail(email).addOnCompleteListener(this,
                OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showMessage(view, "Check your email ")
                    } else {
                        showMessage(view, "Fail to send reset password  ${task.exception?.message}")

                    }
                })
    }

    private fun signUp(view: View, email: String, pass: String) {
        showMessage(view, "Authentication... ")
        auth!!.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                showMessage(view, "Successful")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            } else {
                showMessage(view, "Error ${task.exception?.message}")

            }
        })
    }

    private fun signIn(view: View, email: String, pass: String) {
        showMessage(view, "Authentication... ")
        auth!!.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                showMessage(view, "Successful")
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            } else {
                showMessage(view, "Error ${task.exception?.message}")
            }
        })
    }

    private fun showMessage(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).setAction("FIRE", null).show()
    }

    private fun validateForm(email: String, password: String, view: View): Boolean {

        if (TextUtils.isEmpty(email)) {
            showMessage(view, "Enter email address!")
            return false
        }

        if (TextUtils.isEmpty(password)) {
            showMessage(view, "Enter password!")
            return false
        }

        if (password.length < 8) {
            showMessage(view, "Password too short, enter minimum 8 characters!")
            return false
        }

        return true
    }
}
