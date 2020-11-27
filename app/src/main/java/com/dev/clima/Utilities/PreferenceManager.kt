package com.dev.clima.Utilities

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(val context: Context) {

    val PREF_NAME = "clima_prefs"
    var FULL_NAME = "FULL_NAME"
    var USER_ID = "USER_ID"
    var CREDIT_BALANCE = "CREDIT_BALANCE"
    var TOTAL_SCANS = "TOTAL_SCANS"

    var sharedPreferences: SharedPreferences? = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    var preferenceEditor: SharedPreferences.Editor? = sharedPreferences?.edit()


    fun PreferenceManager(context: Context?){
        sharedPreferences = context?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        preferenceEditor = sharedPreferences?.edit()
    }

    fun getFullName(): String? {
        return sharedPreferences!!.getString(FULL_NAME, "")
    }

    fun setFullName(fullName: String?) {
        preferenceEditor!!.putString(FULL_NAME, fullName)
        preferenceEditor!!.commit()
    }

    fun getUserId(): String? {
        return sharedPreferences!!.getString(USER_ID, "")
    }

    fun setUserId(userId: String?) {
        preferenceEditor!!.putString(USER_ID, userId)
        preferenceEditor!!.commit()
    }

    fun getCreditBalance(): String? {
        return sharedPreferences!!.getString(CREDIT_BALANCE, "")
    }

    fun setCreditBalance(creditBalance: String?) {
        preferenceEditor!!.putString(CREDIT_BALANCE, creditBalance)
        preferenceEditor!!.commit()
    }

    fun getTotalScans(): String? {
        return sharedPreferences!!.getString(TOTAL_SCANS, "")
    }

    fun setTotalScans(totalScans: String?) {
        preferenceEditor!!.putString(TOTAL_SCANS, totalScans)
        preferenceEditor!!.commit()
    }

}