package kg.itg.tirexchange.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javax.inject.Singleton;

/**
 * Jackson {@link XmlMapper} configured for the EPD messages. Replaces the
 * Spring {@code XmlConfig} {@code @Bean ObjectMapper}. As a concrete class with
 * a no-arg constructor, Guice can inject it directly without an explicit
 * binding.
 */
@Singleton
public class TirXmlMapper extends XmlMapper {

    public TirXmlMapper() {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        registerModule(new JavaTimeModule());
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
