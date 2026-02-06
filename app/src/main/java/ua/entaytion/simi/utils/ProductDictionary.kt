package ua.entaytion.simi.utils

object ProductDictionary {
    private val keywords = mapOf(
        // FRESH
        "молоко" to ProductMatrix.FRESH,
        "кефір" to ProductMatrix.FRESH,
        "сир" to ProductMatrix.FRESH,
        "йогурт" to ProductMatrix.FRESH,
        "сметана" to ProductMatrix.FRESH,
        "ковбаса" to ProductMatrix.FRESH,
        "сосиски" to ProductMatrix.FRESH,
        "риба" to ProductMatrix.FRESH,
        "м'ясо" to ProductMatrix.FRESH,
        "салат" to ProductMatrix.FRESH,
        "торт" to ProductMatrix.FRESH,
        "тістечко" to ProductMatrix.FRESH,
        "яця" to ProductMatrix.FRESH,
        "яйце" to ProductMatrix.FRESH,
        "дріжджі" to ProductMatrix.FRESH,
        "гриби" to ProductMatrix.FRESH,
        
        // NON FRESH - SHORT (<2 mo)
        // usually bread, simple bakery
        "хліб" to ProductMatrix.NON_FRESH_SHORT,
        "булка" to ProductMatrix.NON_FRESH_SHORT,
        "лаваш" to ProductMatrix.NON_FRESH_SHORT,
        
        // NON FRESH - MEDIUM (2-6 mo)
        "чіпси" to ProductMatrix.NON_FRESH_MEDIUM, // Some chips are short, some long, assuming medium as safer default if unsure
        "сухарики" to ProductMatrix.NON_FRESH_MEDIUM,
        "горішки" to ProductMatrix.NON_FRESH_MEDIUM,
        "пиво" to ProductMatrix.NON_FRESH_MEDIUM, // Often 6mo
        
        // NON FRESH - LONG (>6 mo)
        "шоколад" to ProductMatrix.NON_FRESH_LONG,
        "цукерки" to ProductMatrix.NON_FRESH_LONG,
        "кіндер" to ProductMatrix.NON_FRESH_LONG,
        "kinder" to ProductMatrix.NON_FRESH_LONG,
        "снікерс" to ProductMatrix.NON_FRESH_LONG,
        "snickers" to ProductMatrix.NON_FRESH_LONG,
        "hell" to ProductMatrix.NON_FRESH_LONG,
        "енергетик" to ProductMatrix.NON_FRESH_LONG,
        "вода" to ProductMatrix.NON_FRESH_LONG,
        "сік" to ProductMatrix.NON_FRESH_LONG,
        "кава" to ProductMatrix.NON_FRESH_LONG,
        "чай" to ProductMatrix.NON_FRESH_LONG,
        "консерви" to ProductMatrix.NON_FRESH_LONG,
        "олія" to ProductMatrix.NON_FRESH_LONG,
        "макарони" to ProductMatrix.NON_FRESH_LONG,
        "крупа" to ProductMatrix.NON_FRESH_LONG,
        "revo" to ProductMatrix.NON_FRESH_LONG,
        "рево" to ProductMatrix.NON_FRESH_LONG,
        "алко" to ProductMatrix.NON_FRESH_LONG,
        "вино" to ProductMatrix.NON_FRESH_LONG,
        "горілка" to ProductMatrix.NON_FRESH_LONG,
        "shake" to ProductMatrix.NON_FRESH_LONG,
        "шейк" to ProductMatrix.NON_FRESH_LONG,
        
        // PROHIBITED (Excise Goods - Strong Alcohol, Tobacco)
        "горілка" to ProductMatrix.PROHIBITED,
        "водка" to ProductMatrix.PROHIBITED,
        "коньяк" to ProductMatrix.PROHIBITED,
        "віскі" to ProductMatrix.PROHIBITED,
        "джин" to ProductMatrix.PROHIBITED,
        "ром" to ProductMatrix.PROHIBITED,
        "текіла" to ProductMatrix.PROHIBITED,
        "сигарети" to ProductMatrix.PROHIBITED,
        "цигарки" to ProductMatrix.PROHIBITED,
        "тютюн" to ProductMatrix.PROHIBITED,
        "снюс" to ProductMatrix.PROHIBITED,
        "snus" to ProductMatrix.PROHIBITED,
        "айкос" to ProductMatrix.PROHIBITED,
        "iqos" to ProductMatrix.PROHIBITED,
        "glo" to ProductMatrix.PROHIBITED,
        "pod" to ProductMatrix.PROHIBITED
    )
    
    fun guessCategory(name: String): ProductMatrix? {
        val lowerName = name.trim().lowercase()
        // Check for exact containment of keywords
        // Sort by length detailed logic could be better, but loop is fine for now
        for ((key, matrix) in keywords) {
            if (lowerName.contains(key)) {
                return matrix
            }
        }
        return null
    }
}
