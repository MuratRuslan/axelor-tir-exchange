package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd029ResponseDto;
import kg.itg.tirexchange.dto.Epd051ResponseDto;
import kg.itg.tirexchange.mapper.EpdDtoMapper;
import kg.itg.tirexchange.service.TirXmlMapper;

@Singleton
public class Epd029Processor extends AbstractEpdProcessor {

    private final EpdDtoMapper epdDtoMapper;

    @Inject
    public Epd029Processor(final TirMessageRepository repo,
                           final TirXmlMapper objectMapper,
                           final EpdDtoMapper epdDtoMapper) {
        super("EPD029", repo, objectMapper);
        this.epdDtoMapper = epdDtoMapper;
    }

    @Override
    public String process(final String xmlPayload) throws JsonProcessingException {
        final Epd029ResponseDto dto = readPayload(xmlPayload, Epd029ResponseDto.class);
        validate(dto);
        final TirMessage tirMessage = findMessage(dto.getGuaranteeNumber());

        if (tirMessage.getStatus().equalsIgnoreCase(STATUS_ACCEPTED)
                && tirMessage.getGuaranteeNumber().startsWith("KG")) {
            final String status = STATUS_ACCEPTED_BY_CUSTOMS;
            saveMessage(tirMessage.getGuaranteeNumber(),
                    tirMessage.getIruReference(),
                    tirMessage.getPayload(),
                    tirMessage.getCustomsIndex(),
                    status);
            dto.setStatus(status);

            return objectMapper.writeValueAsString(dto);
        }

        final String status = STATUS_REJECTED;
        saveMessage(tirMessage.getGuaranteeNumber(),
                tirMessage.getIruReference(),
                tirMessage.getPayload(),
                tirMessage.getCustomsIndex(),
                status);

        final Epd051ResponseDto responseDto = epdDtoMapper.toRejectedEpd051(dto);

        return objectMapper.writeValueAsString(responseDto);
    }

    private void validate(final Epd029ResponseDto dto) {
        requireText(dto.getGuaranteeNumber(), "GuaranteeNumber");
    }
}
