package br.org.cesar.weatherapp.features.list

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.org.cesar.weatherapp.Constants
import br.org.cesar.weatherapp.R
import br.org.cesar.weatherapp.database.RoomManager
import br.org.cesar.weatherapp.entity.City
import br.org.cesar.weatherapp.entity.FavoriteCity
import br.org.cesar.weatherapp.features.setting.ApplicationContextProvider
import br.org.cesar.weatherapp.features.setting.SettingActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.row_city_layout.view.*

/**
 *
 * Classe que cria um Adapter para o nosso RecyclerView
 * @sample https://medium.com/android-dev-br/listas-com-recyclerview-d3f41e0d653c
 *
 */
class WeatherAdapter(private val callback: (City) -> Unit) : RecyclerView.Adapter<WeatherAdapter.MyViewHolder>() {

    private var list: List<City>? = null
    private var listCitiesFavorites: List<Int>? = null


    /**
     * Método responsável por inflar a view e retornar um ViewHolder
     */
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        position: Int): MyViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_city_layout, viewGroup, false)

        return MyViewHolder(view)
    }

    /**
     * Método que retorna a quantidade de itens da lista
     *
     * Aqui utilizamos o operador Elvis Operator ?:
     * https://www.concrete.com.br/2017/06/21/kotlin-no-tratamento-de-erros/
     */
    override fun getItemCount() = list?.size ?: 0

    /**
     * Método responsável por realizar o bind da View com o item
     *
     * @param vh Nosso viewholder criado para reciclar as views
     * @param position posição do item que será inflado no recyclerview
     */
    override fun onBindViewHolder(vh: MyViewHolder, position: Int) {
        list?.let {
            vh.bind(it[position],listCitiesFavorites, callback)
        }
    }

    /**
     * Classe responsável por fazer o bind da View com o objeto City
     */
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        /**
         * Método que faz o bind
         *
         * @param city objeto a ser exibido
         * @param callback expressão lambda que será invokada quando a view for clicada/tocada
         *
         *
         */

        val prefs by lazy {
            ApplicationContextProvider.context?.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)
        }


        fun bind(city: City, listFavoriteCity: List<Int>?, callback: (City) -> Unit) {

            //Variaveis que recuperam as configurações da linguagem e tipo de temperatura

            val isTempC = (prefs?.getBoolean(Constants.PREF_TEMP_C, false))
            val isLangEn = (prefs?.getBoolean(Constants.PREF_LANG_EN, true))


            itemView.tvCityCountry.text = "${city.name}, ${city.systemCountry.country}"
            itemView.tvClouds.text = "${city.weathers[0].description}"
            itemView.tvTemp.text = "${city.temperature.temp.toInt()}"
            itemView.tvTempType.text = if(isTempC!!) "ºC" else "ºF"
            if(isLangEn!!){

                itemView.tvDetails.text = "wind: ${city.wind.speed.toInt()} clouds: ${city.clouds.all } ${city.temperature.pressure.toInt()} hpa "
            }else{

                itemView.tvDetails.text = "vento: ${city.wind.speed.toInt()} nuvens: ${city.clouds.all } ${city.temperature.pressure.toInt()} mbar "
            }

            listFavoriteCity.let{
                it?.forEach {
                    if (it == city.id){

                        itemView.cbFavorites.isChecked = true

                    }else{

                        itemView.cbFavorites.isChecked = false
                    }
                }
            }

            /**
             * Glide é uma lib opensource para facilitar o carregamento de imagens de forma eficiente
             * @sample https://github.com/bumptech/glide
              */
            Glide.with(itemView.context)
                .load("http://openweathermap.org/img/w/${city.weathers[0].icon}.png")
                .placeholder(R.drawable.w_01d)
                .into(itemView.imgIcon)

//            itemView.setOnClickListener {
//                callback(city)
//            }

            itemView.cbFavorites.setOnCheckedChangeListener { buttonView, isChecked ->

                if (isChecked){

                   saveFavorite(city)
                }else{

                    deleteFavorite(city)
                }
            }

            callback(city)
        }

        /**
         * Salva uma cidade favorita no bando de dados de forma SÍNCRONA
         */
        fun saveFavorite(city: City) {

            RoomManager.instance(ApplicationContextProvider.context).favoriteDao().apply {
                val (id,name ) = city
                insert(FavoriteCity(id, name))
            }
        }

        fun deleteFavorite(city: City) {

            RoomManager.instance(ApplicationContextProvider.context).favoriteDao().apply {
                val (id,name ) = city
                delete(FavoriteCity(id, name))
            }
        }

    }

    /**
     * Método responsável por atualizar os itens do recyclerview
     */
    fun updataData(list: List<City>, listBanco: List<Int>?) {
        this.list = list
        this.listCitiesFavorites = listBanco
        notifyDataSetChanged()
    }

}