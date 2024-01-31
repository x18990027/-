package com.cola.shortUrl.domain.dto;

import lombok.Data;

@Data
public class SearchTShortLinkDto {

    private long userId;

    private Long groupId;

    private String shortLink;

    private String longLink;

    private Boolean status;

}
