package com.example.shoes.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoes.Adapter.StatisticAdapter
import com.example.shoes.Model.OrderModel
import com.example.shoes.Model.StatisticModel
import com.example.shoes.R
import com.example.shoes.databinding.ActivityStatisticBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.database.*

class StatisticActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStatisticBinding
    private lateinit var database: DatabaseReference
    private val statisticList = mutableListOf<StatisticModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().reference

        setupUI()
        setupRecyclerView()
        loadStatistics()
    }

    private fun setupUI() {
        // Back button functionality
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewStatistic.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewStatistic.adapter = StatisticAdapter(statisticList)
    }

    private fun loadStatistics() {
        val productStats = mutableMapOf<String, StatisticModel>()

        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnap in snapshot.children) {
                    val ordersSnap = userSnap.child("listOrders")
                    for (orderSnap in ordersSnap.children) {
                        val order = orderSnap.getValue(OrderModel::class.java)
                        if (order != null && order.status == "Delivered") {
                            for (item in order.items) {
                                val key = item.title.trim().lowercase()
                                val stat = productStats[key] ?: StatisticModel(
                                    productId = key,
                                    productName = item.title,
                                    quantitySold = 0,
                                    revenue = 0.0
                                )
                                stat.quantitySold += item.numberInCart
                                stat.revenue += item.numberInCart * item.price
                                productStats[key] = stat
                            }
                        }
                    }
                }

                statisticList.clear()
                statisticList.addAll(productStats.values)
                binding.recyclerViewStatistic.adapter?.notifyDataSetChanged()

                setupBarChart(statisticList)
                updateTotalRevenue(statisticList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StatisticActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun setupBarChart(statistics: List<StatisticModel>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        statistics.forEachIndexed { index, stat ->
            entries.add(BarEntry(index.toFloat(), stat.quantitySold.toFloat()))
            labels.add(stat.productName)
        }

        val dataSet = BarDataSet(entries, getString(R.string.quantity_sold, 0).replace(": 0", ""))
        dataSet.color = resources.getColor(android.R.color.holo_blue_light, theme)

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        binding.barChart.apply {
            data = barData
            setFitBars(true)
            description.isEnabled = false
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                granularity = 1f
                setDrawGridLines(false)
                labelRotationAngle = -45f
            }
            animateY(1000)
            invalidate()
        }
    }

    private fun updateTotalRevenue(statistics: List<StatisticModel>) {
        val totalRevenue = statistics.sumOf { it.revenue }
        binding.totalRevenueTextView.text = getString(R.string.total_revenue, totalRevenue)
    }
}
