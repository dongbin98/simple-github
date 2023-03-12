package com.dbsh.simplegithub.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import com.dbsh.simplegithub.databinding.ActivityMainBinding
import com.dbsh.simplegithub.ui.search.SearchActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnActivityMainSearch.setOnClickListener {
            startActivity(Intent(this@MainActivity,
                SearchActivity::class.java))
        }
    }
}