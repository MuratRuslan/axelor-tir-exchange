package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd028RequestDto;
import kg.itg.tirexchange.mapper.EpdDtoMapper;
import kg.itg.tirexchange.mapper.TirMessageMapper;
import kg.itg.tirexchange.service.TirXmlMapper;

@Singleton
public class Epd028Processor extends AbstractEpdProcessor {
    private final TirMessageMapper tirMessageMapper;
    private final EpdDtoMapper epdDtoMapper;

    @Inject
    public Epd028Processor(final TirMessageRepository repo,
                           final TirXmlMapper objectMapper,
                           final TirMessageMapper tirMessageMapper,
                           final EpdDtoMapper epdDtoMapper) {
        super("EPD028", repo, objectMapper);
        this.tirMessageMapper = tirMessageMapper;
        this.epdDtoMapper = epdDtoMapper;
    }

    @Override
    public String process(final String xmlPayload) throws JsonProcessingException {
        final Epd028RequestDto dto = readPayload(xmlPayload, Epd028RequestDto.class);
        validate(dto);

        saveMessage(dto.getGuaranteeNumber(),
                null,
                xmlPayload,
                dto.getCustomsIndex(),
                dto.getStatus());

        final TirMessage foundMessage = findMessage(dto.getGuaranteeNumber());

        if (foundMessage != null && foundMessage.getStatus().equalsIgnoreCase(STATUS_ACCEPTED)) {
            return objectMapper.writeValueAsString(tirMessageMapper.toEpd029Response(foundMessage));
        }

        return objectMapper.writeValueAsString(
                epdDtoMapper.toEpd016Response(
                        "Not accepted by customs",
                        STATUS_REJECTED,
                        dto.getGuaranteeNumber()));
    }

    private void validate(final Epd028RequestDto dto) {
        required()
                .text(dto.getGuaranteeNumber(), "GuaranteeNumber")
                .text(dto.getStatus(), "Status")
                .text(dto.getCustomsIndex(), "Customs index")
                .validate();
    }
}
