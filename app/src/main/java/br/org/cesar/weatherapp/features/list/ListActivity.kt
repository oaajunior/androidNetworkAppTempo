package br.org.cesar.weatherapp.features.list

import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import br.org.cesar.weatherapp.features.setting.SettingActivity
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import br.org.cesar.weatherapp.Constants
import br.org.cesar.weatherapp.R
import br.org.cesar.weatherapp.api.RetrofitManager
import br.org.cesar.weatherapp.database.RoomManager
import br.org.cesar.weatherapp.database.WeatherDatabase
import br.org.cesar.weatherapp.entity.City
import br.org.cesar.weatherapp.entity.FavoriteCity
import br.org.cesar.weatherapp.entity.FindResult
import br.org.cesar.weatherapp.features.setting.ApplicationContextProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_preferences.*
import kotlinx.android.synthetic.main.row_city_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class ListActivity : AppCompatActivity() {

    val adapter = WeatherAdapter{}
    lateinit var tipoTemperatura: String
    lateinit var language :String
    val listCitiesInt :MutableList<Int> = ArrayList()
    lateinit var listCitiesStrings :String
    val prefs by lazy {
        getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
    }
     //Metodo que inicia a activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         listCitiesStrings = listFavoriteCitiesIDs()
        initUI()
        readPrefs()

        if(listCitiesStrings != null) listCitiesFavorites()
      }

    //Metodo que lê o banco de dados e atualiza a variável com uma lista
    fun listFavoriteCitiesIDs():String{

        if (listCitiesInt!!.size > 0) listCitiesInt!!.clear()

        RoomManager.instance(ApplicationContextProvider.context).favoriteDao().apply {

            selectAll().forEach {

            listCitiesInt?.add(it.id)

            }
        }
        if (listCitiesInt != null) return TextUtils.join(",", listCitiesInt) else return ""
    }

     /**
     * Salva uma cidade favorita no banco de dados de forma ASSÍNCRONA
     */
    fun saveFavoriteAsync(city: City) {
        FavoriteAsync(this).execute(city)
    }

    /**
     * Classe que herda de AsyncTask para salvar a cidade favorita de forma assíncrona
     */
    class FavoriteAsync(val context: Context) : AsyncTask<City, Void, List<FavoriteCity>>() {

        /**
         * Método executado em segundo plano
         */
        override fun doInBackground(vararg params: City?): List<FavoriteCity> {
            RoomManager.instance(context).favoriteDao().apply {
                params[0]?.let {
                    val (id,name) = it
                    insert(FavoriteCity(id, name))
                }
                return selectAll()
            }
        }

        /**
         * Método que será executado após o doInBackground.
         * Aqui, o processamento será realizado na Main Thread
         */
        override fun onPostExecute(result: List<FavoriteCity>?) {
            result?.apply {
                forEach {
                    Log.d("w", it.name) }
            }
        }
    }

    /**
     * Método que faz a requisição a Weather API e atualiza o recycler view quando é informado uma cidade
     */
    private fun refreshList() {

        progressBar.visibility = View.VISIBLE
        readPrefs()

        val rm = RetrofitManager()

        val call = rm.weatherService().find(
            edtSearch.text.toString(), language!!, tipoTemperatura!!,
            "5fde54966e3e1c8a80e436245bdf9672")

        call.enqueue(object : Callback<FindResult> {

            override fun onFailure(call: Call<FindResult>, t: Throwable) {
                progressBar.visibility = View.GONE
            }

            override fun onResponse(call: Call<FindResult>, response: Response<FindResult>) {
                if (response.isSuccessful) {
                    response.body()?.let { findResult ->
                        adapter.updataData(findResult.list, listCitiesInt)
                    }
                }
                progressBar.visibility = View.GONE
            }

        })
    }

    /**
     * Método que faz a requisição a Weather API e atualiza o recycler view quando há cidades já gravadas no banco
     */
    private fun listCitiesFavorites() {

        if(listCitiesStrings != null  && listCitiesStrings != "") {
            readPrefs()
            progressBar.visibility = View.VISIBLE

            val rm = RetrofitManager()

            val call = listCitiesStrings?.let {
                rm.weatherService().group(
                    it, language!!, tipoTemperatura!!,
                    "5fde54966e3e1c8a80e436245bdf9672"
                )
            }

            call?.enqueue(object : Callback<FindResult> {

                override fun onFailure(call: Call<FindResult>, t: Throwable) {
                    progressBar.visibility = View.GONE
                }

                override fun onResponse(call: Call<FindResult>, response: Response<FindResult>) {
                    if (response.isSuccessful) {
                        response.body()?.let { findResult ->
                            adapter.updataData(findResult.list, listCitiesInt)
                        }
                    }
                    progressBar.visibility = View.GONE
                }

            })
        }
        progressBar.visibility = View.GONE
    }

    /**
     * Inicializa os componentes da UI
     */
    private fun initUI() {

        btnSearch.setOnClickListener {

            listCitiesStrings = listFavoriteCitiesIDs()

            if (isDeviceConnected()) {

                if(edtSearch.text.toString() == "" ){

                    listCitiesFavorites()

                } else {

                    refreshList()
                }
            } else {
                Toast.makeText(this, "Desconectado", Toast.LENGTH_SHORT).show()
            }
        }

        // RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Verifica se o device está conectado a internet
     */
    private fun isDeviceConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    /**
     * Método que infla o menu na Actionbar/Toolbar
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.weather_menu, menu)
        return true
    }

    /**
     * Método que será invocado quando o item do menu for selecionado
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.settings_action) {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        return true
    }


    //Metodo que recupera as configurações da linguagem e tipo de temperatura
    private fun readPrefs() {
        val isTempC = (prefs.getBoolean(Constants.PREF_TEMP_C, false))
        val isLangEn = (prefs.getBoolean(Constants.PREF_LANG_EN, true))

        tipoTemperatura = if (isTempC) "metric" else "imperial"
        language = if (isLangEn) "en" else "pt"
    }
}
