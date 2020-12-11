package de.tub.sense.daq.old.json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public abstract class JacksonFactory {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static <T> List<T> deserializeList(String json, Class<T> clazz) throws IOException {
        return (List<T>)MAPPER.readValue(json, (JavaType)MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
        return (T)MAPPER.readValue(json, clazz);
    }

    public static String serialize(Object clazz) throws JsonProcessingException {
        return MAPPER.setVisibility(MAPPER.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .writeValueAsString(clazz);
    }
}
