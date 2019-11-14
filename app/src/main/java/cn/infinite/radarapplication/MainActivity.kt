package cn.infinite.radarapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        radarView.spotCount=3

        btnGenerate.setOnClickListener {
            radarView.generateSpot(5)
        }
    }
}
