package entities

import core.DBException
import core.IIDentifiable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.math.roundToLong

data class FinanceRecord(override val id: Int, var operations: List<FinanceOperation>) : IIDentifiable {
    var totals = mapOf<Int, Long>()

    override fun write(stream: DataOutputStream) {
        TODO("Not yet implemented")
    }

    fun updateChanges(changes: FinanceChanges, accounts: Accounts, subcategories: Subcategories) {
        operations.forEach { it.updateChanges(changes, accounts, subcategories) }
    }

    fun buildChanges(accounts: Accounts, subcategories: Subcategories): FinanceChanges {
        val changes = FinanceChanges(totals)
        operations.forEach {  it.updateChanges(changes, accounts, subcategories) }
        return changes
    }
}

@Serializable
data class FinanceOperation2(
    val id: Int,
    val amount: Double?,
    val summa: Double,
    @SerialName("subcategoryId") val subcategory: Int,
    @SerialName("accountId") val account: Int,
    @SerialName("finOpProperies") val properties: List<FinOpProperty2>?
) {
    fun toFinanceOperation(): FinanceOperation {
        return FinanceOperation(id,
            if (amount == null) null else (amount * 1000).roundToLong(),
            (summa * 100).roundToLong(),
            subcategory,
            account,
            if (properties == null) null else properties.map { it.toFinOpProperty() }.toList()
        )
    }
}

@Serializable
data class FinOpProperty2(
    val numericValue: Long?, val stringValue: String?,
    @Serializable(with = DBDateSerializer::class) val dateValue: Int?, @SerialName("propertyCode") val code: String
) {
    fun toFinOpProperty(): FinOpProperty {
        return FinOpProperty(numericValue, stringValue, dateValue, code)
    }
}


@Serializable
data class FinanceOperation(
    @SerialName("Id") override val id: Int,
    @SerialName("Amount") val amount: Long?,
    @SerialName("Summa") val summa: Long,
    @SerialName("SubcategoryId") val subcategory: Int,
    @SerialName("AccountId") val account: Int,
    @SerialName("FinOpProperies") val properties: List<FinOpProperty>?
) : IIDentifiable {
    override fun write(stream: DataOutputStream) {
        TODO("Not yet implemented")
    }

    fun updateChanges(changes: FinanceChanges, accounts: Accounts, subcategories: Subcategories) {
        val subcategory = subcategories.get(subcategory);
        when (subcategory.operationCode) {
            "INCM" -> changes.income(account, summa)
            "EXPN" -> changes.expenditure(account, summa)
            "SPCL" -> {
                if (subcategory.code == null) {
                    throw DBException("missing subcategory code")
                }
                when (subcategory.code) {
                    // Пополнение карточного счета наличными
                    "INCC" -> handleIncc(changes, accounts)
                    // Снятие наличных в банкомате
                    "EXPC" -> handleExpc(changes, accounts)
                    // Обмен валюты
                    "EXCH" -> handleExch(changes)
                    // Перевод средств между платежными картами
                    "TRFR" -> handleTrfr(changes)
                    else -> throw DBException("unknown subcategory code")
                }
            }

            else -> throw DBException("unknown subcategory operation code")
        }
    }

    private fun handleTrfr(changes: FinanceChanges) {
        handleTrfrWithSumma(changes, summa)
    }

    private fun handleExch(changes: FinanceChanges) {
        if (amount != null) {
            handleTrfrWithSumma(changes, amount / 10)
        }
    }

    private fun handleTrfrWithSumma(changes: FinanceChanges, value: Long)
    {
        if (properties == null) return
        changes.expenditure(account, value);
        val secondAccountProperty = properties.find { it.code == "SECA" }
        if (secondAccountProperty?.numericValue != null)
        {
            changes.income(secondAccountProperty.numericValue.toInt(), summa);
        }
    }

    private fun handleExpc(changes: FinanceChanges, accounts: Accounts) {
        changes.expenditure(account, summa);
        // cash account for corresponding currency code
        val cashAccount = accounts.getCashAccount(account);
        changes.income(cashAccount, summa);
    }

    private fun handleIncc(changes: FinanceChanges, accounts: Accounts) {
        changes.income(account, summa);
        // cash account for corresponding currency code
        val cashAccount = accounts.getCashAccount(account);
        changes.expenditure(cashAccount, summa);
    }
}

fun createFinanceRecord(stream: DataInputStream): FinanceRecord {
    TODO()
}

@Serializable
data class FinOpProperty(
    @SerialName("NumericValue") val numericValue: Long?, @SerialName("StringValue") val stringValue: String?,
    @Serializable(with = DBDateSerializer::class) @SerialName("DateValue") val dateValue: Int?,
    @SerialName("PropertyCode") val code: String
)

class FinanceChanges(totals: Map<Int, Long>) {
    val changes: MutableMap<Int, FinanceChange> =
        totals.map { it.key to FinanceChange(it.value, 0, 0) }.toMap().toMutableMap()

    fun buildTotals(): Map<Int, Long> {
        return changes.map { it.key to it.value.getEndSumma() }.toMap()
    }

    fun income(account: Int, value: Long) {
        changes.getOrPut(account) { FinanceChange(0, 0, 0) }.income += value
    }

    fun expenditure(account: Int, value: Long) {
        changes.getOrPut(account) { FinanceChange(0, 0, 0) }.expenditure += value
    }
}

data class FinanceChange(val summa: Long, var income: Long, var expenditure: Long) {
    fun getEndSumma(): Long {
        return summa + income - expenditure
    }
}