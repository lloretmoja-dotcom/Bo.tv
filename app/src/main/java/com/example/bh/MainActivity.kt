package com.example.bh

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bh.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private val vm: MainViewModel by viewModels()
    private val adapter = ChannelAdapter { ch ->
        startActivity(Intent(this, PlayerActivity::class.java).apply {
            putExtra("name", ch.name)
            putExtra("url", ch.url)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter
        b.inputSearch.doAfterTextChanged { vm.setQuery(it?.toString() ?: "") }
        b.swipeRefresh.setOnRefreshListener { vm.load(force = true) }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.state.collect { s ->
                        b.progress.visibility = if (s.loading) View.VISIBLE else View.GONE
                        b.swipeRefresh.isRefreshing = false
                        b.textError.visibility = if (s.error != null) View.VISIBLE else View.GONE
                        b.textError.text = s.error ?: ""
                        adapter.submitList(s.channels)
                        b.textCount.text = "${s.channels.size} canales"
                    }
                }
                launch {
                    vm.countries.collect { list -> renderChips(list) }
                }
            }
        }
    }

    private fun renderChips(countries: List<String>) {
        b.chipGroup.removeAllViews()
        b.chipGroup.addView(Chip(this).apply {
            text = "Todos"
            isCheckable = true
            isChecked = true
            setOnClickListener { vm.setCountry(null) }
        })
        countries.take(40).forEach { country ->
            b.chipGroup.addView(Chip(this).apply {
                text = country
                isCheckable = true
                setOnClickListener { vm.setCountry(country) }
            })
        }
    }
}
