package core

import java.nio.file.Files
import java.nio.file.Paths

class JsonListSource<T>(private val loader: (String) -> List<T>): DataSource<List<T>>{
    override fun load(fileName: String, addExtension: Boolean): List<T> {
        val path = Paths.get(if (addExtension) {"$fileName.json"} else {fileName})
        val contents = Files.readString(path)
        return loader.invoke(contents)
    }

    override fun save(value: List<T>, fileName: String, addExtension: Boolean) {
        TODO("Not yet implemented")
    }
}