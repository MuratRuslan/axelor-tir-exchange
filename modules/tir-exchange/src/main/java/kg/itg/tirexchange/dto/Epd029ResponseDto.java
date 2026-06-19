package kg.itg.tirexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "EPD029")
public class Epd029ResponseDto {

    @JacksonXmlProperty(localName = "CustomsIndex")
    private String customsIndex;

    @JacksonXmlProperty(localName = "Status")
    private String status;

    @JacksonXmlProperty(localName = "GuaranteeNumber")
    private String guaranteeNumber;

    @JacksonXmlProperty(localName = "ValidationDate")
    private LocalDateTime validationDate;
}
