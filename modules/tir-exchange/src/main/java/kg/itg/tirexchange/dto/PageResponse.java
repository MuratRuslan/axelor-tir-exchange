package kg.itg.tirexchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Lightweight replacement for Spring Data's {@code Page}. Serialized to JSON
 * with the same field names the original {@code /api/tir/messages} endpoint
 * exposed ({@code content}, {@code totalElements}, ...), so existing clients
 * keep working against the Axelor port.
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> PageResponse<T> of(final List<T> content,
                                         final long totalElements,
                                         final int page,
                                         final int size) {
        final int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, totalElements, totalPages, page, size);
    }
}
