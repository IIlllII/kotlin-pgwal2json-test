Start postgres with wal2json by running
```docker-compose up -d```

Run main in ```Main.kt``` to run a very simple test of an outbox implementation using wal2json

An outbox is a table where we store messages we want to send to an external system triggered by some change in our own system.

| aggregateid (PKEY) | schema | message | previous_schema | previous_message | refresh |
| ------------------------- | ------ | ------- | --------------- | ---------------- | ---- |
| "uuid"                    | "0.1.2"| "wat_nu"| "0.1.1"         | "wat_prev"       | true |
| "uuid-2"                  | "0.0.1"| "foo_nu"| null            | null      | true |


Once started any changes to the outbox table will be picked up after 5 seconds and logged.

Insert is done by this query, which ensures that the old message and message_schema is copied to respective columns, 
atomically so that differences in messages can be picked up by the receiver.
```
INSERT INTO outbox AS oref (aggregateid, model, message_schema, message, previous_message_schema,previous_message,refresh)
VALUES ('e17f0b65-f783-44c9-9b3e-2fc9aba047f4','test','schema-1','message1',null,null,true)
ON CONFLICT (aggregateid)
    DO UPDATE SET aggregateid = EXCLUDED.aggregateid, model = EXCLUDED.model, message_schema = EXCLUDED.message_schema, message = EXCLUDED.message, previous_message_schema = oref.message_schema, previous_message = oref.message;
```

An easy way to touch all rows to trigger a WAL entry and therefore send, is to do.
```
update outbox set refresh = true where refresh=true;
```

The current implementation does an at-most-once send, since it does
```
SELECT data FROM pg_logical_slot_get_changes('test_slot', NULL, NULL, 'pretty-print', '1', 'add-msg-prefixes', 'wal2json','include-lsn','true', 'format-version','1','add-tables','*.outbox')
```
Here the replication slot is already advanced, so if using the data fails, it is lost.

For at least once, you can do:
```
SELECT data FROM pg_logical_slot_peek_changes('test_slot', NULL, 100, 'pretty-print', '1', 'add-msg-prefixes', 'wal2json','include-lsn','true'); //Get changes
//Process and send changes
SELECT * from  pg_replication_slot_advance('test_slot','last <lsn> you got'); //Ack data so it is not sent again.
```

### Replication slot
This is what ensures that the WAL is not deleted until it has been replicated.
Created by:
```
SELECT 'init' FROM pg_create_logical_replication_slot('test_slot', 'wal2json')
```