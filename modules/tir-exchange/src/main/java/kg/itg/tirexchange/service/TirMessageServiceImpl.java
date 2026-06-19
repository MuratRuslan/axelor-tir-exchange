package kg.itg.tirexchange.service;

import com.google.inject.persist.Transactional;
import javax.inject.Inject;
import javax.inject.Singleton;
import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.db.repo.TirMessageRepository;
import kg.itg.tirexchange.dto.PageResponse;
import kg.itg.tirexchange.service.processor.EpdFactory;
import kg.itg.tirexchange.service.processor.TirMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class TirMessageServiceImpl implements TirMessageService {

    private static final Logger log = LoggerFactory.getLogger(TirMessageServiceImpl.class);

    private final EpdFactory epdFactory;
    private final TirMessageRepository repo;

    @Inject
    public TirMessageServiceImpl(final EpdFactory epdFactory, final TirMessageRepository repo) {
        this.epdFactory = epdFactory;
        this.repo = repo;
    }

    @Override
    @Transactional
    public String process(final String xml) throws IOException, ParserConfigurationException, SAXException {
        log.debug("Incoming TIR message: {}", xml);

        final String epdType = epdType(xml);

        final TirMessageProcessor epdProcessor = epdFactory.getEpdProcessor(epdType);

        if (null == epdProcessor) {
            log.warn("Unsupported EPD type: {}", epdType);
            throw new IllegalArgumentException("Unsupported epd type: " + epdType);
        }

        final String response = epdProcessor.process(xml);
        log.debug("Outgoing TIR message: {}", response);
        return response;
    }

    @Override
    public PageResponse<TirMessage> findAll(final int page, final int size) {
        log.debug("Finding all TIR messages: page={}, size={}", page, size);
        final long total = repo.all().count();
        return PageResponse.of(repo.findAll(page, size), total, page, size);
    }

    @Override
    public PageResponse<TirMessage> findByMessageType(final String messageType, final int page, final int size) {
        log.debug("Finding TIR messages by type: type={}, page={}, size={}", messageType, page, size);
        final long total = repo.countByMessageType(messageType);
        return PageResponse.of(repo.findByMessageType(messageType, page, size), total, page, size);
    }

    @Override
    public PageResponse<TirMessage> findByGuaranteeNumber(final String guaranteeNumber, final int page, final int size) {
        log.debug("Finding TIR messages by guarantee number: guaranteeNumber={}, page={}, size={}",
                guaranteeNumber, page, size);
        final long total = repo.countByGuaranteeNumber(guaranteeNumber);
        return PageResponse.of(repo.findByGuaranteeNumber(guaranteeNumber, page, size), total, page, size);
    }

    @Override
    public Optional<TirMessage> findById(final Long id) {
        log.debug("Finding TIR message by id: {}", id);
        return Optional.ofNullable(repo.find(id));
    }

    private String epdType(final String xml) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes()))
                .getDocumentElement()
                .getTagName();
    }
}
