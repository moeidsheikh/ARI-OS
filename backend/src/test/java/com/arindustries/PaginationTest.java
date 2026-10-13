package com.arindustries;

import com.arindustries.pagination.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaginationTest {

    @Test
    @DisplayName("PageResponse contains exact required fields: data, totalRecords, currentPage, pageSize")
    void testPageResponseStructure() {
        List<String> items = List.of("Item 1", "Item 2");
        PageResponse<String> response = new PageResponse<>(items, 25L, 0, 10);

        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());
        assertEquals(25L, response.getTotalRecords());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isHasPrevious());
        assertTrue(response.isHasNext());
    }

    @Test
    @DisplayName("PageResponse handles empty results properly with non-null empty list")
    void testEmptyPageResponse() {
        PageResponse<Object> response = new PageResponse<>(Collections.emptyList(), 0L, 0, 10);

        assertNotNull(response.getData());
        assertTrue(response.getData().isEmpty());
        assertEquals(0L, response.getTotalRecords());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    @DisplayName("PageResponse pagination navigation flags work across pages")
    void testPageNavigationFlags() {
        // Page 1 of 3 (zero-indexed: 0, 1, 2)
        PageResponse<String> page1 = new PageResponse<>(List.of("A", "B"), 30L, 1, 10);
        assertTrue(page1.isHasPrevious());
        assertTrue(page1.isHasNext());

        // Page 2 of 3 (last page)
        PageResponse<String> page2 = new PageResponse<>(List.of("C"), 21L, 2, 10);
        assertTrue(page2.isHasPrevious());
        assertFalse(page2.isHasNext());
    }
}
