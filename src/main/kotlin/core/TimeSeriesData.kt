package core

abstract class TimeSeriesData<T: IIDentifiable>(val configuration: DBConfiguration, val suffix: String) {
    val data = configuration.getFileList(suffix)
        .flatMap { loadFile(it).toList() }
        .associate { it.first to it.second }
        .toSortedMap()

    abstract fun loadFile(fileInfo: DBConfiguration.DbFileInfo): Map<Int, T>
}