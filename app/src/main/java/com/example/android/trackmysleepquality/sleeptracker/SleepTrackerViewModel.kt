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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*
/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {
        //1.create viewModelJOb and and override onCleared() to cancel
        private var viewModelJob=Job()

        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }
        //2.define scope for coroutines to run in
        private val uiScope= CoroutineScope(Dispatchers.Main+viewModelJob) //Main mains run on mainthread


        //3.create toight livedata var and use a coroutine to initialize it from DB
        private var tonight = MutableLiveData<SleepNight?>()

        //4. getAllNights from DB
         val nights = database.getAllNights()


        private val _navigateToSleepQuality=MutableLiveData<SleepNight>()

        val navigateToSleepQuality :LiveData<SleepNight>
        get()=_navigateToSleepQuality

        //snackbar after clearig data
        private var _showSnackbarEvent = MutableLiveData<Boolean>()

        val showSnackBarEvent: LiveData<Boolean>
                get() = _showSnackbarEvent

        fun doneNavigating()
        {
                _navigateToSleepQuality.value=null
        }

        fun doneShowingSnackbar() {
                _showSnackbarEvent.value = false
        }


        /**
         * Converted nights to Spanned for displaying.
         */
        val nightsString = Transformations.map(nights) { nights ->
                formatNights(nights, application.resources)
        }
        //bottons visibility
        val startButtonVisible=Transformations.map(tonight){
                null==it
        }
        val stopButtonVisible=Transformations.map(tonight){
                null!=it
        }
        val clearButtonVisible=Transformations.map(nights){
                it?.isNotEmpty()
        }


        init {
                initializeTonight()
        }

        private fun initializeTonight() {
                uiScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun getTonightFromDatabase(): SleepNight? {
                return withContext(Dispatchers.IO) {
                        var night = database.getTonight()
                        if (night?.endTimeMillis != night?.startTimeMillis) {
                                night = null
                        }

                        night
                }

        }

        fun onStartTracking() {
                viewModelScope.launch {
                        // Create a new night, which captures the current time,
                        // and insert it into the database.
                        val newNight = SleepNight()

                        insert(newNight)

                        tonight.value = getTonightFromDatabase()
                }
        }



        //add local functions to update(),insert() and clear()








        /**
         *  Handling the case of the stopped app or forgotten recording,
         *  the start and end times will be the same.j
         *
         *  If the start time and end time are not the same, then we do not have an unfinished
         *  recording.
         */


        private suspend fun clear() {
                withContext(Dispatchers.IO) {
                        database.clear()
                }
        }

        private suspend fun update(night: SleepNight) {
                withContext(Dispatchers.IO) {
                        database.update(night)
                }
        }

        private suspend fun insert(night: SleepNight) {
                withContext(Dispatchers.IO) {
                        database.insert(night)
                }
        }

        /**
         * Executes when the START button is clicked.
         */


        /**
         * Executes when the STOP button is clicked.
         */
        fun onStopTracking() {
                viewModelScope.launch {
                        // In Kotlin, the return@label syntax is used for specifying which function among
                        // several nested ones this statement returns from.
                        // In this case, we are specifying to return from launch(),
                        // not the lambda.
                        val oldNight = tonight.value ?: return@launch

                        // Update the night in the database to add the end time.
                        oldNight.endTimeMillis = System.currentTimeMillis()

                        update(oldNight)
                        _navigateToSleepQuality.value=oldNight
                }
        }



        /**
         * Executes when the CLEAR button is clicked.
         */
        fun onClear() {
                uiScope.launch {
                        // Clear the database table.
                        clear()

                        // And clear tonight since it's no longer in the database
                        tonight.value = null

                        _showSnackbarEvent.value = true
                }
        }


}

