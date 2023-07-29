package com.application.test.bases

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Hessam.R on 7/25/23 - 2:46 PM
 */
abstract class BaseActivity<V : ViewModel>(open var classType: Class<V>) : FragmentActivity(){

    protected val viewModel: V by lazy {
        ViewModelProvider(this)[classType]
    }

}