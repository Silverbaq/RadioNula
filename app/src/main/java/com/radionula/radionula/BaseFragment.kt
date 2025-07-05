package com.radionula.radionula

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

abstract class BaseFragment : Fragment() {
    protected fun <T> LiveData<T>.onResult(action: (T) -> Unit) {
        observe(viewLifecycleOwner, Observer { data -> data?.let(action) })
    }

    protected fun <T> LiveData<T>.onNullableResult(action: (T?) -> Unit) {
        observe(viewLifecycleOwner, Observer { data -> action.invoke(data) })
    }
}