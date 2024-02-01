package core

import java.io.DataInputStream
import java.io.DataOutputStream

interface IIDentifiable {
    val id: Int
    fun write(stream: DataOutputStream)
}

open class DataSource<T : IIDentifiable>(
    configuration: DBConfiguration, val fileName: String,
    creator: (DataInputStream) -> T,
    loader: (String) -> List<T>
) {
    val data: Map<Int, T> = configuration.loadFile(fileName, creator, loader).associateBy { it.id }

    fun get(index: Int): T {
        return data.getValue(index)
    }

    fun save(configuration: DBConfiguration) {
        configuration.saveFile(data.values, fileName)
    }
}