package core

import java.nio.file.Paths

abstract class TimeSeriesData<T>(private val source: DatedSource<T>, private val dataFolderPath: String, private val suffix: String) {
    val data = getFileList()
        .groupBy { calculateKey(source.getDate(it)) }
        .map { it.key to source.load(it.value.map { info -> DBFileWithDate(info.fileName, source.getDate(info)) })}
        .toMap()
        .toSortedMap()

    private fun getFileList(directory: String = ""): Iterable<DBFileInfo> {
        val path = Paths.get(dataFolderPath, suffix, directory).toFile()
        return path.listFiles()?.flatMap { file -> if (file.isDirectory) getFileList(file.name) else listOf(
            DBFileInfo(directory, file.path)
        ) }
            ?: listOf()
    }

    abstract fun calculateKey(date: Int): Int
}