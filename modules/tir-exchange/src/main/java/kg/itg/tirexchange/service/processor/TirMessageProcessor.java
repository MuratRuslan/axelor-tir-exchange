package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface TirMessageProcessor {

    String process(String xmlPayload) throws JsonProcessingException;
}
