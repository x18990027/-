package com.cola.shortUrl.mapper;

import com.cola.shortUrl.domain.LinkAccessStatist;
import org.apache.ibatis.annotations.Select;

public interface LinkAccessStatisticsMapper {

    @Select("select count(*) from link_access_statistics where short_link_id=#{shortLinkId} )")
    long selectUrlCount(long shortLinkId);

    int insertLog(LinkAccessStatist linkAccessStatist);

}
