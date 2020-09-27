package com.example.microbitlearner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.example.microbitlearner.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout

    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        drawer = binding.drawerLayout

        val navCtrl = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupActionBarWithNavController(this, navCtrl, drawer)

        // only allow the navigation drawer to appear on the title screen of the app
        navCtrl.addOnDestinationChangedListener { navCtrl: NavController, navDest: NavDestination, args: Bundle? ->
            if (navDest.id == navCtrl.graph.startDestination) { // unlock drawer on title screen
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            } else { // lock everywhere else
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }

        appBarConfig = AppBarConfiguration(navCtrl.graph, drawer)
        NavigationUI.setupWithNavController(binding.navView, navCtrl)
    }

    override fun onSupportNavigateUp(): Boolean { // set up the up navigation
        val navCtrl = this.findNavController(R.id.myNavHostFragment)
        return NavigationUI.navigateUp(navCtrl, appBarConfig)
    }
}