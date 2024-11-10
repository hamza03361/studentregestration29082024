package com.example.studentregistration

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studentregistration.Apis.MonitoringLog
import com.example.studentregistration.Apis.Timing

class InOutTimeAdapter(private val timings: List<MonitoringLog>) :
    RecyclerView.Adapter<InOutTimeAdapter.InOutTimeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InOutTimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inandout, parent, false)
        return InOutTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: InOutTimeViewHolder, position: Int) {
        val timing = timings[position]
        holder.bind(timing)
    }

    override fun getItemCount(): Int = timings.size

    inner class InOutTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val inTimeTextView: TextView = itemView.findViewById(R.id.intime)
        private val outTimeTextView: TextView = itemView.findViewById(R.id.outtime)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)

        // Assuming MonitoringLog has the appropriate fields
        fun bind(timing: MonitoringLog) {
            dateTextView.text = timing.date ?: "N/A"            // Display date or "N/A" if null
            inTimeTextView.text = timing.timeIn ?: "N/A"        // Display time_in or "N/A" if null
            outTimeTextView.text = timing.timeOut ?: "N/A"      // Display time_out or "N/A" if null
        }



    }
}

