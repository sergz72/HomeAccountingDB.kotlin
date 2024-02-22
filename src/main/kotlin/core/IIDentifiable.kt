package core

interface IIDentifiable {
    val id: Int
}

data class DBFileWithDate(val fileName: String, val date: Int)
data class DBFileInfo(val folder: String, val fileName: String)

interface DatedSource<T> {
    fun load(files: Iterable<DBFileWithDate>): T
    fun save(value: T, dataFolderPath: String, key: Int)
    fun getDate(fileInfo: DBFileInfo): Int
}

interface DataSource<T> {
    fun load(fileName: String, addExtension: Boolean): T
    fun save(value: T, fileName: String, addExtension: Boolean)
}

open class MapData<T: IIDentifiable>(source: DataSource<List<T>>, fileName: String) {
    protected val data: Map<Int, T>
    init {
        val d = source.load(fileName, true)
        data = d.associateBy { it.id }
    }

    fun get(key: Int): T {
        return data.getValue(key)
    }
}