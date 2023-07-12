package com.atomgroups.progressbarsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.atomgroups.progressbar.ProgressBar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tierProgressBar =  findViewById<ProgressBar>(R.id.tierProgressBar)
        tierProgressBar.setDataModelView(
            listOf(
                ModelState("Tier 1", 10, "#FF0000"),
                ModelState("Tier 2", 20, "#00FF00"),
                ModelState("Tier 3", 30, "#0000FF"),
                ModelState("Tier 4", 40, "#FFFF00"),
            )
        )
    }
}