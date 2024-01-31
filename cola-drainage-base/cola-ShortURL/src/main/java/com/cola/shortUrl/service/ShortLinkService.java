package com.cola.shortUrl.service;


import com.cola.shortUrl.domain.LinkAccessStatist;
import com.cola.shortUrl.domain.dto.SearchTShortLinkDto;
import com.cola.shortUrl.domain.dto.TShortLinkDto;
import com.cola.shortUrl.domain.dto.UpdateTShortLinkDto;
import com.cola.shortUrl.domain.vo.TShortLinkVo;
import java.util.List;



public interface ShortLinkService {



    List<TShortLinkVo> findList(SearchTShortLinkDto searchTShortLinkDto);

    String generateShortLink(TShortLinkDto tShortLinkDto);

    String findByShortLink(String domainKey,String urlKey, LinkAccessStatist linkAccessStatist);

    int delLink(List<Long> idList);

    int updateLink(UpdateTShortLinkDto updateTShortLinkDto);

    int updateStatus(Long id,boolean status,Long userId);


}
