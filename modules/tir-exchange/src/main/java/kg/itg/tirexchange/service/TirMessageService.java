package kg.itg.tirexchange.service;

import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.dto.PageResponse;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Optional;

public interface TirMessageService {

    String process(String xml) throws IOException, ParserConfigurationException, SAXException;

    PageResponse<TirMessage> findAll(int page, int size);

    PageResponse<TirMessage> findByMessageType(String messageType, int page, int size);

    PageResponse<TirMessage> findByGuaranteeNumber(String guaranteeNumber, int page, int size);

    Optional<TirMessage> findById(Long id);
}
