import core.*
import entities.*
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

class JsonDBConfiguration: DBConfiguration {
    override fun getAccountsSource(): DataSource<List<Account>> {
        return JsonListSource { Json.decodeFromString(it) }
    }

    override fun getSubcategoriesSource(): DataSource<List<Subcategory>> {
        return JsonListSource { Json.decodeFromString(it) }
    }

    override fun getMainDataSource(): DatedSource<FinanceRecord> {
        return JsonDatedSource()
    }
}

class JsonDatedSource : DatedSource<FinanceRecord> {
    override fun load(files: Iterable<DBFileWithDate>): FinanceRecord {
        return FinanceRecord(files.flatMap { loadFile(it) })
    }

    private fun loadFile(file: DBFileWithDate): Iterable<FinanceOperation> {
        val contents = Files.readString(Path.of(file.fileName))
        var list: Iterable<FinanceOperation>
        try {
            list = Json.decodeFromString<List<FinanceOperation>>(contents)
        } catch (e: Exception) {
            val l = Json.decodeFromString<List<FinanceOperation2>>(contents)
            list = l.map { it.toFinanceOperation() }
        }
        list.forEach {it.date = file.date}
        return list
    }

    override fun getDate(fileInfo: DBFileInfo): Int {
        return fileInfo.folder.toInt()
    }

    override fun save(value: FinanceRecord, dataFolderPath: String, key: Int) {
        TODO("Not yet implemented")
    }
}
