package tk.romanaugusto.expense_tracker_app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _greeting = MutableLiveData<String>().apply {
        value = "Hello"
    }
    val greeting: MutableLiveData<String> = _greeting
}