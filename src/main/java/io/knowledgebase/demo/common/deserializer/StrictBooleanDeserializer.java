package io.knowledgebase.demo.common.deserializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class StrictBooleanDeserializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {

        JsonToken jsonToken = jsonParser.getCurrentToken();

        if (jsonToken == JsonToken.VALUE_TRUE || jsonToken == JsonToken.VALUE_FALSE) {
            return jsonParser.getBooleanValue();
        }

        throw new JsonParseException(jsonParser, "Cannot deserialize boolean");
    }
}
