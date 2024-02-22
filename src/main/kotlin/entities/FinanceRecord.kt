package entities

import core.DBException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToLong

data class FinanceRecord(var operations: List<FinanceOperation>) {
    var totals = mapOf<Int, Long>()

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
    @SerialName("id") val date: Int,
    val amount: Double?,
    val summa: Double,
    @SerialName("subcategoryId") val subcategory: Int,
    @SerialName("accountId") val account: Int,
    @SerialName("finOpProperies") val properties: List<FinOpProperty2>?
) {
    fun toFinanceOperation(): FinanceOperation {
        return FinanceOperation(0,
            if (amount == null) null else (amount * 1000).roundToLong(),
            (summa * 100).roundToLong(),
            subcategory,
            account,
            properties?.map { it.toFinOpProperty() }?.toList()
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
    @SerialName("Id") var date: Int,
    @SerialName("Amount") val amount: Long?,
    @SerialName("Summa") val summa: Long,
    @SerialName("SubcategoryId") val subcategory: Int,
    @SerialName("AccountId") val account: Int,
    @SerialName("FinOpProperies") val properties: List<FinOpProperty>?
) {
    fun updateChanges(changes: FinanceChanges, accounts: Accounts, subcategories: Subcategories) {
        val subcategory = subcategories.get(subcategory)
        when (subcategory.operationCode) {
            SubcategoryOperationCode.Incm -> changes.income(account, summa)
            SubcategoryOperationCode.Expn -> changes.expenditure(account, summa)
            SubcategoryOperationCode.Spcl -> {
                when (subcategory.code) {
                    // Пополнение карточного счета наличными
                    SubcategoryCode.Incc -> handleIncc(changes, accounts)
                    // Снятие наличных в банкомате
                    SubcategoryCode.Expc -> handleExpc(changes, accounts)
                    // Обмен валюты
                    SubcategoryCode.Exch -> handleExch(changes)
                    // Перевод средств между платежными картами
                    SubcategoryCode.Trfr -> handleTrfr(changes)
                    else -> throw DBException("unknown subcategory code")
                }
            }
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
        changes.expenditure(account, value)
        val secondAccountProperty = properties.find { it.code == "SECA" }
        if (secondAccountProperty?.numericValue != null)
        {
            changes.income(secondAccountProperty.numericValue.toInt(), summa)
        }
    }

    private fun handleExpc(changes: FinanceChanges, accounts: Accounts) {
        changes.expenditure(account, summa)
        // cash account for corresponding currency code
        val cashAccount = accounts.get(account).cashAccount
        if (cashAccount != null) {
            changes.income(cashAccount, summa)
        }
    }

    private fun handleIncc(changes: FinanceChanges, accounts: Accounts) {
        changes.income(account, summa)
        // cash account for corresponding currency code
        val cashAccount = accounts.get(account).cashAccount
        if (cashAccount != null) {
            changes.expenditure(cashAccount, summa)
        }
    }
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