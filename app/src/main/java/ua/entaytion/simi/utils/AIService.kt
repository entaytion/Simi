package ua.entaytion.simi.utils

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AnalyzedProduct(
    val name: String,
    val dates: List<LocalDate>,
    val matrix: ProductMatrix
)

object AIService {
    private const val API_KEY = ua.entaytion.simi.BuildConfig.GEMINI_API_KEY

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = API_KEY,
        generationConfig = generationConfig {
            temperature = 0.4f
            topK = 32
            topP = 1f
            maxOutputTokens = 1024
        }
    )

    suspend fun suggestCategory(productName: String): ProductMatrix? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Ти — асистент продуктового магазину. Класифікуй товар '$productName' в одну з цих категорій:
                    - FRESH (молочка, м'ясо, риба, торти, салати)
                    - NON_FRESH_SHORT (хліб, лаваш, випічка, < 2 місяці термін)
                    - NON_FRESH_MEDIUM (чіпси, сухарики, пиво, 2-6 місяців термін)
                    - NON_FRESH_LONG (шоколад, вода, крупи, консерви, > 6 місяців)
                    - PROHIBITED (міцний алкоголь, тютюн)
                    
                    Відповідай ТІЛЬКИ назвою категорії (наприклад, FRESH).
                """.trimIndent()

                val response = generativeModel.generateContent(prompt)
                val text = response.text?.trim()?.uppercase() ?: return@withContext null

                return@withContext parseMatrix(text)
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    suspend fun analyzeProductFromImage(bitmap: Bitmap): AnalyzedProduct? {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = """
                    Ти — асистент інвентаризації. Проаналізуй фото упаковки товару.
                    Витягни:
                    1. Назва товару (коротко, українською, наприклад: "Молоко Галичина 2.5%").
                    2. Дата спливання терміну (шукай "Вжити до", "Exp", або дату). 
                       Якщо на фото кілька дат або ти не впевнений, випиши до 3-5 найбільш ймовірних варіантів через кому.
                       Формат ТІЛЬКИ DD.MM.YYYY.
                    3. Категорія (FRESH, NON_FRESH_SHORT, NON_FRESH_MEDIUM, NON_FRESH_LONG, PROHIBITED).
                    
                    Формат відповіді:
                    NAME: <назва>
                    DATES: <DD.MM.YYYY, DD.MM.YYYY>
                    CATEGORY: <категорія>
                """.trimIndent()

                val response = generativeModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
                
                val text = response.text ?: return@withContext null
                
                var name = ""
                var datesStr = ""
                var categoryStr = ""
                
                text.lines().forEach { line ->
                    val clean = line.trim()
                    when {
                        clean.startsWith("NAME:") -> name = clean.substringAfter("NAME:").trim()
                        clean.startsWith("DATES:") -> datesStr = clean.substringAfter("DATES:").trim()
                        clean.startsWith("CATEGORY:") -> categoryStr = clean.substringAfter("CATEGORY:").trim()
                    }
                }
                
                if (name.isEmpty()) return@withContext null

                val parsedDates = datesStr.split(",")
                    .map { it.trim() }
                    .mapNotNull {
                        try {
                            LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        } catch (e: Exception) {
                            null
                        }
                    }

                val matrix = parseMatrix(categoryStr) ?: ProductMatrix.FRESH
                
                return@withContext AnalyzedProduct(name, parsedDates, matrix)

            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }
    
    private fun parseMatrix(text: String): ProductMatrix? {
        return when {
            text.contains("FRESH") -> ProductMatrix.FRESH
            text.contains("NON_FRESH_SHORT") -> ProductMatrix.NON_FRESH_SHORT
            text.contains("NON_FRESH_MEDIUM") -> ProductMatrix.NON_FRESH_MEDIUM
            text.contains("NON_FRESH_LONG") -> ProductMatrix.NON_FRESH_LONG
            text.contains("PROHIBITED") -> ProductMatrix.PROHIBITED
            else -> null
        }
    }
}
