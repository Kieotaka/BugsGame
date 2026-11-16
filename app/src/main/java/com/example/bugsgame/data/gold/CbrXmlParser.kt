package com.example.bugsgame.data.gold

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

object CbrXmlParser {
    fun parseGoldPrice(input: InputStream): Double? {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, "windows-1251")

        var lastGoldSell: Double? = null
        var event = parser.eventType
        var currentCode: String? = null

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Record" -> currentCode = parser.getAttributeValue(null, "Code")
                        "Sell" -> if (currentCode == "1") {
                            val text = parser.nextText().replace(",", ".").trim()
                            text.toDoubleOrNull()?.let { lastGoldSell = it }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "Record") currentCode = null
                }
            }
            event = parser.next()
        }
        return lastGoldSell
    }
}