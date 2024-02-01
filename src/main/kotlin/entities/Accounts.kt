package entities

import core.DBConfiguration
import core.DataSource
import core.IIDentifiable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream

class Accounts(configuration: DBConfiguration, fileName: String) :
    DataSource<Account>(configuration, fileName, { createAccount(it) }, { Json.decodeFromString(it) }) {
    private val cashAccounts = data.filter { it.value.isCash }.map { it.value.currency to it.key }.toMap()

    fun getCashAccount(account: Int): Int {
        return cashAccounts.getValue(data.getValue(account).currency)
    }
}

@Serializable
data class Account(
    override val id: Int,
    val name: String,
    @SerialName("valutaCode") val currency: String,
    @Serializable(with = DBDateSerializer::class) val activeTo: Int?,
    val isCash: Boolean
) :
    IIDentifiable {
    override fun write(stream: DataOutputStream) {
        TODO("Not yet implemented")
    }
}

fun createAccount(stream: DataInputStream): Account {
    TODO()
}