package com.dicoding.capstone.dermaface

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserRepository(private val auth: FirebaseAuth) {

    private val preferences: UserPreferences = UserPreferences(auth.app.applicationContext)

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun getUserDetails(firebaseUser: FirebaseUser): User {
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName
        )
    }

    fun signOut() {
        auth.signOut()
    }

    fun saveUserToken(token: String) {
        preferences.saveUserToken(token)
    }
}
