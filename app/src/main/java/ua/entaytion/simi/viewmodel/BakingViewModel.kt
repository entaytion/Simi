package ua.entaytion.simi.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import java.time.LocalDateTime

data class BakingProduct(
    val code: String,
    val name: String,
    val shelfLifeHours: Int
)

data class BakingEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val product: BakingProduct,
    val ptp: LocalDateTime,
    val ktp: LocalDateTime
)

class BakingViewModel : ViewModel() {
    private val _entries = mutableStateListOf<BakingEntry>()
    val entries: List<BakingEntry>
        get() = _entries

    val availableProducts = listOf(
        BakingProduct("31380", "Піца \"Сирна\" з соусом н/ф 170г /FishkaFood/", 16),
        BakingProduct("39342", "Піца \"Шинка та гриби\", з соусом 170г", 16),
        BakingProduct("41491", "Піцета з куркою та грибами, 135г", 16),
        BakingProduct("41492", "Піцета з мисливськими ковбаски, 135г", 16),
        BakingProduct("41618", "Донат піца \"Маргарита\" з часниковою начинкою", 16),
        BakingProduct("41619", "Донат піца \"Альфредо\" з часниковою начинкою", 16),
        BakingProduct("20138", "Ролліні з грибами, 85г", 36),
        BakingProduct("20137", "Ролліні з сиром та зеленню, 85г", 36),
        BakingProduct("18179", "Ролліні з бринзою та шпинатом, 85г", 36),
        BakingProduct("18178", "Ролліні з куркою, 85г", 36),
        BakingProduct("26994", "Ролліні з беконом та горохом, 85г", 36),
        BakingProduct("31938", "Патті Філло з сиром та зеленню", 36),
        BakingProduct("38840", "Хачапурі з сиром \"Сулугуні\" н/ф 200г", 48),
        BakingProduct("31782", "Сирна паличка листова, 100г", 36),
        BakingProduct("17811", "Сосиска в листовому тісті, 115г", 36),
        BakingProduct("17808", "Туба з куркою та сметаною, 85г", 36),
        BakingProduct("40162", "Сосиска в тісті з америк. соусом XL, 190г", 36),
        BakingProduct("37008", "Слойка з кленовим сиропом та горіхами пекана", 24),
        BakingProduct("40337", "Слойка житньо-солодова \"Зернова\" 90г", 36),
        BakingProduct("38865", "Круасан з вишневим джемом, 115г", 16),
        BakingProduct("38867", "Круасан з шоколадом, 115г", 16),
        BakingProduct("40016", "Круасан з заварним кремом, 115г", 16),
        BakingProduct("22979", "Багет пшеничний 200г", 24),
        BakingProduct("23450", "Круасан зерновий, 105г", 24),
        BakingProduct("23017", "Плюшка з цукром, 170г", 24),
        BakingProduct("37778", "Штрудель з чорницею 90г (без пакування)", 24),
        BakingProduct("37778", "Штрудель з чорницею 90г (запакований)", 36),
        BakingProduct("37774", "Штрудель з вишнями 90г (без пакування)", 24),
        BakingProduct("37774", "Штрудель з вишнями 90г (запакований)", 36),
        BakingProduct("37791", "Слойка \"Курячий стейк з сиром\" 140г (без пакування)", 16),
        BakingProduct("37791", "Слойка \"Курячий стейк з сиром\" 140г (запакована)", 24),
        BakingProduct("37782", "Сарделька в тісті 130г (без пакування)", 16),
        BakingProduct("37782", "Сарделька в тісті 130г (запакована)", 24),
        BakingProduct("37787", "Трубочка з баварською ковбаскою, сиром 130г (без пакування)", 16),
        BakingProduct("37787", "Трубочка з баварською ковбаскою, сиром 130г (запакована)", 24),
        BakingProduct("37785", "Хачапурі з м'ясом та зеленню 190г (без пакування)", 16),
        BakingProduct("37785", "Хачапурі з м'ясом та зеленню 190г (запаковане)", 24),
        BakingProduct("23159", "Багет Паризький", 24),
        BakingProduct("39974", "Багет \"Французький\" з часником, 175г", 24),
        BakingProduct("32974", "Багет \"Французький\" 280г (неупакований)", 24),
        BakingProduct("32974", "Багет \"Французький\" 280г (запакований)", 48),
        BakingProduct("32973", "Багет \"Французький\" міні 90г (неупакований)", 24),
        BakingProduct("32973", "Багет \"Французький\" міні 90г (запакований)", 48),
        BakingProduct("40727", "Пампух \"Асорті\" з начинкою 65 г", 48),
        BakingProduct("32015", "Булочка Сінабон", 168)
    )

    fun addEntry(product: BakingProduct, ptp: LocalDateTime) {
        val ktp = ptp.plusHours(product.shelfLifeHours.toLong()).minusMinutes(1)
        _entries.add(0, BakingEntry(product = product, ptp = ptp, ktp = ktp))
    }

    fun removeEntry(entry: BakingEntry) {
        _entries.remove(entry)
    }
}
