package com.winnyking.wincalendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.winnyking.wincalendar.databinding.ActivityMainBinding

class ActivityMain : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Links to original sample repositories
        binding.buttonOpenGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginSample"))
            startActivity(intent)
        }
        binding.buttonOpenPluginPage.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/joaomgcd/TaskerPluginKotlin"))
            startActivity(intent)
        }
    }
}
