package com.example.appenggo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.appenggo.R
import com.example.appenggo.RetrofitClient
import com.example.appenggo.repository.AuthRepository
import com.example.appenggo.viewmodel.AuthResult
import com.example.appenggo.viewmodel.AuthViewModel
import com.example.appenggo.viewmodel.AuthViewModelFactory
import com.example.appenggo.websocket.WebSocketManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        
        btnLogout = view.findViewById(R.id.btn_logout)
        btnBack = view.findViewById(R.id.btn_back)
        
        val btnProfile = view.findViewById<View>(R.id.btn_profile_info)
        btnProfile.setOnClickListener {
            val intent = Intent(requireContext(), ProfileInfoActivity::class.java)
            startActivity(intent)
        }

        val btnChangePassword = view.findViewById<View>(R.id.btn_change_password)
        btnChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        setupViewModel()
        setupListeners()
        observeViewModel()
        
        return view
    }

    private fun setupViewModel() {
        val repository = AuthRepository(RetrofitClient.api)
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            handleLogout()
        }

        btnBack.setOnClickListener {
            // Chuyển BottomNavigation về tab Home.
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav?.selectedItemId = R.id.nav_home
        }
    }

    private fun observeViewModel() {
        authViewModel.logoutResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthResult.Loading -> {
                    btnLogout.isEnabled = false
                }
                is AuthResult.Success -> {
                    clearUserSession()
                    navigateToLogin()
                }
                is AuthResult.Error -> {
                    btnLogout.isEnabled = true
                    Toast.makeText(context, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show()
                    clearUserSession()
                    navigateToLogin()
                }
            }
        }
    }

    private fun handleLogout() {
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("TOKEN", "") ?: ""
        
        if (token.isNotEmpty()) {
            authViewModel.logout(token)
        } else {
            clearUserSession()
            navigateToLogin()
        }
    }

    private fun clearUserSession() {
        val sharedPrefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        WebSocketManager.disconnect()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}