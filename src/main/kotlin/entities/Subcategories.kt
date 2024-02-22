package entities

import core.DataSource
import core.IIDentifiable
import core.MapData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class Subcategories(source: DataSource<List<Subcategory>>, dataFolderPath: String):
    MapData<Subcategory>(source, "$dataFolderPath/subcategories")

enum class SubcategoryCode {
    Comb,
    Comc,
    Fuel,
    Prcn,
    Incc,
    Expc,
    Exch,
    Trfr,
    None
}

enum class SubcategoryOperationCode {
    Incm,
    Expn,
    Spcl
}

@Serializable
data class Subcategory(override val id: Int, val name: String,
                       @Serializable(with = CodeSerializer::class) val code: SubcategoryCode,
                       @Serializable(with = OperationCodeSerializer::class)
                       @SerialName("operationCodeId")
                       val operationCode: SubcategoryOperationCode,
                       @SerialName("categoryId") val category: Int): IIDentifiable

object CodeSerializer : KSerializer<SubcategoryCode> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SubcategoryCode", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SubcategoryCode) {
        TODO()
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): SubcategoryCode {
        if (decoder.decodeNotNullMark()) {
            return when (val v = decoder.decodeString()) {
                "COMB" -> SubcategoryCode.Comb
                "COMC" -> SubcategoryCode.Comc
                "INCC" -> SubcategoryCode.Incc
                "EXPC" -> SubcategoryCode.Expc
                "EXCH" -> SubcategoryCode.Exch
                "TRFR" -> SubcategoryCode.Trfr
                "PRCN" -> SubcategoryCode.Prcn
                "FUEL" -> SubcategoryCode.Fuel
                else -> throw IllegalArgumentException("unknown subcategory code $v")
            }
        } else {
            decoder.decodeNull()
            return SubcategoryCode.None
        }
    }
}

object OperationCodeSerializer : KSerializer<SubcategoryOperationCode> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SubcategoryOperationCode", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SubcategoryOperationCode) {
        TODO()
    }

    override fun deserialize(decoder: Decoder): SubcategoryOperationCode {
        return when (val v = decoder.decodeString()) {
            "INCM" -> SubcategoryOperationCode.Incm
            "EXPN" -> SubcategoryOperationCode.Expn
            "SPCL" -> SubcategoryOperationCode.Spcl
            else -> throw IllegalArgumentException("unknown subcategory operation code $v")
        }
    }
}
