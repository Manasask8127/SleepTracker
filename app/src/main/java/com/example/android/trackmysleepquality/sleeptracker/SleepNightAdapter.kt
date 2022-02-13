package com.example.android.trackmysleepquality.sleeptracker

import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight

class SleepNightAdapter : RecyclerView.Adapter<TextItemViewHolder>() {
    var data = listOf<SleepNight>()

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        TODO("Not yet implemented")
        val item=data[position]
        holder.textView.text=item.sleepQuality.toString()
    }
}