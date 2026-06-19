package kg.itg.tirexchange.mapper;

import kg.itg.tirexchange.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface EpdDtoMapper {

    Epd016ResponseDto toResponse(Epd016RequestDto request);

    Epd016ResponseDto toEpd016Response(String reason, String status, String guaranteeNumber);

    @Mapping(target = "status", constant = "FINISHED")
    Epd045ResponseDto toResponse(Epd045RequestDto request);

    @Mapping(target = "status", constant = "REJECTED")
    @Mapping(target = "reason", constant = "REJECTED BY CUSTOMS")
    Epd051ResponseDto toRejectedEpd051(Epd029ResponseDto response);
}
