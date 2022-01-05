import org.jetbrains.exposed.sql.BlobColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.UUIDColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.ResultSet
import java.util.*

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
            |INSERT INTO outbox AS oref (aggregateid, model, message_schema, message, previous_message_schema,previous_message,refresh) VALUES (?,?,?,?,null,null,true) 
            |ON CONFLICT (aggregateid) 
            |DO UPDATE SET aggregateid = EXCLUDED.aggregateid, model = EXCLUDED.model, message_schema = EXCLUDED.message_schema, message = EXCLUDED.message, refresh = EXCLUDED.refresh, previous_message_schema = oref.message_schema, previous_message = oref.message 
            |""".trimMargin(),
        args = listOf(
            UUIDColumnType() to aggregateId,
            VarCharColumnType() to model,
            VarCharColumnType() to schema,
            BlobColumnType() to message
        )
    )
}