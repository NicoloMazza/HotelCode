package com.hotelcode.database

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClientNetwork {
    val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/webmobile/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserAPI::class.java)

        //.baseUrl("http://192.168.1.8:8000/webmobile/")
        //.baseUrl("http://10.0.2.2:8080/webmobile/")
    }
}