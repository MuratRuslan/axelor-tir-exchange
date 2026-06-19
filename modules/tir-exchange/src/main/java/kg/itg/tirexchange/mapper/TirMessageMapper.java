package kg.itg.tirexchange.mapper;

import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.dto.Epd029ResponseDto;
import kg.itg.tirexchange.dto.PageResponse;
import kg.itg.tirexchange.dto.TirMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "default")
public interface TirMessageMapper {

    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "toEpochMilli")
    TirMessageDto toDto(TirMessage tirMessage);

    List<TirMessageDto> toDtoList(List<TirMessage> messages);

    default PageResponse<TirMessageDto> toDtoPage(final PageResponse<TirMessage> page) {
        return new PageResponse<>(
                toDtoList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    @Mapping(target = "validationDate", expression = "java(java.time.LocalDateTime.now())")
    Epd029ResponseDto toEpd029Response(TirMessage tirMessage);

    @Named("toEpochMilli")
    default Long toEpochMilli(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }

        return createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
