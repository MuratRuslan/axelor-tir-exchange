package kg.itg.tirexchange.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.inject.Inject;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.service.TirMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web controller backing the TIR message form. Lets a user enter an EPD payload
 * in the form (when adding/editing a record in the grid) and see the processing
 * result without leaving the UI — it runs the same pipeline as
 * {@code POST /ws/tir/exchange}.
 */
public class TirMessageController {

    private static final Logger log = LoggerFactory.getLogger(TirMessageController.class);

    @Inject
    private TirMessageService service;

    /**
     * Builds an EPD payload from the form values (message type as the root tag,
     * the other fields as child elements), routes it through the EPD pipeline,
     * and shows the generated request in {@code payload} and the result in
     * {@code responseXml}. Wired to the "Process message" button.
     */
    public void process(final ActionRequest request, final ActionResponse response) {
        final TirMessage message = request.getContext().asType(TirMessage.class);

        final String messageType = trim(message.getMessageType());
        if (messageType == null) {
            response.setAlert("Select a message type first.");
            return;
        }

        final String payload = buildPayload(message, messageType);
        response.setValue("payload", payload);

        try {
            final String result = service.process(payload);
            response.setValue("responseXml", result);
            response.setNotify("Message processed.");
        } catch (final Exception ex) {
            log.warn("Processing failed: {}", ex.getMessage(), ex);
            final String error = ex instanceof NullPointerException ? "Not found!" : ex.getMessage();
            response.setValue("responseXml", null);
            // Validation errors are newline-separated; render them on separate lines in the dialog.
            response.setError(error == null ? "Processing failed" : error.replace("\n", "<br>"),
                    "Processing failed");
        }
    }

    /** Assembles {@code <MessageType>...child elements...</MessageType>} from non-blank field values. */
    private static String buildPayload(final TirMessage m, final String messageType) {
        final StringBuilder sb = new StringBuilder();
        sb.append('<').append(messageType).append(">\n");
        appendElement(sb, "GuaranteeNumber", m.getGuaranteeNumber());
        appendElement(sb, "HolderNumber", m.getHolderNumber());
        appendElement(sb, "IruReference", m.getIruReference());
        appendElement(sb, "CustomsIndex", m.getCustomsIndex());
        appendElement(sb, "Status", m.getStatus());
        sb.append("</").append(messageType).append('>');
        return sb.toString();
    }

    private static void appendElement(final StringBuilder sb, final String name, final String value) {
        final String trimmed = trim(value);
        if (trimmed != null) {
            sb.append("    <").append(name).append('>')
                    .append(escapeXml(trimmed))
                    .append("</").append(name).append(">\n");
        }
    }

    private static String trim(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String escapeXml(final String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
