package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd045ResponseDto;
import kg.itg.tirexchange.dto.Epd051ResponseDto;
import kg.itg.tirexchange.service.TirXmlMapper;

@Singleton
public class Epd051Processor extends AbstractEpdProcessor {

    @Inject
    public Epd051Processor(final TirMessageRepository repo, final TirXmlMapper objectMapper) {
        super("EPD051", repo, objectMapper);
    }

    @Override
    public String process(String xmlPayload) throws JsonProcessingException {
        final Epd051ResponseDto dto = readPayload(xmlPayload, Epd051ResponseDto.class);
        validate(dto);

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                null,
                dto.getStatus());

        final Epd045ResponseDto responseDto = new Epd045ResponseDto(STATUS_REJECTED_FOR_TRANSIT,
                dto.getGuaranteeNumber());

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                null,
                STATUS_REJECTED_FOR_TRANSIT);

        return objectMapper.writeValueAsString(responseDto);
    }

    private void validate(final Epd051ResponseDto dto) {
        requireText(dto.getGuaranteeNumber(), "GuaranteeNumber");
    }
}
