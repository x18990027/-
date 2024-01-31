package com.cola.shortUrl.domain;


import lombok.Data;

import java.util.Date;

@Data
public class TShortLink {

    private long id;

    private String shortLink;

    private String urlKey;

    private String domainKey;

    private String longLink;

    private int status;

    private long numLimit;

    private String numLimitLink;

    private String accessPassword;

    private long userId;

    private long groupId;

    private String remark;

    private long visitsNum;

    private Date expiryTime;

    private Date createTime;

    private Date updateTime;


}
