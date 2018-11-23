package com.b5eg.sergburlaka.cropimagetest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<CustomRombImageView>(R.id.civ_test).apply {
            Glide
                    .with(this@MainActivity)
                    .load("https://animals.sandiegozoo.org/sites/default/files/2016-08/animals_hero_bald_eagle.jpg")
                    .into(this)
        }
    }
}
