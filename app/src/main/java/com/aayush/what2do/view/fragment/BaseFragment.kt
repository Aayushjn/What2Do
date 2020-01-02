package com.aayush.what2do.view.fragment

import android.content.Context
import androidx.fragment.app.Fragment

abstract class BaseFragment: Fragment() {
    protected var parentContext: Context? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentContext = context
    }

    override fun onDetach() {
        super.onDetach()
        parentContext = null
    }
}