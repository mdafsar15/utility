package com.ndmc.ndmc_record.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class CustomParameterDeserializer extends JsonDeserializer<SlaDetailsModel> {
    @Override
    public SlaDetailsModel deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        System.out.println("CustomParameterDeserializer====");
        if(jsonParser == null)return null;

        SlaDetailsModel slaDetailsModel = new SlaDetailsModel();
        slaDetailsModel.setSlaDetailsId(Long.valueOf(jsonParser.getText()));
        System.out.println("CustomParameterDeserializer===="+slaDetailsModel);
        return slaDetailsModel;
    }
}
