package com.example.cw2

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cw2.repo.WorkRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val jobs = mutableListOf<Job>()

    private var count = 1
    private var running = false

    private lateinit var tvCount: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnSequential: Button
    private lateinit var btnParallel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCount = findViewById(R.id.tv_count)
        tvStatus = findViewById(R.id.tv_status)
        btnSequential = findViewById(R.id.btn_sequential)
        btnParallel = findViewById(R.id.btn_parallel)
        val btnCancel: Button = findViewById(R.id.btn_cancel)
        val seekBar: SeekBar = findViewById(R.id.seek_bar)

        tvStatus.text = getString(R.string.status_idle)
        updateCountText()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                count = progress + 1
                updateCountText()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSequential.setOnClickListener {
            runSequential(count)
        }

        btnParallel.setOnClickListener {
            runParallel(count)
        }

        btnCancel.setOnClickListener {
            jobs.forEach { it.cancel() }
            jobs.clear()
            running = false
            updateButtons()
            tvStatus.text = getString(R.string.status_cancelled)
        }
    }

    private fun updateCountText() {
        tvCount.text = getString(R.string.coroutines_count_label, count)
    }

    private fun updateButtons() {
        btnSequential.isEnabled = !running
        btnParallel.isEnabled = !running
    }

    private fun runSequential(count: Int) {
        running = true
        updateButtons()
        val job = scope.launch {
            for (i in 1..count) {
                WorkRepository.doHeavyWork(i)
                withContext(Dispatchers.Main) {
                    tvStatus.text = getString(R.string.status_running, i, count)
                }
            }
            withContext(Dispatchers.Main) {
                tvStatus.text = getString(R.string.status_done)
                running = false
                updateButtons()
            }
        }
        jobs.add(job)
    }

    private fun runParallel(count: Int) {
        running = true
        updateButtons()
        var finished = 0
        val job = scope.launch {
            for (i in 1..count) {
                launch {
                    WorkRepository.doHeavyWork(i)
                    finished++
                    withContext(Dispatchers.Main) {
                        if (finished == count) {
                            tvStatus.text = getString(R.string.status_done)
                            running = false
                            updateButtons()
                        } else {
                            tvStatus.text = getString(R.string.status_running, finished, count)
                        }
                    }
                }
            }
        }
        jobs.add(job)
    }
}
