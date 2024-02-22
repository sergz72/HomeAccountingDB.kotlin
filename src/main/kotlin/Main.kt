import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    if (args.size < 3 || args.size > 5) {
        usage()
        return
    }

    when (args[1]) {
        "test_json" -> {
            val start1 = System.currentTimeMillis()
            val db = DB(JsonDBConfiguration(), args[0])
            val end1 = System.currentTimeMillis()
            println("Database has been loaded in ${end1 - start1} ms")
            val us = measureNanoTime { db.calculateTotals(0) } / 1000
            println("Totals calculation finished in $us us")
            db.printChanges(args[2].toInt())
        }
        else -> usage()
    }
}

fun usage() {
    println("Usage: java -jar home_accounting_db.jar data_folder_path\n  test_json date\n  test date aes_key_file")
    println("  migrate source_folder_path aes_key\n  server port rsa_key_file")
}