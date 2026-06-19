package kg.itg.tirexchange.processor;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.ValidationException;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.service.TirXmlMapper;
import kg.itg.tirexchange.service.processor.Epd015Processor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class Epd015ProcessorTest {

    private final Epd015Processor epd015Processor =
            new Epd015Processor(mock(TirMessageRepository.class), new TirXmlMapper());

    @Test
    void process_customs_index_created_ok() throws JsonProcessingException {
        final String requestXml = "<EPD015>\n" +
                "<GuaranteeNumber>KG12345678</GuaranteeNumber>\n" +
                "<IruReference>IRU-2025-001</IruReference>\n" +
                "<VehicleNumber>01KG123ABC</VehicleNumber>\n" +
                "<HolderNumber>TIRH-998877</HolderNumber>\n" +
                "</EPD015>";
        final String result = epd015Processor.process(requestXml);
        System.out.println(result);
    }

    @Test
    void process_validation_rejected_xx() throws JsonProcessingException {
        final String requestXml = "<EPD015>\n" +
                "<GuaranteeNumber>XX12345678</GuaranteeNumber>\n" +
                "<IruReference>IRU-2025-001</IruReference>\n" +
                "<VehicleNumber>01KG123ABC</VehicleNumber>\n" +
                "<HolderNumber>TIRH-998877</HolderNumber>\n" +
                "</EPD015>";
        final String result = epd015Processor.process(requestXml);

        assertEquals("<EPD016><Reason>Invalid guarantee number</Reason><Status>REJECTED</Status>" +
                        "<GuaranteeNumber>XX12345678</GuaranteeNumber></EPD016>",
                result);
    }

    @Test
    void process_validation_rejected_no_holder_number() {
        final String requestXml = "<EPD015>\n" +
                "<GuaranteeNumber>XX12345678</GuaranteeNumber>\n" +
                "<IruReference>IRU-2025-001</IruReference>\n" +
                "<VehicleNumber>01KG123ABC</VehicleNumber>\n" +
                "</EPD015>";

        assertThrows(ValidationException.class, () -> epd015Processor.process(requestXml));
    }

    @Test
    void process_validation_parse_exception() {
        final String requestXml = "<EPD015>\n" +
                "<Point>KG-BISHKEK</Point>\n" +
                "</Route>\n" +
                "</EPD015>";

        assertThrows(JsonParseException.class, () -> epd015Processor.process(requestXml));
    }
}
