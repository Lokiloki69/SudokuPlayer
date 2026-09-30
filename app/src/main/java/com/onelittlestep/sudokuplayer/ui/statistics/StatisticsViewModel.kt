package com.onelittlestep.sudokuplayer.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onelittlestep.sudokuplayer.model.AllStatistics
import com.onelittlestep.sudokuplayer.repository.StatisticsRepository

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val statisticsRepository = StatisticsRepository(application)

    private val _statistics = MutableLiveData<AllStatistics>()
    val statistics: LiveData<AllStatistics> = _statistics

    fun refresh() {
        _statistics.value = statisticsRepository.getAll()
    }
}
