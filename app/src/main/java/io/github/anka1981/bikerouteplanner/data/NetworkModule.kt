package io.github.anka1981.bikerouteplanner.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/** Erfuellt Nominatims Nutzungsrichtlinie, die einen aussagekraeftigen User-Agent verlangt. */
private class UserAgentInterceptor(private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request().newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(request)
    }
}

object NetworkModule {

    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val bbbikeClient = OkHttpClient.Builder()
        // bbbike kann fuer laengere Strecken bzw. mit der gedrosselten "guest"-appid
        // deutlich mehr als die OkHttp-Standard-10s zum Berechnen einer Route brauchen.
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val nominatimClient = OkHttpClient.Builder()
        .addInterceptor(UserAgentInterceptor("BikeRoutePlanner-AndroidApp/1.0 (privater Gebrauch)"))
        .addInterceptor(loggingInterceptor)
        .build()

    val bbbikeApi: BBBikeApi = Retrofit.Builder()
        .baseUrl(BBBikeApi.BASE_URL)
        .client(bbbikeClient)
        .build()
        .create(BBBikeApi::class.java)

    val bbbikeDeApi: BbbikeDeApi = Retrofit.Builder()
        .baseUrl(BbbikeDeApi.BASE_URL)
        .client(bbbikeClient)
        .build()
        .create(BbbikeDeApi::class.java)

    val nominatimApi: NominatimApi = Retrofit.Builder()
        .baseUrl(NominatimApi.BASE_URL)
        .client(nominatimClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(NominatimApi::class.java)
}
