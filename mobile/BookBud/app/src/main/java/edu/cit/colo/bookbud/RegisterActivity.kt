package edu.cit.colo.bookbud

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.activity.ComponentActivity

class RegisterActivity : ComponentActivity() {

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val textTitle: TextView = findViewById(R.id.textRegisterTitle)
        val editName: EditText = findViewById(R.id.editName)
        val editEmail: EditText = findViewById(R.id.editEmail)
        val editPassword: EditText = findViewById(R.id.editPassword)
        val editConfirmPassword: EditText = findViewById(R.id.editConfirmPassword)
        val togglePassword: ImageView = findViewById(R.id.togglePassword)
        val toggleConfirmPassword: ImageView = findViewById(R.id.toggleConfirmPassword)
        val layoutPassword: FrameLayout = findViewById(R.id.layoutPassword)
        val layoutConfirm: FrameLayout = findViewById(R.id.layoutConfirmPassword)
        val progress: ProgressBar = findViewById(R.id.progressRegister)
        val buttonRegister: Button = findViewById(R.id.buttonRegister)
        val layoutHaveAccount: LinearLayout = findViewById(R.id.layoutHaveAccount)
        val textLoginLink: TextView = findViewById(R.id.textLoginLink)

        // Entrance animations
        animateEntrance(textTitle, editName, editEmail, layoutPassword, layoutConfirm, buttonRegister, layoutHaveAccount)

        // Password visibility toggles
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            toggleVisibility(editPassword, togglePassword, isPasswordVisible)
        }
        toggleConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            toggleVisibility(editConfirmPassword, toggleConfirmPassword, isConfirmPasswordVisible)
        }

        textLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        buttonRegister.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }

        buttonRegister.setOnClickListener {
            val name = editName.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val password = editPassword.text.toString()
            val confirmPassword = editConfirmPassword.text.toString()

            if (!validateInputs(name, email, password, confirmPassword, editName, editEmail, editPassword, editConfirmPassword)) return@setOnClickListener

            progress.visibility = View.VISIBLE
            buttonRegister.isEnabled = false

            Thread {
                val result = AuthApiClient.register(name, email, password, confirmPassword)
                runOnUiThread {
                    progress.visibility = View.GONE
                    buttonRegister.isEnabled = true
                    when (result) {
                        is AuthResult.Success -> {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                            finish()
                        }
                        is AuthResult.Error -> Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }

    private fun animateEntrance(vararg views: View) {
        views.forEachIndexed { index, view ->
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 500
                startDelay = (index * 80).toLong()
            }
            val transY = ObjectAnimator.ofFloat(view, "translationY", 40f, 0f).apply {
                duration = 500
                startDelay = (index * 80).toLong()
                interpolator = DecelerateInterpolator()
            }
            AnimatorSet().apply { playTogether(alpha, transY); start() }
        }
    }

    private fun toggleVisibility(editText: EditText, icon: ImageView, visible: Boolean) {
        val cursorPos = editText.selectionEnd
        if (visible) {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icon.setImageResource(R.drawable.ic_eye_on)
        } else {
            editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            icon.setImageResource(R.drawable.ic_eye_off)
        }
        editText.setSelection(cursorPos)
    }

    private fun validateInputs(
        name: String, email: String, password: String, confirmPassword: String,
        editName: EditText, editEmail: EditText, editPassword: EditText, editConfirmPassword: EditText
    ): Boolean {
        if (name.length < 2) {
            editName.error = "Name is required"
            editName.requestFocus()
            return false
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.error = "Valid email is required"; editEmail.requestFocus(); return false
        }
        if (password.length < 6) {
            editPassword.error = "Password must be at least 6 characters"; editPassword.requestFocus(); return false
        }
        if (password != confirmPassword) {
            editConfirmPassword.error = "Passwords do not match"; editConfirmPassword.requestFocus(); return false
        }
        return true
    }
}
