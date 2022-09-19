package com.ndmc.ndmc_record.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.ndmc.ndmc_record.model.BirthModel;

import javax.swing.text.NumberFormatter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class JsonUtil {
    public static String getJsonString(Object object) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)).addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
        //ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        mapper.registerModule(new SimpleModule().addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE)).addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
        //mapper.registerModule(new SimpleModule().addSerializer(new NumberSerializer(NumberFormatter.class.toString()).addDeserializer(NumberSerializer.class, new NumberDeserializers.NumberDeserializer(NumberFormatter.class)));

        return mapper.writeValueAsString(object);
    }

    public static <T> T getObjectFromJson(String json, Class<T> classT) throws Exception {
         ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SimpleModule().addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE)).addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));
        objectMapper.registerModule(new SimpleModule().addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE)).addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE)));

         json = json.replace(":\"\"", ": null");
         json = json.replace(": \"\"", ": null");
         return objectMapper.readValue(json, classT);

    }
}
