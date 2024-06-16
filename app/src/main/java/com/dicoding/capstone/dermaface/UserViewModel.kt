package com.dicoding.capstone.dermaface.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.capstone.dermaface.UserRepository
import com.dicoding.capstone.dermaface.User
import com.google.firebase.auth.FirebaseAuth

class UserViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository(auth)
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    init {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser != null) {
            _user.value = userRepository.getUserDetails(currentUser)
        } else {
            _user.value = null
        }
    }

    fun signOut() {
        userRepository.signOut()
        _user.value = null
    }

    fun saveUserToken(token: String) {
        userRepository.saveUserToken(token)
    }
}
