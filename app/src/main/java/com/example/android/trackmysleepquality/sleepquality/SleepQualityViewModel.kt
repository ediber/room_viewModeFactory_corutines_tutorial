/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleepquality

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

class SleepQualityViewModel(
        val database: SleepDatabaseDao,
        private val sleepNightKey: Long = 0L) : ViewModel(){

    private var viewModelJob = Job()

    private val uiScope = CoroutineScope(Dispatchers.Main +  viewModelJob)
    private var tonight = MutableLiveData<SleepNight?>()

    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker : LiveData<Boolean?>
        get() = _navigateToSleepTracker


    init{
        initializeTonight()
      //  _navigateToSleepTracker.value = false
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }

    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.get(sleepNightKey)

            night
        }
    }

    fun onSetSleepQuality(quality : Int){
        tonight.value?.sleepQuality  = quality
        uiScope.launch {
            withContext(Dispatchers.IO){
                database.update(tonight.value!!)
            }
        }

        _navigateToSleepTracker.value = true
    }

    fun doneNavigating() {
        _navigateToSleepTracker.value = null
    }


    override fun onCleared() {
        super.onCleared()

        viewModelJob.cancel()
    }


}
