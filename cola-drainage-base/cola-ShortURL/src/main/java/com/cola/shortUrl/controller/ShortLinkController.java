package com.cola.shortUrl.controller;


import com.cola.common.core.domain.dto.CommonBatchIdDto;
import com.cola.common.enums.UrlKeyEnum;
import com.cola.common.utils.SecurityUtils;
import com.cola.shortUrl.domain.dto.SearchTShortLinkDto;
import com.cola.shortUrl.domain.dto.TShortLinkDto;
import com.cola.shortUrl.domain.dto.UpdateStatusTShortLinkDto;
import com.cola.shortUrl.domain.dto.UpdateTShortLinkDto;
import com.cola.shortUrl.domain.vo.TShortLinkVo;
import com.cola.shortUrl.service.ShortLinkService;
import com.cola.common.core.controller.BaseController;
import com.cola.common.core.page.TableDataInfo;
import com.cola.common.enums.SystemStateCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/shortUrl/url")
public class ShortLinkController extends BaseController {


    @Resource
    private ShortLinkService shortLinkService;

    @PreAuthorize("@ss.hasPermi('shortUrl:url:add')")
    @PostMapping(value = "/add")
    public TableDataInfo linkAdd(@Validated @RequestBody TShortLinkDto tShortLinkDto) {

        String shortLink = shortLinkService.generateShortLink(tShortLinkDto);
        return getDataTable(SystemStateCodeEnum.SUCCESS, shortLink);
    }

    @PreAuthorize("@ss.hasPermi('shortUrl:url:list')")
    @PostMapping(value = "/list")
    public TableDataInfo linkList(@RequestBody SearchTShortLinkDto searchTShortLinkDto) {

        startPage();
        List<TShortLinkVo> list = shortLinkService.findList(searchTShortLinkDto);
        return getDataTable(SystemStateCodeEnum.SUCCESS, list);
    }

    @PreAuthorize("@ss.hasPermi('shortUrl:url:del')")
    @PostMapping(value = "/del")
    public TableDataInfo delLink(@Validated @RequestBody CommonBatchIdDto commonBatchIdDto) {

        shortLinkService.delLink(commonBatchIdDto.getIdList());
        return getDataTable(SystemStateCodeEnum.SUCCESS);
    }

    @PreAuthorize("@ss.hasPermi('shortUrl:url:update')")
    @PostMapping(value = "/update")
    public TableDataInfo updateLink(@Validated @RequestBody UpdateTShortLinkDto updateTShortLinkDto) {

        shortLinkService.updateLink(updateTShortLinkDto);
        return getDataTable(SystemStateCodeEnum.SUCCESS);
    }

    @PreAuthorize("@ss.hasPermi('shortUrl:url:updateStatus')")
    @PostMapping(value = "/updateStatus")
    public TableDataInfo updateStatus(@Validated @RequestBody UpdateStatusTShortLinkDto updateTShortLinkDto) {

        shortLinkService.updateStatus(updateTShortLinkDto.getId(), updateTShortLinkDto.getStatus(), SecurityUtils.getUserId());
        return getDataTable(SystemStateCodeEnum.SUCCESS);
    }



}
