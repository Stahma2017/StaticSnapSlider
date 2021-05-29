package ru.myapplication.sdslidersample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import ru.myapplication.sdslidersample.ui.custom.IndicatorSeekBar
import ru.myapplication.sdslidersample.ui.custom.OnSeekChangeListener
import ru.myapplication.sdslidersample.ui.custom.SeekParams

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

}