
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.ResultSet
import java.util.*

object Outbox : Table() {
    val aggregateId = uuid("aggregateid")
    val model = varchar("model", length = 256)
    val schema = varchar("message_schema", length = 40)
    val message = blob("message")
    val previousSchema = varchar("previous_message_schema", length = 40).nullable()
    val previousMessage = blob("previous_message").nullable()
    override val primaryKey = PrimaryKey(aggregateId, name = "PK_NEW_AGGREGATE_ID") // name is optional here
}

fun <T:Any> String.execAndMap(transform : (ResultSet) -> T) : List<T> {
    val result = arrayListOf<T>()
    TransactionManager.current().exec(this) { rs ->
        while (rs.next()) {
            result += transform(rs)
        }
    }
    return result
}


fun Transaction.addToOutBox(
    aggregateId : UUID,
    model : String,
    schema : String,
    message : ByteArray
) {
    exec(stmt = """
            |INSERT INTO outbox AS oref (aggregateid, model, message_schema, message, previous_message_schema,previous_message) VALUES (?,?,?,?,?,?) 
            |ON CONFLICT (aggregateid) 
            |DO UPDATE SET aggregateid = EXCLUDED.aggregateid, model = EXCLUDED.model, message_schema = EXCLUDED.message_schema, message = EXCLUDED.message, previous_message_schema = oref.message_schema, previous_message = oref.message 
            |""".trimMargin(),
        args = listOf(
            UUIDColumnType() to aggregateId,
            VarCharColumnType() to model,
            VarCharColumnType() to schema,
            BlobColumnType() to message,
            VarCharColumnType() to null,
            BlobColumnType() to null
        )
    )
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

    val dt = UUID.randomUUID()
    transaction(db) {
        addToOutBox(
            dt,
            "test",
            "0.1.1",
            "asdlkfjaødlsfjkaø".toByteArray()
        )
    }

    transaction(db) {
        addToOutBox(
            dt,
            "test",
            "0.1.1",
            "asdasdasd".toByteArray()
        )
    }

    transaction(db) {
        addToOutBox(
            dt,
            "test",
            "0.2.1",
            "asdasdasd".toByteArray()
        )
    }

    transaction(db) {
        "SELECT data FROM pg_logical_slot_get_changes('test_slot', NULL, NULL, 'pretty-print', '1', 'add-msg-prefixes', 'wal2json','format-version','2','add-tables','*.outbox')".execAndMap { rs ->
            val data = rs.getObject("data")
            println(data)
        }
    }


}