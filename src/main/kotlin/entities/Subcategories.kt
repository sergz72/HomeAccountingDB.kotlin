package entities

import core.DBConfiguration
import core.DataSource
import core.IIDentifiable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream

class Subcategories(configuration: DBConfiguration, fileName: String) :
    DataSource<Subcategory>(configuration, fileName, { createSubcategory(it) }, { Json.decodeFromString(it) }) {
}

@Serializable
data class Subcategory(override val id: Int, val name: String, val code: String?,
                       @SerialName("operationCodeId") val operationCode: String,
                       @SerialName("categoryId") val category: Int): IIDentifiable {
    override fun write(stream: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

fun createSubcategory(stream: DataInputStream): Subcategory {
    TODO()
}