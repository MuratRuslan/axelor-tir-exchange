package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd016RequestDto;
import kg.itg.tirexchange.mapper.EpdDtoMapper;
import kg.itg.tirexchange.service.TirXmlMapper;

@Singleton
public class Epd016Processor extends AbstractEpdProcessor {

    private final EpdDtoMapper epdDtoMapper;

    @Inject
    public Epd016Processor(final TirMessageRepository repo,
                           final TirXmlMapper objectMapper,
                           final EpdDtoMapper epdDtoMapper) {
        super("EPD016", repo, objectMapper);
        this.epdDtoMapper = epdDtoMapper;
    }

    @Override
    public String process(final String xmlPayload) throws JsonProcessingException {
        final Epd016RequestDto dto = readPayload(xmlPayload, Epd016RequestDto.class);
        validate(dto);

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                null,
                dto.getStatus());

        return objectMapper.writeValueAsString(epdDtoMapper.toResponse(dto));
    }

    private void validate(final Epd016RequestDto dto) {
        required()
                .text(dto.getGuaranteeNumber(), "GuaranteeNumber")
                .text(dto.getStatus(), "Status")
                .validate();
    }
}
