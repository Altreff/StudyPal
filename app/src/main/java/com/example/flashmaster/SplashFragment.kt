package com.example.flashmaster

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("DEBUG", "SplashFragment loaded")

        // jump to home page after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            // Only navigate if we're not already on the home fragment
            if (findNavController().currentDestination?.id != R.id.homeFragment) {
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            }
        }, 2000)
    }
}