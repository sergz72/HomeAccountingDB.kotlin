package entities

import core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class Accounts(source: DataSource<List<Account>>, dataFolderPath: String):
    MapData<Account>(source, "$dataFolderPath/accounts") {
    init {
        val cashAccounts = data.filter { it.value.cashAccount == null }.map { it.value.currency to it.key }.toMap()
        data.values.filter { it.cashAccount == 0 }.forEach { it.cashAccount = cashAccounts.getValue(it.currency) }
    }
}

@Serializable
data class Account(
    override val id: Int,
    val name: String,
    @SerialName("valutaCode") val currency: String,
    @Serializable(with = DBDateSerializer::class) val activeTo: Int?,
    @Serializable(with = IsCashSerializer::class) @SerialName("isCash") var cashAccount: Int?
) : IIDentifiable

object IsCashSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IsCash", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        TODO()
    }

    override fun deserialize(decoder: Decoder): Int? {
        val isCash = decoder.decodeBoolean()
        return if (isCash) {null} else{0}
    }
}