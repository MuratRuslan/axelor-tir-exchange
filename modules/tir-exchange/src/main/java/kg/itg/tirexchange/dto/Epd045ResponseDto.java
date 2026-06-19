package kg.itg.tirexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "EPD045")
public class Epd045ResponseDto {
    @JacksonXmlProperty(localName = "Status")
    private String status;

    @JacksonXmlProperty(localName = "GuaranteeNumber")
    private String guaranteeNumber;
}
