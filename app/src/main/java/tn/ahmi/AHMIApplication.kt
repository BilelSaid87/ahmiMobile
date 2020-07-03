package tn.ahmi

import android.app.Application
import android.content.Context
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import tn.ahmi.data.db.AppDatabase
import tn.ahmi.data.network.MyApi
import tn.ahmi.data.network.NetworkConnectionInterceptor
import tn.ahmi.data.preferences.PreferenceProvider
import tn.ahmi.data.repositories.UserRepository
import tn.ahmi.ui.auth.AuthViewModelFactory
import tn.ahmi.util.LocaleHelper

class AHMIApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@AHMIApplication))
        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { MyApi(instance()) }
        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { PreferenceProvider(instance()) }
        bind() from singleton { UserRepository(instance(), instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(base!!,""))
    }

}