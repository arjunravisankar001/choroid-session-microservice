package com.ddbs.choroid_session_service.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PageResponse<T> {

    private List<T> items;
    private int page; // current page number (0-indexed)
    private int size; // number of items per page
    private long totalItems; // total number of items across all pages
    private int totalPages; // total number of pages

}
