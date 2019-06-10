package br.org.cesar.weatherapp.features.setting
import android.app.Application
import android.content.Context

class ApplicationContextProvider : Application() {

    override fun onCreate() {
        super.onCreate()

        context = applicationContext

    }

    companion object {

        var context: Context? = null
            private set
    }

}