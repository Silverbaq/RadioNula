package com.radionula.radionula

import android.telephony.TelephonyManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.radionula.radionula.util.PhoneStateLiveData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PhoneStateLiveDataTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var telephonyManager : TelephonyManager
    private lateinit var phoneStateLiveData: PhoneStateLiveData

    @Before
    fun before() {
        telephonyManager = mock()

        phoneStateLiveData = PhoneStateLiveData(telephonyManager)
    }

    @Test
    fun idle_state_is_true() {
        whenever(telephonyManager.callState).thenReturn(TelephonyManager.CALL_STATE_IDLE)

        val observer = mock<Observer<Boolean>>()
        phoneStateLiveData.observeForever(observer)

        verify(observer).onChanged(true)
    }

    @Test
    fun offhook_state_is_false() {
        whenever(telephonyManager.callState).thenReturn(TelephonyManager.CALL_STATE_OFFHOOK)

        val observer = mock<Observer<Boolean>>()
        phoneStateLiveData.observeForever(observer)

        verify(observer).onChanged(false)
    }

    @Test
    fun ringing_state_is_false() {
        whenever(telephonyManager.callState).thenReturn(TelephonyManager.CALL_STATE_RINGING)

        val observer = mock<Observer<Boolean>>()
        phoneStateLiveData.observeForever(observer)

        verify(observer).onChanged(false)
    }
}