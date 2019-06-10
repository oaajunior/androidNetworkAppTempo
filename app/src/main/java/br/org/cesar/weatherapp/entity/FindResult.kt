package br.org.cesar.weatherapp.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class FindResult(var list: List<City>)

data class City(
    var id: Int,
    var name: String,
    @SerializedName("weather") var weathers: List<Weather>,
    @SerializedName("sys") var systemCountry: SystemCountry,
    @SerializedName("main") var temperature: Temperature,
    var wind: Wind,
    var clouds: Clouds)


data class Weather(var main: String, var icon: String, var description:String)
data class SystemCountry(var country:String)
data class Temperature(var temp:Double, var pressure:Double)
data class Wind(var speed: Double)
data class Clouds(var all: Int)

@Entity(tableName = "tb_favorite_city")
data class FavoriteCity(
    @PrimaryKey
    var id: Int,
    @ColumnInfo(name = "city_name")
    var name: String)