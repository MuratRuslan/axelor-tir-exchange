package kg.itg.tirexchange.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import javax.validation.ValidationException;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.Epd016ResponseDto;
import kg.itg.tirexchange.dto.Epd029ResponseDto;
import kg.itg.tirexchange.dto.Epd045ResponseDto;
import kg.itg.tirexchange.dto.Epd051ResponseDto;
import kg.itg.tirexchange.mapper.EpdDtoMapper;
import kg.itg.tirexchange.mapper.EpdDtoMapperImpl;
import kg.itg.tirexchange.mapper.TirMessageMapper;
import kg.itg.tirexchange.mapper.TirMessageMapperImpl;
import kg.itg.tirexchange.service.TirXmlMapper;
import kg.itg.tirexchange.service.processor.Epd016Processor;
import kg.itg.tirexchange.service.processor.Epd028Processor;
import kg.itg.tirexchange.service.processor.Epd029Processor;
import kg.itg.tirexchange.service.processor.Epd045Processor;
import kg.itg.tirexchange.service.processor.Epd051Processor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpdProcessorTest {

    private final TirXmlMapper objectMapper = new TirXmlMapper();
    private final TirMessageMapper tirMessageMapper = new TirMessageMapperImpl();
    private final EpdDtoMapper epdDtoMapper = new EpdDtoMapperImpl();

    @Test
    void epd016_returns_response_and_saves_message() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd016Processor processor = new Epd016Processor(repo, objectMapper, epdDtoMapper);
        final String payload = "<EPD016>\n"
                + "    <Reason>Invalid guarantee number</Reason>\n"
                + "    <Status>REJECTED</Status>\n"
                + "    <GuaranteeNumber>XX12345678</GuaranteeNumber>\n"
                + "</EPD016>";

        final Epd016ResponseDto response = read(processor.process(payload), Epd016ResponseDto.class);

        assertThat(response.getReason()).isEqualTo("Invalid guarantee number");
        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getGuaranteeNumber()).isEqualTo("XX12345678");

        final TirMessage saved = captureSaved(repo);
        assertThat(saved.getMessageType()).isEqualTo("EPD016");
        assertThat(saved.getGuaranteeNumber()).isEqualTo("XX12345678");
        assertThat(saved.getStatus()).isEqualTo("REJECTED");
    }

    @Test
    void epd016_requires_status() {
        final Epd016Processor processor = new Epd016Processor(mock(TirMessageRepository.class), objectMapper, epdDtoMapper);
        final String payload = "<EPD016>\n"
                + "    <GuaranteeNumber>XX12345678</GuaranteeNumber>\n"
                + "</EPD016>";

        final ValidationException exception =
                assertThrows(ValidationException.class, () -> processor.process(payload));

        assertThat(exception).hasMessage("Status is required");
    }

    @Test
    void reports_all_missing_required_fields_at_once() {
        final Epd028Processor processor =
                new Epd028Processor(mock(TirMessageRepository.class), objectMapper, tirMessageMapper, epdDtoMapper);

        final ValidationException exception =
                assertThrows(ValidationException.class, () -> processor.process("<EPD028></EPD028>"));

        assertThat(exception.getMessage())
                .contains("GuaranteeNumber is required")
                .contains("Status is required")
                .contains("Customs index is required");
    }

    @Test
    void epd028_returns_epd029_when_original_message_is_accepted() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd028Processor processor = new Epd028Processor(repo, objectMapper, tirMessageMapper, epdDtoMapper);
        when(repo.findByGuaranteeNumber("KG12345678"))
                .thenReturn(List.of(message("EPD015", "KG12345678", "IRU-1", "CI-123", "ACCEPTED", "<EPD015/>")));

        final String payload = "<EPD028>\n"
                + "    <CustomsIndex>CI-123</CustomsIndex>\n"
                + "    <Status>ACCEPTED</Status>\n"
                + "    <GuaranteeNumber>KG12345678</GuaranteeNumber>\n"
                + "</EPD028>";
        final Epd029ResponseDto response = read(processor.process(payload), Epd029ResponseDto.class);

        assertThat(response.getCustomsIndex()).isEqualTo("CI-123");
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getGuaranteeNumber()).isEqualTo("KG12345678");
        assertThat(response.getValidationDate()).isNotNull();
        verify(repo).save(any(TirMessage.class));
    }

    @Test
    void epd028_returns_rejection_when_original_message_is_not_accepted() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd028Processor processor = new Epd028Processor(repo, objectMapper, tirMessageMapper, epdDtoMapper);
        when(repo.findByGuaranteeNumber("XX12345678"))
                .thenReturn(List.of(message("EPD015", "XX12345678", "IRU-1", null, "REJECTED", "<EPD015/>")));

        final String payload = "<EPD028>\n"
                + "    <CustomsIndex>CI-123</CustomsIndex>\n"
                + "    <Status>REJECTED</Status>\n"
                + "    <GuaranteeNumber>XX12345678</GuaranteeNumber>\n"
                + "</EPD028>";
        final Epd016ResponseDto response = read(processor.process(payload), Epd016ResponseDto.class);

        assertThat(response.getReason()).isEqualTo("Not accepted by customs");
        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getGuaranteeNumber()).isEqualTo("XX12345678");
    }

    @Test
    void epd028_requires_existing_message() {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd028Processor processor = new Epd028Processor(repo, objectMapper, tirMessageMapper, epdDtoMapper);
        when(repo.findByGuaranteeNumber("KG404")).thenReturn(List.of());

        final String payload = "<EPD028>\n"
                + "    <CustomsIndex>CI-404</CustomsIndex>\n"
                + "    <Status>ACCEPTED</Status>\n"
                + "    <GuaranteeNumber>KG404</GuaranteeNumber>\n"
                + "</EPD028>";
        final ValidationException exception =
                assertThrows(ValidationException.class, () -> processor.process(payload));

        assertThat(exception).hasMessage("Message not found for guarantee number: KG404");
    }

    @Test
    void epd029_accepts_kg_message_by_customs() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd029Processor processor = new Epd029Processor(repo, objectMapper, epdDtoMapper);
        when(repo.findByGuaranteeNumber("KG12345678"))
                .thenReturn(List.of(message("EPD015", "KG12345678", "IRU-1", "CI-123", "ACCEPTED", "<EPD015/>")));

        final String payload = "<EPD029>\n"
                + "    <CustomsIndex>CI-123</CustomsIndex>\n"
                + "    <Status>ACCEPTED</Status>\n"
                + "    <GuaranteeNumber>KG12345678</GuaranteeNumber>\n"
                + "</EPD029>";
        final Epd029ResponseDto response = read(processor.process(payload), Epd029ResponseDto.class);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED_BY_CUSTOMS");
        assertThat(response.getGuaranteeNumber()).isEqualTo("KG12345678");

        final TirMessage saved = captureSaved(repo);
        assertThat(saved.getMessageType()).isEqualTo("EPD029");
        assertThat(saved.getStatus()).isEqualTo("ACCEPTED_BY_CUSTOMS");
        assertThat(saved.getIruReference()).isEqualTo("IRU-1");
    }

    @Test
    void epd029_uses_latest_message_for_same_guarantee_number() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd029Processor processor = new Epd029Processor(repo, objectMapper, epdDtoMapper);
        final TirMessage olderMessage = message("EPD015",
                "KG12345678",
                "IRU-OLD",
                "CI-OLD",
                "REJECTED",
                "<EPD015/>",
                LocalDateTime.now().minusDays(1));
        final TirMessage latestMessage = message("EPD015",
                "KG12345678",
                "IRU-LATEST",
                "CI-LATEST",
                "ACCEPTED",
                "<EPD015/>",
                LocalDateTime.now());
        when(repo.findByGuaranteeNumber("KG12345678"))
                .thenReturn(List.of(olderMessage, latestMessage));

        final String payload = "<EPD029>\n"
                + "    <CustomsIndex>CI-LATEST</CustomsIndex>\n"
                + "    <Status>ACCEPTED</Status>\n"
                + "    <GuaranteeNumber>KG12345678</GuaranteeNumber>\n"
                + "</EPD029>";
        final Epd029ResponseDto response = read(processor.process(payload), Epd029ResponseDto.class);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED_BY_CUSTOMS");

        final TirMessage saved = captureSaved(repo);
        assertThat(saved.getCustomsIndex()).isEqualTo("CI-LATEST");
        assertThat(saved.getIruReference()).isEqualTo("IRU-LATEST");
    }

    @Test
    void epd029_rejects_non_kg_message() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd029Processor processor = new Epd029Processor(repo, objectMapper, epdDtoMapper);
        when(repo.findByGuaranteeNumber("TR12345678"))
                .thenReturn(List.of(message("EPD015", "TR12345678", "IRU-2", "CI-999", "ACCEPTED", "<EPD015/>")));

        final String payload = "<EPD029>\n"
                + "    <CustomsIndex>CI-999</CustomsIndex>\n"
                + "    <Status>ACCEPTED</Status>\n"
                + "    <GuaranteeNumber>TR12345678</GuaranteeNumber>\n"
                + "</EPD029>";
        final Epd051ResponseDto response = read(processor.process(payload), Epd051ResponseDto.class);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getReason()).isEqualTo("REJECTED BY CUSTOMS");
        assertThat(response.getGuaranteeNumber()).isEqualTo("TR12345678");
    }

    @Test
    void epd045_finishes_message_and_saves_request_and_final_state() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd045Processor processor = new Epd045Processor(repo, objectMapper, epdDtoMapper);

        final String payload = "<EPD045>\n"
                + "    <Status>ACCEPTED_BY_CUSTOMS</Status>\n"
                + "    <GuaranteeNumber>KG12345678</GuaranteeNumber>\n"
                + "</EPD045>";
        final Epd045ResponseDto response = read(processor.process(payload), Epd045ResponseDto.class);

        assertThat(response.getStatus()).isEqualTo("FINISHED");
        assertThat(response.getGuaranteeNumber()).isEqualTo("KG12345678");

        final List<TirMessage> saved = captureSaved(repo, 2);
        assertThat(saved).extracting(TirMessage::getStatus)
                .containsExactly("ACCEPTED_BY_CUSTOMS", "FINISHED");
    }

    @Test
    void epd051_rejects_for_transit_and_saves_request_and_final_state() throws JsonProcessingException {
        final TirMessageRepository repo = mock(TirMessageRepository.class);
        final Epd051Processor processor = new Epd051Processor(repo, objectMapper);

        final String payload = "<EPD051>\n"
                + "    <Status>REJECTED</Status>\n"
                + "    <Reason>REJECTED BY CUSTOMS</Reason>\n"
                + "    <GuaranteeNumber>XX12345678</GuaranteeNumber>\n"
                + "</EPD051>";
        final Epd045ResponseDto response = read(processor.process(payload), Epd045ResponseDto.class);

        assertThat(response.getStatus()).isEqualTo("REJECTED_FOR_TRANSIT");
        assertThat(response.getGuaranteeNumber()).isEqualTo("XX12345678");

        final List<TirMessage> saved = captureSaved(repo, 2);
        assertThat(saved).extracting(TirMessage::getStatus)
                .containsExactly("REJECTED", "REJECTED_FOR_TRANSIT");
    }

    private <T> T read(final String xml, final Class<T> type) throws JsonProcessingException {
        return objectMapper.readValue(xml, type);
    }

    private TirMessage captureSaved(final TirMessageRepository repo) {
        return captureSaved(repo, 1).get(0);
    }

    private List<TirMessage> captureSaved(final TirMessageRepository repo, final int times) {
        final ArgumentCaptor<TirMessage> captor = ArgumentCaptor.forClass(TirMessage.class);
        verify(repo, times(times)).save(captor.capture());
        return captor.getAllValues();
    }

    private TirMessage message(
            final String messageType,
            final String guaranteeNumber,
            final String iruReference,
            final String customsIndex,
            final String status,
            final String payload) {
        return message(messageType, guaranteeNumber, iruReference, customsIndex, status, payload, null);
    }

    private TirMessage message(
            final String messageType,
            final String guaranteeNumber,
            final String iruReference,
            final String customsIndex,
            final String status,
            final String payload,
            final LocalDateTime createdAt) {

        final TirMessage message = new TirMessage();
        message.setMessageType(messageType);
        message.setGuaranteeNumber(guaranteeNumber);
        message.setIruReference(iruReference);
        message.setCustomsIndex(customsIndex);
        message.setStatus(status);
        message.setPayload(payload);
        message.setCreatedAt(createdAt);
        return message;
    }
}
