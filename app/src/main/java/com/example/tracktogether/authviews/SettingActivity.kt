package com.example.tracktogether.authviews


import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.tracktogether.Interfaces.ISuccessFlag
import com.example.tracktogether.TrackTogetherApp
import com.example.tracktogether.databinding.ActivitySettingBinding
import com.example.tracktogether.viewmodel.SettingViewModel
import com.example.tracktogether.viewmodel.SettingViewModelFactory

class SettingActivity : AppCompatActivity(){

    private lateinit var binding: ActivitySettingBinding

    private val settingViewModel: SettingViewModel by viewModels {
        SettingViewModelFactory(
            (application as TrackTogetherApp).userPreferencesRepository,
            (application as TrackTogetherApp).authrepo,
            (application as TrackTogetherApp).imageRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSwitchState()

        binding.enableBiometricLogin.setOnClickListener {
            val isChecked = binding.enableBiometricLogin.isChecked
            settingViewModel.saveBiometricPref(isChecked)

        }
    }

    private fun setSwitchState() {
        settingViewModel.biometricPref.observe(this) { isChecked ->
            binding.enableBiometricLogin.isChecked = isChecked
        }
    }

}