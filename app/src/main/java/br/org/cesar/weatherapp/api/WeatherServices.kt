package br.org.cesar.weatherapp.api

import br.org.cesar.weatherapp.Constants
import br.org.cesar.weatherapp.entity.FindResult
import br.org.cesar.weatherapp.features.setting.SettingActivity
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface que descreve os serviços, verbos HTTP (GET, POST, PUT, DELETE) e parâmetros (Query, Path) dos end points
 */
interface WeatherServices {

    @GET("find")

    fun find(
        @Query("q")
        cityName: String,

        @Query("lang")
        language: String,

        @Query("units")
        metricaTempo: String,

        @Query("appid")
        appKey: String): Call<FindResult>


    @GET("group")

    fun group(
        @Query("id")
        citiesID: String,

        @Query("lang")
        language: String,

        @Query("units")
        metricaTempo: String,

        @Query("appid")
        appKey: String): Call<FindResult>

}