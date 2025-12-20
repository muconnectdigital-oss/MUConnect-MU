package com.rsservice.muconnect.ui.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.rsservice.muconnect.R

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        // Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Drawer item click
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.menu_dashboard -> {
                    // Already on dashboard
                }

                R.id.menu_add_student -> {
                    startActivity(
                        Intent(this, AddStudentActivity::class.java)
                    )
                }
                R.id.menu_manage_student ->{
                    startActivity(
                        Intent(this, ManageStudentsActivity::class.java)
                    )
                }

                R.id.menu_add_teacher -> {
                    startActivity(
                        Intent(this, AddTeacherActivity::class.java)
                    )
                }

                R.id.menu_logout -> {
                    finish()
                }
                R.id.menu_create_class ->{
                    startActivity(
                        Intent(this, CreateClassActivity::class.java)
                    )
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }
}
