INSERT INTO outbox AS oref (aggregateid, message_schema, message, previous_message_schema, previous_message, refresh)
VALUES ('e17f0b65-f783-44c9-9b3e-2fc9aba047f4','schema-1','message1', null, null, true)
ON CONFLICT (aggregateid)
    DO UPDATE SET aggregateid = EXCLUDED.aggregateid, message_schema = EXCLUDED.message_schema, message = EXCLUDED.message, refresh = EXCLUDED.refresh, previous_message_schema = oref.message_schema, previous_message = oref.message;

update outbox set refresh = true where refresh = true;