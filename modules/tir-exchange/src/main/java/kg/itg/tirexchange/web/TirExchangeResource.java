package kg.itg.tirexchange.web;

import kg.itg.tirexchange.db.TirMessage;
import kg.itg.tirexchange.dto.PageResponse;
import kg.itg.tirexchange.mapper.TirMessageMapper;
import kg.itg.tirexchange.service.TirMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * JAX-RS resource exposing the same REST surface as the original Spring
 * controllers. AOP auto-discovers any {@code @Path} class and mounts JAX-RS
 * under {@code /ws}, so the endpoints are:
 *
 * <ul>
 *   <li>{@code POST /ws/tir/exchange} (XML in / XML out) — was {@code POST /api/tir/exchange}</li>
 *   <li>{@code GET  /ws/tir/messages} (JSON) — was {@code GET /api/tir/messages}</li>
 *   <li>{@code GET  /ws/tir/messages/{id}}</li>
 *   <li>{@code GET  /ws/tir/messages/type/{type}}</li>
 *   <li>{@code GET  /ws/tir/messages/guarantee/{guaranteeNumber}}</li>
 * </ul>
 *
 * <p>API documentation is provided by a hand-authored OpenAPI spec served by the
 * bundled Swagger UI ({@code /swagger-ui/}); AOP 7.4's OpenAPI scanner does not
 * emit response/request models from annotations, so they are not used here.
 *
 * <p>Note: AOP secures {@code /ws/*} with Shiro, so requests must be
 * authenticated (e.g. HTTP Basic with a valid user). The only base-path change
 * vs. Spring is the {@code /api} → {@code /ws} prefix, which is an AOP convention.
 */
@Path("/tir")
public class TirExchangeResource {

    private static final Logger log = LoggerFactory.getLogger(TirExchangeResource.class);

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    @Inject
    private TirMessageService service;

    @Inject
    private TirMessageMapper tirMessageMapper;

    @POST
    @Path("/exchange")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response exchange(final String xml) {
        try {
            return Response.ok(service.process(xml), MediaType.APPLICATION_XML).build();
        } catch (final Exception ex) {
            log.warn("Request failed: {}", ex.getMessage(), ex);
            final String message = ex instanceof NullPointerException ? "Not found!" : ex.getMessage();
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_XML)
                    .entity(soapFault(message))
                    .build();
        }
    }

    @GET
    @Path("/messages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(@QueryParam("page") @DefaultValue("0") final int page,
                           @QueryParam("size") @DefaultValue("20") final int size) {
        final PageResponse<TirMessage> result = service.findAll(safePage(page), safeSize(size));
        return Response.ok(tirMessageMapper.toDtoPage(result)).build();
    }

    @GET
    @Path("/messages/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("id") final Long id) {
        log.debug("Fetching TIR message by id: {}", id);
        return service.findById(id)
                .map(tirMessageMapper::toDto)
                .map(dto -> Response.ok(dto).build())
                .orElseGet(() -> {
                    log.warn("TIR message not found: id={}", id);
                    return Response.status(Response.Status.NOT_FOUND).build();
                });
    }

    @GET
    @Path("/messages/type/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByMessageType(@PathParam("type") final String type,
                                     @QueryParam("page") @DefaultValue("0") final int page,
                                     @QueryParam("size") @DefaultValue("20") final int size) {
        final PageResponse<TirMessage> result =
                service.findByMessageType(type, safePage(page), safeSize(size));
        return Response.ok(tirMessageMapper.toDtoPage(result)).build();
    }

    @GET
    @Path("/messages/guarantee/{guaranteeNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getByGuaranteeNumber(@PathParam("guaranteeNumber") final String guaranteeNumber,
                                         @QueryParam("page") @DefaultValue("0") final int page,
                                         @QueryParam("size") @DefaultValue("20") final int size) {
        final PageResponse<TirMessage> result =
                service.findByGuaranteeNumber(guaranteeNumber, safePage(page), safeSize(size));
        return Response.ok(tirMessageMapper.toDtoPage(result)).build();
    }

    private static int safePage(final int page) {
        return page < 0 ? DEFAULT_PAGE : page;
    }

    private static int safeSize(final int size) {
        return size <= 0 ? DEFAULT_SIZE : size;
    }

    private static String soapFault(final String message) {
        return "<soap:Fault>\n"
                + "    <faultcode>CLIENT_VALIDATION_ERROR</faultcode>\n"
                + "    <faultstring>" + escapeXml(message) + "</faultstring>\n"
                + "</soap:Fault>\n";
    }

    private static String escapeXml(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
