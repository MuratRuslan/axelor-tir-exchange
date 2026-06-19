package kg.itg.tirexchange.service.processor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EpdFactory {
    private final Epd015Processor epd015Processor;
    private final Epd016Processor epd016Processor;
    private final Epd028Processor epd028Processor;
    private final Epd029Processor epd029Processor;
    private final Epd045Processor epd045Processor;
    private final Epd051Processor epd051Processor;

    @Inject
    public EpdFactory(final Epd015Processor epd015Processor,
                      final Epd016Processor epd016Processor,
                      final Epd028Processor epd028Processor,
                      final Epd029Processor epd029Processor,
                      final Epd045Processor epd045Processor,
                      final Epd051Processor epd051Processor) {
        this.epd015Processor = epd015Processor;
        this.epd016Processor = epd016Processor;
        this.epd028Processor = epd028Processor;
        this.epd029Processor = epd029Processor;
        this.epd045Processor = epd045Processor;
        this.epd051Processor = epd051Processor;
    }

    public TirMessageProcessor getEpdProcessor(final String rootTag) {
        if (rootTag.equalsIgnoreCase("EPD015")) {
            return epd015Processor;
        }
        if (rootTag.equalsIgnoreCase("EPD016")) {
            return epd016Processor;
        }
        if (rootTag.equalsIgnoreCase("EPD028")) {
            return epd028Processor;
        }
        if (rootTag.equalsIgnoreCase("EPD029")) {
            return epd029Processor;
        }
        if (rootTag.equalsIgnoreCase("EPD045")) {
            return epd045Processor;
        }
        if (rootTag.equalsIgnoreCase("EPD051")) {
            return epd051Processor;
        }
        return null;
    }
}
