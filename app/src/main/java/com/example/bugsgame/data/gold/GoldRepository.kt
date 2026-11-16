package com.example.bugsgame.data.gold

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GoldRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Android; BugsGame)")
                    .build()
            )
        }
        .build()

    private val api: CbrApi = Retrofit.Builder()
        .baseUrl(CbrApi.BASE_URL)
        .client(client)
        .build()
        .create(CbrApi::class.java)

    @Volatile
    var cachedPrice: Double? = null
        private set

    @Volatile
    var lastUpdate: Long = 0L
        private set

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)

    suspend fun fetchGoldPrice(forceRefresh: Boolean = false): Double? =
        withContext(Dispatchers.IO) {
            if (!forceRefresh && cachedPrice != null &&
                System.currentTimeMillis() - lastUpdate < 3_600_000L
            ) {
                return@withContext cachedPrice
            }
            try {
                val today = dateFmt.format(Date())
                val body = api.getMetals(today, today)
                body.byteStream().use { stream ->
                    val price = CbrXmlParser.parseGoldPrice(stream)
                    if (price != null) {
                        cachedPrice = price
                        lastUpdate = System.currentTimeMillis()
                    }
                    price
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cachedPrice
            }
        }

    companion object {
        @Volatile
        private var INSTANCE: GoldRepository? = null
        fun get(): GoldRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: GoldRepository().also { INSTANCE = it }
        }
    }
}