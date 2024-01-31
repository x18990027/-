package com.cola.shortUrl.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
public class UpdateTShortLinkDto extends TShortLinkDto {

    @NotNull(message = "短链id不能为空！")
    private Long id;

    private Long userId;

    private Date updateTime;

}
