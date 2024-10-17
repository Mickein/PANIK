package za.co.varsitycollege.st10215473.pank

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _selectedLanguage = MutableLiveData<String>().apply { value = "en" } // Default to English
    val selectedLanguage: LiveData<String> get() = _selectedLanguage

    fun setLanguage(language: String) {
        _selectedLanguage.value = language
    }
}
