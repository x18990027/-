package com.cola.shortUrl.controller;

import com.cola.common.annotation.Anonymous;
import com.cola.common.core.controller.BaseController;
import com.cola.shortUrl.domain.LinkAccessStatist;
import com.cola.shortUrl.service.ShortLinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RestController
@RequestMapping("/openUrl")
public class OpenShortLinkController extends BaseController {


    @Resource
    private ShortLinkService shortLinkService;


    /**
     * 解码重定向
     *
     * @param urlKey 原始链接的编码
     * @return 重定向
     */
    @RequestMapping("/{domainKey}/{urlKey}")
    @Anonymous
    public RedirectView redirectToLongLink(@PathVariable("domainKey")  String domainKey , @PathVariable("urlKey") String urlKey, HttpServletRequest request, HttpServletResponse response) {

        LinkAccessStatist linkAccessStatist = new LinkAccessStatist();
        linkAccessStatist.setIp(request.getRemoteAddr());

        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.contains("Mobile")) {
            linkAccessStatist.setUserAgent(request.getHeader("User-Agent"));
            linkAccessStatist.setTerminal(1);
        } else {
            linkAccessStatist.setUserAgent(request.getHeader("User-Agent"));
            linkAccessStatist.setTerminal(2);
        }
        String longLink = shortLinkService.findByShortLink(domainKey,urlKey, linkAccessStatist);
        RedirectView redirectView = new RedirectView(longLink);
//        // 301永久重定向，避免网络劫持
        redirectView.setStatusCode(HttpStatus.FOUND);
        return redirectView;




    }



}
