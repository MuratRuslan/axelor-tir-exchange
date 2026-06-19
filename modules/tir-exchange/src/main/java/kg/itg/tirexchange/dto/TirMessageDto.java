package kg.itg.tirexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Data;

/**
 * JSON representation of a saved {@code TirMessage}, returned by the
 * {@code /ws/tir/messages} endpoints.
 *
 * <p>Unlike the original Spring DTO, the {@code payload} field is a plain
 * string: AOP serializes this endpoint as JSON via RESTEasy/Jackson, so the
 * XML-only {@code RawXmlSerializer} (which wrote raw, unescaped XML) is dropped
 * to keep the JSON well-formed. The {@code @JacksonXmlProperty} hints are kept
 * harmless for callers that prefer the XML representation.
 */
@Data
@Builder
@JacksonXmlRootElement(localName = "TirMessage")
public class TirMessageDto {
    @JacksonXmlProperty(localName = "Id")
    private Long id;
    @JacksonXmlProperty(localName = "MessageType")
    private String messageType;
    @JacksonXmlProperty(localName = "GuaranteeNumber")
    private String guaranteeNumber;
    @JacksonXmlProperty(localName = "IruReference")
    private String iruReference;
    @JacksonXmlProperty(localName = "CustomsIndex")
    private String customsIndex;
    @JacksonXmlProperty(localName = "Status")
    private String status;
    @JacksonXmlProperty(localName = "Payload")
    private String payload;
    @JacksonXmlProperty(localName = "CreatedAt")
    private Long createdAt;
}
