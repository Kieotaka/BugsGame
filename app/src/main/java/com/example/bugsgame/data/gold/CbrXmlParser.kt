package com.example.bugsgame.data.gold

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object CbrXmlParser {

    fun parseGoldPrice(input: InputStream): Double? {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, "windows-1251")

        var lastGoldPrice: Double? = null
        var event = parser.eventType
        var currentCode: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    // Запоминаем, какой сейчас обрабатывается металл (1 = Золото)
                    if (parser.name == "Record") {
                        currentCode = parser.getAttributeValue(null, "Code")
                    }
                    // Если мы внутри записи о золоте, ищем цену в любом из этих тегов
                    else if (currentCode == "1") {
                        if (parser.name == "Buy" || parser.name == "Sell" || parser.name == "Price") {
                            val text = parser.nextText().replace(",", ".").trim()
                            val parsedValue = text.toDoubleOrNull()
                            if (parsedValue != null) {
                                lastGoldPrice = parsedValue
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    // Как только запись о металле закончилась, сбрасываем код
                    if (parser.name == "Record") currentCode = null
                }
            }
            event = parser.next()
        }

        if (lastGoldPrice == null) {
            Log.e("GOLD_DEBUG", "Парсер так и не нашел цену. Возможно, ЦБ снова сменил формат.")
        } else {
            Log.d("GOLD_DEBUG", "Успешно найден курс: $lastGoldPrice")
        }
        return lastGoldPrice
    }
}