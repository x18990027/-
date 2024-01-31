package com.cola.shortUrl.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class TShortLinkVo {

    private long id;

    private String shortLink;

    private String longLink;

    private long groupId;

    private String groupName;

    private boolean status;

    private long numLimit;

    private String numLimitLink;

    private String accessPassword;

    private String remark;

    private long visitsNum;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date expiryTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;


}
