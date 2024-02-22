import core.DataSource
import core.DatedSource
import core.TimeSeriesData
import entities.*

interface DBConfiguration
{
    fun getAccountsSource(): DataSource<List<Account>>
    fun getSubcategoriesSource(): DataSource<List<Subcategory>>
    fun getMainDataSource(): DatedSource<FinanceRecord>
}

class DB(configuration: DBConfiguration, dataFolderPath: String) :
    TimeSeriesData<FinanceRecord>(configuration.getMainDataSource(), dataFolderPath, "dates") {
    private val accounts: Accounts = Accounts(configuration.getAccountsSource(), dataFolderPath)
    private val subcategories: Subcategories = Subcategories(configuration.getSubcategoriesSource(), dataFolderPath)

    fun calculateTotals(from: Int)
    {
        var changes: FinanceChanges? = null
        for (kv in data.filter { it.key >= from }) {
            if (changes == null)
                changes = FinanceChanges(kv.value.totals)
            else
                kv.value.totals = changes.buildTotals()
            kv.value.updateChanges(changes, accounts, subcategories)
        }
    }

    fun printChanges(date: Int) {
        val index = if (date == 0) data.lastKey() else date
        val record = data.getValue(index)
        val changes = record.buildChanges(accounts, subcategories)
        for (change in changes.changes)
        {
            val accountName = accounts.get(change.key).name
            println("$accountName ${change.value.summa} ${change.value.income} ${change.value.expenditure} ${change.value.getEndSumma()}")
        }
    }

    override fun calculateKey(date: Int): Int {
        return date / 100
    }
}