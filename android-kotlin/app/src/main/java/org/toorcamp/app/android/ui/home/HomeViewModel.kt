package org.toorcamp.app.android.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "If there's time, this is where the schedule will go."
    }
    val text: LiveData<String> = _text
}