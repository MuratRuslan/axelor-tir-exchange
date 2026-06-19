package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd045RequestDto;
import kg.itg.tirexchange.mapper.EpdDtoMapper;
import kg.itg.tirexchange.service.TirXmlMapper;

@Singleton
public class Epd045Processor extends AbstractEpdProcessor {

    private final EpdDtoMapper epdDtoMapper;

    @Inject
    public Epd045Processor(final TirMessageRepository repo,
                           final TirXmlMapper objectMapper,
                           final EpdDtoMapper epdDtoMapper) {
        super("EPD045", repo, objectMapper);
        this.epdDtoMapper = epdDtoMapper;
    }

    @Override
    public String process(final String xmlPayload) throws JsonProcessingException {
        final Epd045RequestDto dto = readPayload(xmlPayload, Epd045RequestDto.class);
        validate(dto);

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                null,
                dto.getStatus());

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                null,
                STATUS_FINISHED);

        return objectMapper.writeValueAsString(epdDtoMapper.toResponse(dto));
    }

    private void validate(final Epd045RequestDto dto) {
        requireText(dto.getGuaranteeNumber(), "GuaranteeNumber");
    }
}
