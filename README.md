Start postgres with wal2json by running
```docker-compose up -d```

Run main in ```Main.kt``` to run a very simple test of and outbox implementation using wal2json

Once started any changes to the outbox table will be picked up after 5 seconds and logged.

Insert is done by this query, which ensures that the previous message and message schema is kept, 
so that differences in messages can be picked up by the receiver.
```
INSERT INTO outbox AS oref (aggregateid, model, message_schema, message, previous_message_schema,previous_message,refresh)
VALUES ('e17f0b65-f783-44c9-9b3e-2fc9aba047f4','test','schema-1','message1',null,null,true)
ON CONFLICT (aggregateid)
    DO UPDATE SET aggregateid = EXCLUDED.aggregateid, model = EXCLUDED.model, message_schema = EXCLUDED.message_schema, message = EXCLUDED.message, previous_message_schema = oref.message_schema, previous_message = oref.message;
```

An easy way to touch all rows to trigger a WAL entry and therefore a send, is to do.
```
update outbox set refresh = true where refresh=true;
```

