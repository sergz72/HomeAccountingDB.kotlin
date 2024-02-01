import core.DBConfiguration
import core.TimeSeriesData
import entities.*
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

class DB(useJson: Boolean, dataFolderPath: String) :
    TimeSeriesData<FinanceRecord>(Configuration(useJson, dataFolderPath), "dates") {
    class Configuration(useJson: Boolean, dataFolderPath: String) : DBConfiguration(useJson, dataFolderPath) {
        override fun decrypt(data: ByteArray): ByteArray {
            TODO("Not yet implemented")
        }

        override fun encrypt(data: ByteArray): ByteArray {
            TODO("Not yet implemented")
        }
    }

    private val accounts: Accounts = Accounts(configuration, "accounts")
    private val subcategories: Subcategories = Subcategories(configuration, "subcategories")

    override fun loadFile(fileInfo: DBConfiguration.DbFileInfo): Map<Int, FinanceRecord> {
        if (configuration.useJson)
        {
            val key = fileInfo.folder.toInt()
            val contents = Files.readString(Path.of(fileInfo.fileName))
            try {
                val list = Json.decodeFromString<List<FinanceOperation>>(contents)
                return mapOf(key to FinanceRecord(key, list))
            } catch (e: Exception) {
                val list = Json.decodeFromString<List<FinanceOperation2>>(contents)
                return mapOf(key to FinanceRecord(key, list.map { it.toFinanceOperation() }.toList()))
            }
        }
        else
        {
            val list = configuration.loadBinaryFile(Path.of(fileInfo.fileName)) { createFinanceRecord(it) }
            return list.associateBy { it.id }
        }
    }

    fun calculateTotals(from: Int)
    {
        var changes: FinanceChanges? = null
        for (kv in data.filter { it.key >= from }) {
            if (changes == null)
                changes = FinanceChanges(kv.value.totals)
            else
                kv.value.totals = changes.buildTotals()
            kv.value.updateChanges(changes, accounts, subcategories);
        }
    }

    fun printChanges(date: Int) {
        val index = if (date == 0) data.lastKey() else date
        val record = data.getValue(index)
        val changes = record.buildChanges(accounts, subcategories);
        for (change in changes.changes)
        {
            val accountName = accounts.get(change.key).name
            println("$accountName ${change.value.summa} ${change.value.income} ${change.value.expenditure} ${change.value.getEndSumma()}")
        }
    }
}