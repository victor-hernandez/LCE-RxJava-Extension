package com.jpardogo.example.presentation.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.jpardogo.example.presentation.common.di.ViewModelFactory
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

abstract class BaseDaggerActivity : BaseActivity(), HasFragmentInjector,
    HasSupportFragmentInjector {

    @Inject
    lateinit var vmf: ViewModelFactory

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Suppress("DEPRECATION")
    @Inject
    lateinit var frameworkFragmentInjector: DispatchingAndroidInjector<android.app.Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = supportFragmentInjector

    @Suppress("DEPRECATION")
    override fun fragmentInjector(): AndroidInjector<android.app.Fragment> =
        frameworkFragmentInjector
}