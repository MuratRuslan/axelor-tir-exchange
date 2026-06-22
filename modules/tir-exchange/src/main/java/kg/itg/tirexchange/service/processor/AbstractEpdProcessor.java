package kg.itg.tirexchange.service.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.ValidationException;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.db.repo.TirMessageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractEpdProcessor implements TirMessageProcessor {

    protected static final String STATUS_RECEIVED = "RECEIVED";
    protected static final String STATUS_REJECTED = "REJECTED";
    protected static final String STATUS_REJECTED_FOR_TRANSIT = "REJECTED_FOR_TRANSIT";
    protected static final String STATUS_ACCEPTED = "ACCEPTED";
    protected static final String STATUS_ACCEPTED_BY_CUSTOMS = "ACCEPTED_BY_CUSTOMS";
    protected static final String STATUS_FINISHED = "FINISHED";

    protected final String messageType;
    protected final TirMessageRepository repo;
    protected final ObjectMapper objectMapper;

    protected AbstractEpdProcessor(final String messageType,
                                   final TirMessageRepository repo,
                                   final ObjectMapper objectMapper) {
        this.messageType = messageType;
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    protected <T> T readPayload(final String xmlPayload, final Class<T> payloadType)
            throws JsonProcessingException {

        return objectMapper.readValue(xmlPayload, payloadType);
    }

    /** Starts a fluent required-field check that collects every error and reports them together. */
    protected Required required() {
        return new Required();
    }

    /**
     * Accumulates required-field validation errors so they can be reported all at once,
     * instead of failing on the first missing field.
     */
    protected static final class Required {
        private final List<String> errors = new ArrayList<>();

        public Required text(final String value, final String fieldName) {
            if (value == null || value.isBlank()) {
                errors.add(fieldName + " is required");
            }
            return this;
        }

        /** Throws a {@link ValidationException} with all collected errors (newline-separated), if any. */
        public void validate() {
            if (!errors.isEmpty()) {
                throw new ValidationException(String.join("\n", errors));
            }
        }
    }

    protected TirMessage findMessage(final String guaranteeNumber) {
        return repo.findByGuaranteeNumber(guaranteeNumber)
                .stream()
                .max(Comparator.comparing(TirMessage::getCreatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new ValidationException(
                        "Message not found for guarantee number: " + guaranteeNumber));
    }

    protected void saveMessage(
            final String guaranteeNumber,
            final String iruReference,
            final String payload,
            final String customIndex,
            final String status) {

        final TirMessage entity = new TirMessage();

        entity.setMessageType(messageType);
        entity.setGuaranteeNumber(guaranteeNumber);
        entity.setIruReference(iruReference);
        entity.setPayload(payload);
        entity.setStatus(status);
        entity.setCustomsIndex(customIndex);
        entity.setCreatedAt(LocalDateTime.now());

        repo.save(entity);
    }
}
