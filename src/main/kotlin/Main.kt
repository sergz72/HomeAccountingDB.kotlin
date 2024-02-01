fun main(args: Array<String>) {
    if (args.size < 3 || args.size > 5) {
        usage()
        return
    }

    when (args[1]) {
        "test_json" -> {
            val start1 = System.currentTimeMillis()
            val db = DB(true, args[0])
            val end1 = System.currentTimeMillis()
            println("Database has been loaded in ${end1 - start1} ms")
            val start2 = System.currentTimeMillis()
            db.calculateTotals(0)
            val end2 = System.currentTimeMillis()
            println("Totals calculation finished in ${end2 - start2} ms")
            db.printChanges(args[2].toInt());
        }
        else -> usage()
    }
}

fun usage() {
    println("Usage: java -jar home_accounting_db.jar data_folder_path\n  test_json date\n  test date aes_key_file")
    println("  migrate source_folder_path aes_key\n  server port rsa_key_file")
}