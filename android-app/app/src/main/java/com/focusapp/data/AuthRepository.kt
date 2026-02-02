package com.focusapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class User(
    val email: String,
    val passwordHash: String
)

class AuthRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_CURRENT_USER = "current_user"
    }
    
    // Get all users
    private fun getUsers(): MutableMap<String, User> {
        val json = prefs.getString(KEY_USERS, null) ?: return mutableMapOf()
        val type = object : TypeToken<MutableMap<String, User>>() {}.type
        return gson.fromJson(json, type) ?: mutableMapOf()
    }
    
    // Save users
    private fun saveUsers(users: Map<String, User>) {
        val json = gson.toJson(users)
        prefs.edit().putString(KEY_USERS, json).apply()
    }
    
    // Simple password hashing (for demo - use proper hashing in production)
    private fun hashPassword(password: String): String {
        return password.hashCode().toString()
    }
    
    // Sign up new user
    fun signUp(email: String, password: String): Boolean {
        val users = getUsers()
        
        // Check if user already exists
        if (users.containsKey(email)) {
            return false
        }
        
        // Create new user
        val user = User(email, hashPassword(password))
        users[email] = user
        saveUsers(users)
        
        // Auto sign in
        setCurrentUser(email)
        return true
    }
    
    // Sign in existing user
    fun signIn(email: String, password: String): Boolean {
        val users = getUsers()
        val user = users[email] ?: return false
        
        // Check password
        if (user.passwordHash != hashPassword(password)) {
            return false
        }
        
        // Set as current user
        setCurrentUser(email)
        return true
    }
    
    // Sign out
    fun signOut() {
        prefs.edit().remove(KEY_CURRENT_USER).apply()
    }
    
    // Get current user
    fun getCurrentUser(): String? {
        return prefs.getString(KEY_CURRENT_USER, null)
    }
    
    // Set current user
    private fun setCurrentUser(email: String) {
        prefs.edit().putString(KEY_CURRENT_USER, email).apply()
    }
    
    // Check if authenticated
    fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }
}
