package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd015RequestDto;
import kg.itg.tirexchange.dto.Epd016ResponseDto;
import kg.itg.tirexchange.dto.Epd028ResponseDto;
import kg.itg.tirexchange.service.TirXmlMapper;

import java.time.LocalDateTime;
import java.util.Random;

@Singleton
public class Epd015Processor extends AbstractEpdProcessor {

    @Inject
    public Epd015Processor(final TirMessageRepository repo, final TirXmlMapper objectMapper) {
        super("EPD015", repo, objectMapper);
    }

    @Override
    public String process(final String xmlPayload) throws JsonProcessingException {
        final Epd015RequestDto epd015RequestDto = readPayload(xmlPayload, Epd015RequestDto.class);
        validate(epd015RequestDto);

        saveMessage(epd015RequestDto.getGuaranteeNumber(),
                epd015RequestDto.getIruReference(),
                xmlPayload,
                null,
                STATUS_RECEIVED);

        if (epd015RequestDto.getGuaranteeNumber().startsWith("KG")) {
            final String customIndex = generateCustomIndex();
            final String status = STATUS_ACCEPTED;
            final Epd028ResponseDto response =
                    new Epd028ResponseDto(customIndex, status, epd015RequestDto.getGuaranteeNumber());

            saveMessage(epd015RequestDto.getGuaranteeNumber(),
                    epd015RequestDto.getIruReference(),
                    xmlPayload,
                    customIndex,
                    status);
            return objectMapper.writeValueAsString(response);
        }

        if (epd015RequestDto.getGuaranteeNumber().startsWith("XX")) {
            final String status = STATUS_REJECTED;
            final Epd016ResponseDto response =
                    new Epd016ResponseDto("Invalid guarantee number", status, epd015RequestDto.getGuaranteeNumber());

            saveMessage(epd015RequestDto.getGuaranteeNumber(),
                    epd015RequestDto.getIruReference(),
                    xmlPayload,
                    null,
                    status);


            return objectMapper.writeValueAsString(response);
        }

        throw new ValidationException(
                "Unsupported guarantee number");
    }

    private String generateCustomIndex() {
        final Random random = new Random();
        final LocalDateTime now = LocalDateTime.now();
        return "CI-" + now.getDayOfMonth()
                + "" + now.getMonth().getValue()
                + "" + now.getYear()
                + "" + ('A' + random.nextInt(26))
                + ('A' + random.nextInt(26))
                + ('A' + random.nextInt(26))
                + ('A' + random.nextInt(26));
    }

    private void validate(final Epd015RequestDto dto) {
        required()
                .text(dto.getHolderNumber(), "HolderNumber")
                .text(dto.getGuaranteeNumber(), "GuaranteeNumber")
                .validate();
    }
}
