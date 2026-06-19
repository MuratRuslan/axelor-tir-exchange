package kg.itg.tirexchange.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "EPD015")
public class Epd015RequestDto {

    @JacksonXmlProperty(localName = "GuaranteeNumber")
    String guaranteeNumber;

    @JacksonXmlProperty(localName = "IruReference")
    String iruReference;

    @JacksonXmlProperty(localName = "VehicleNumber")
    String vehicleNumber;

    @JacksonXmlProperty(localName = "HolderNumber")
    String HolderNumber;
}
