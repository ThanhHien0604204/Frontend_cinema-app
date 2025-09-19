package com.ntth.movie_ticket_booking_app.dto;

import java.util.List;

public class PageResponse<T> {
    public List<T> content;
    public int page;
    public int size;           // page size
    public long totalElements;
    public int totalPages;
    public boolean last;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}