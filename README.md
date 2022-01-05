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

