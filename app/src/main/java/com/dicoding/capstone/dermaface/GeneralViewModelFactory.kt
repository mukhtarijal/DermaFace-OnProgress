package com.dicoding.capstone.dermaface

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GeneralViewModelFactory(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DetailHistoryViewModel::class.java) -> {
                DetailHistoryViewModel(firestore, auth) as T
            }
            modelClass.isAssignableFrom(ArticleViewModel::class.java) -> {
                ArticleViewModel(ArticleRepository()) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
