package com.example.fintechpagoflex.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfilePreferences private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    private val _name = MutableStateFlow(getName())
    val name: StateFlow<String> = _name.asStateFlow()

    private val _photoUri = MutableStateFlow(getPhotoUri())
    val photoUri: StateFlow<String?> = _photoUri.asStateFlow()

    fun getName(): String {
        return prefs.getString("name", "Juan Pérez") ?: "Juan Pérez"
    }

    fun saveName(name: String) {
        prefs.edit().putString("name", name).apply()
        _name.value = name
    }

    fun getPhotoUri(): String? {
        return prefs.getString("photoUriString", null)
    }

    fun savePhotoUri(uri: String?) {
        prefs.edit().putString("photoUriString", uri).apply()
        _photoUri.value = uri
    }

    companion object {
        @Volatile
        private var INSTANCE: ProfilePreferences? = null

        fun getInstance(context: Context): ProfilePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ProfilePreferences(context).also { INSTANCE = it }
            }
        }
    }
}