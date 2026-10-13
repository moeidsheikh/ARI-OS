package com.arindustries.pagination;

import java.util.List;

public class PageResponse<T> {
    private List<T> data;
    private long totalRecords;
    private int currentPage;
    private int pageSize;

    public PageResponse() {}

    public PageResponse(List<T> data, long totalRecords, int currentPage, int pageSize) {
        this.data = data;
        this.totalRecords = totalRecords;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public static <T> PageResponse<T> of(List<T> data, long totalRecords, int currentPage, int pageSize) {
        return new PageResponse<>(data, totalRecords, currentPage, pageSize);
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) totalRecords / pageSize);
    }

    public boolean isHasNext() {
        return currentPage + 1 < getTotalPages();
    }

    public boolean isHasPrevious() {
        return currentPage > 0;
    }
}
