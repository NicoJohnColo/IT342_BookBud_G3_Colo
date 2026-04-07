package edu.cit.colo.bookbud

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val textGoodMorning: TextView = findViewById(R.id.textGoodMorning)
        val textUsername: TextView = findViewById(R.id.textUsername)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)

        // Load username from SharedPreferences
        val prefs = getSharedPreferences("bookbud_prefs", MODE_PRIVATE)
        val username = prefs.getString("username", "Reader")
        textUsername.text = username ?: "Reader"

        // Entrance animations
        animateEntrance(textGoodMorning, textUsername)

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_explore -> true
                R.id.nav_listings -> true
                R.id.nav_chats -> true
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    private fun animateEntrance(vararg views: View) {
        views.forEachIndexed { index, view ->
            val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
                duration = 500; startDelay = (index * 100).toLong()
            }
            val transY = ObjectAnimator.ofFloat(view, "translationY", 20f, 0f).apply {
                duration = 500; startDelay = (index * 100).toLong()
                interpolator = DecelerateInterpolator()
            }
            AnimatorSet().apply { playTogether(alpha, transY); start() }
        }
    }
}
