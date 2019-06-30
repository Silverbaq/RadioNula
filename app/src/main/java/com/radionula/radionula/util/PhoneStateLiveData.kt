package com.radionula.radionula.util

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData


class PhoneStateLiveData @VisibleForTesting internal constructor(private val telephonyManager: TelephonyManager)
    : LiveData<Boolean>() {

    override fun onActive() {
        super.onActive()

        val phoneState = telephonyManager.callState
        postValue(phoneState == TelephonyManager.CALL_STATE_IDLE)

        telephonyManager.listen(teleListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    private val teleListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)

            when (state) {
                TelephonyManager.CALL_STATE_IDLE -> {
                    postValue(true)
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    postValue(false)
                }
                TelephonyManager.CALL_STATE_RINGING -> {
                    postValue(false)
                }
                else -> {
                    postValue(false)
                }
            }// TODO: Needs to restart music after a phone call. (If it was playing).
        }
    }

}