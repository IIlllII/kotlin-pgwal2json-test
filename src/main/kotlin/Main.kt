
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

object Outbox : Table() {
    val aggregateId = uuid("aggregateid")
    val schema = varchar("message_schema", length = 40)
    val message = blob("message")
    val previousSchema = varchar("previous_message_schema", length = 40).nullable()
    val previousMessage = blob("previous_message").nullable()
    val version = bool("refresh")
    override val primaryKey = PrimaryKey(aggregateId, name = "PK_NEW_AGGREGATE_ID") // name is optional here
}


fun main() {

    val db = Database.connect("jdbc:postgresql://localhost:5432/postgres", driver = "org.postgresql.Driver", user = "test", password = "test")

    transaction {
        val count = "select slot_name from pg_replication_slots where slot_name='test_slot' ".execAndMap { rs-> }.count()
        if(count == 0) {
            "SELECT 'init' FROM pg_create_logical_replication_slot('test_slot', 'wal2json')".execAndMap { rs ->
                println(rs.getObject("init"))
            }
        }
    }

    transaction(db) {
        SchemaUtils.create (Outbox)
    }

    addTestData(db)

    while(true) {
        val json = transaction(db) {
            "SELECT data FROM pg_logical_slot_get_changes('test_slot', NULL, NULL, 'pretty-print', '1', 'add-msg-prefixes', 'wal2json','format-version','1','add-tables','*.outbox')".execAndMap { rs ->
                val json = rs.getObject("data")
                json
            }
        }
        if(json.isEmpty()){
            println("No changes")
        }
        else {
            json.forEach {
                println(it)
            }
        }
        Thread.sleep(5000)
    }


}

private fun addTestData(db: Database) {
    val dt = UUID.randomUUID()
    transaction(db) {
        addToOutBox(
            dt,
            "0.1.1",
            "asdlkfjaødlsfjkaø".toByteArray()
        )
    }

    transaction(db) {
        addToOutBox(
            dt,
            "0.1.1",
            "asdasdasd".toByteArray()
        )
    }

    transaction(db) {
        addToOutBox(
            dt,
            "0.2.1",
            "asdasdasd".toByteArray()
        )
    }
}