package com.example.bugsgame.data.gold

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.ByteArrayInputStream
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
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.cbr.ru/")
                    .header("Accept", "*/*")
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
            // Если есть кэш и он свежий (меньше часа), отдаем его
            if (!forceRefresh && cachedPrice != null &&
                System.currentTimeMillis() - lastUpdate < 3_600_000L
            ) {
                Log.d("GOLD_DEBUG", "Берем цену из кэша: $cachedPrice")
                return@withContext cachedPrice
            }
            try {
                // Берем сегодня и 3 дня назад (чтобы захватить рабочие дни)
                val today = dateFmt.format(Date())
                val threeDaysAgo = dateFmt.format(Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L))

                Log.d("GOLD_DEBUG", "Отправляем запрос к ЦБ с $threeDaysAgo по $today")

                val body = api.getMetals(threeDaysAgo, today)

                Log.d("GOLD_DEBUG", "Ответ от ЦБ получен, читаем XML...")

                val rawXml = body.string()
                Log.d("GOLD_RAW", "Начало XML: ${rawXml.take(1000)}")

                val price = ByteArrayInputStream(rawXml.toByteArray(Charsets.UTF_8)).use { stream ->
                    CbrXmlParser.parseGoldPrice(stream)
                }

                if (price != null) {
                    Log.d("GOLD_DEBUG", "Успешный парсинг: $price")
                    cachedPrice = price
                    lastUpdate = System.currentTimeMillis()
                    price
                } else {
                    Log.w("GOLD_DEBUG", "Парсер вернул null. ЦБ не дал данных за эти дни.")
                    cachedPrice ?: 5000.0 // Если кэша нет, ставим заглушку 5000
                }

            } catch (e: Exception) {
                Log.e("GOLD_DEBUG", "Ошибка загрузки курса ЦБ: ${e.message}", e)
                cachedPrice ?: 5000.0
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