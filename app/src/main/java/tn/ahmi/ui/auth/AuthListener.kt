package tn.ahmi.ui.auth

import tn.ahmi.data.db.entities.User


interface AuthListener {
    fun onStarted()
    fun onSuccess(result: String)
    fun onFailure(message: String)
}