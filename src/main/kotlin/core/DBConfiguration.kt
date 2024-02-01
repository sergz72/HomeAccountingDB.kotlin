package core

import kotlinx.serialization.json.Json
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class DBConfiguration(val useJson: Boolean, val dataFolderPath: String) {
    data class DbFileInfo(val folder: String, val fileName: String)

    fun getFileList(suffix: String, directory: String = ""): Iterable<DbFileInfo> {
        val path = Paths.get(dataFolderPath, suffix, directory).toFile()
        return path.listFiles()?.flatMap { file -> if (file.isDirectory) getFileList(suffix, file.name) else listOf(DbFileInfo(directory, file.path)) }
            ?: listOf()
    }

    fun <T> loadFile(fileName: String, creator: (DataInputStream) -> T, loader: (String) -> List<T>): List<T>
    {
        if (useJson)
        {
            val path = Paths.get(dataFolderPath, "$fileName.json")
            val contents = Files.readString(path)
            return loader(contents)
        }
        else
        {
            val path = Paths.get(dataFolderPath, "$fileName.bin")
            return loadBinaryFile(path, creator)
        }
    }

    fun <T> loadBinaryFile(path: Path, creator: (DataInputStream) -> T): List<T>
    {
        val bytes = Files.readAllBytes(path)
        val decoded = decrypt(bytes)
        val result = mutableListOf<T>()
        ByteArrayInputStream(decoded).use {
            DataInputStream(it).use { data ->
                var length = data.readInt()
                while (length-- > 0) {
                    val value = creator(data)
                    result.add(value)
                }
            }
        }
        return result
    }

    fun <T: IIDentifiable> saveFile(data: Collection<T>, fileName: String) {
        ByteArrayOutputStream().use {
            DataOutputStream(it).use { stream ->
                stream.writeInt(data.size)
                for (item in data) {
                    item.write(stream)
                }
            }
            val encoded = encrypt(it.toByteArray())
            Files.write(Path.of("$dataFolderPath$fileName.bin"), encoded)
        }
    }

    abstract fun decrypt(data: ByteArray): ByteArray
    abstract fun encrypt(data: ByteArray): ByteArray
}