package com.cola.shortUrl.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.cola.common.enums.SystemStateCodeEnum;
import com.cola.common.enums.UrlKeyEnum;
import com.cola.common.exception.ServiceException;
import com.cola.common.utils.DateUtils;
import com.cola.common.utils.SecurityUtils;
import com.cola.common.utils.bean.BeanUtils;
import com.cola.shortUrl.domain.LinkAccessStatist;
import com.cola.shortUrl.domain.dto.SearchTShortLinkDto;
import com.cola.shortUrl.domain.dto.TShortLinkDto;
import com.cola.shortUrl.domain.dto.UpdateTShortLinkDto;
import com.cola.shortUrl.domain.vo.TShortLinkVo;
import com.cola.shortUrl.mapper.LinkAccessStatisticsMapper;
import com.cola.shortUrl.mapper.LinkGroupMapper;
import com.cola.shortUrl.mapper.ShortLinkMapper;
import com.cola.shortUrl.domain.TShortLink;
import com.cola.shortUrl.util.Base62Utils;
import com.cola.shortUrl.util.SnowFlakeUtils;
import com.cola.shortUrl.service.ShortLinkService;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ShortLinkServiceImpl implements ShortLinkService {


    private final Log log = LogFactory.get();

    private static final Logger logger = LoggerFactory.getLogger(ShortLinkServiceImpl.class);

    @Resource
    private ShortLinkMapper shortLinkDao;

    @Resource
    private LinkGroupMapper groupMapper;

    @Resource
    private LinkAccessStatisticsMapper linkAccessStatisticsMapper;

    /**
     * 异步处理离线任务的线程池
     */
    private ThreadPoolTaskExecutor asyncTaskTreadPool;

    @PostConstruct
    public void init() {
        this.asyncTaskTreadPool = new ThreadPoolTaskExecutor();
        this.asyncTaskTreadPool.initialize();
        this.asyncTaskTreadPool.setCorePoolSize(2);
        this.asyncTaskTreadPool.setMaxPoolSize(8);
        this.asyncTaskTreadPool.setQueueCapacity(16);
        this.asyncTaskTreadPool.setThreadNamePrefix("离线任务线程");
    }

    @PreDestroy
    public void destroy() {
        if (this.asyncTaskTreadPool != null) {
            this.asyncTaskTreadPool.shutdown();
        }
    }


    @Override
    public List<TShortLinkVo> findList(SearchTShortLinkDto searchTShortLinkDto) {

        searchTShortLinkDto.setUserId(SecurityUtils.getUserId());
        List<TShortLinkVo> list = shortLinkDao.findList(searchTShortLinkDto);

        return list;
    }

    /**
     * 生成短链接
     *
     * @param tShortLinkDto 请求类
     * @return {@code String}
     */
    @Override
    public String generateShortLink(TShortLinkDto tShortLinkDto) {


        //长链接格式校验
        boolean validURL = isValidURL(tShortLinkDto.getLongLink());
        if (!validURL) {
            throw new ServiceException(SystemStateCodeEnum.ERROR_URL_FORMAT.getMsg());
        }

        if (tShortLinkDto.getNumLimitLink() != null) {
            boolean validNumLimit = isValidURL(tShortLinkDto.getNumLimitLink());
            if (!validNumLimit) {
                throw new ServiceException(SystemStateCodeEnum.ERROR_URL_FORMAT.getMsg());
            }
        }


        //校验分组合法性
        int oneself = groupMapper.isOneself(SecurityUtils.getUserId(), tShortLinkDto.getGroupId());
        if (oneself <= 0) {
            throw new ServiceException(SystemStateCodeEnum.ERROR_REQUEST_ILLEGAL.getMsg());
        }

        Long timestamp = System.currentTimeMillis();

        // 使用 Murmurhash算法，进行哈希，得到长链接Hash值
        long longLinkHash = Hashing.murmur3_32().hashString(tShortLinkDto.getLongLink() + timestamp, StandardCharsets.UTF_8).padToLong();

        String domainKey = UrlKeyEnum.getDomainKeyMap().get(tShortLinkDto.getDomainType());
        String urlKey = regenerateOnHashConflict(domainKey, tShortLinkDto.getLongLink(), longLinkHash);

        String shortUrl = UrlKeyEnum.getDomainNameMap().get(domainKey) + urlKey;

        TShortLink tShortLink = new TShortLink();
        BeanUtils.copyProperties(tShortLinkDto, tShortLink);
        if (tShortLink.getAccessPassword() == null || tShortLink.getAccessPassword().isEmpty()) {
            tShortLink.setAccessPassword(null);
        } else {
            tShortLink.setAccessPassword(tShortLink.getAccessPassword().replaceAll("\\s", ""));
        }

        tShortLink.setDomainKey(domainKey);
        tShortLink.setUrlKey(urlKey);
        tShortLink.setShortLink(shortUrl);
        tShortLink.setVisitsNum(0);
        tShortLink.setStatus(1);
        tShortLink.setUserId(SecurityUtils.getUserId());
        tShortLink.setCreateTime(new Date());
        tShortLink.setUpdateTime(new Date());
        shortLinkDao.insertShortUrl(tShortLink);
        return shortUrl;

    }

    @Override
    public String findByShortLink(String domainKey, String urlKey, LinkAccessStatist linkAccessStatist) {

        TShortLink tShortLink = shortLinkDao.selectByShortLink(domainKey, urlKey);

        //查询不到或者状态停用
        if (tShortLink == null || tShortLink.getStatus() != 1) {
            return null;
        }

        //设置了过期时间并且超过了当前时间，该短链失效
        if (tShortLink.getExpiryTime() != null && DateUtils.getNowDate().after(tShortLink.getExpiryTime())) {
            return null;
        }

        if (tShortLink.getAccessPassword() != null) {
            //需要密码访问逻辑处理
            //TODO
        }

        //访问次数到达阈值跳转超过阈值的长链接
        if (tShortLink.getNumLimit() > 0) {
            long accessNum = linkAccessStatisticsMapper.selectUrlCount(tShortLink.getId());
            if (accessNum > tShortLink.getNumLimit()) {
                return tShortLink.getNumLimitLink();
            }
        }

        CompletableFuture.runAsync(() -> {
            linkAccessStatist.setShortLinkId(tShortLink.getId());
            insertAccessLog(linkAccessStatist, tShortLink);
        }, asyncTaskTreadPool);

        return tShortLink.getLongLink();
    }

    @Override
    public int delLink(List<Long> idList) {
        return shortLinkDao.delLink(idList, SecurityUtils.getUserId());
    }

    @Override
    public int updateLink(UpdateTShortLinkDto updateTShortLinkDto) {

        updateTShortLinkDto.setUpdateTime(DateUtils.getNowDate());
        updateTShortLinkDto.setUserId(SecurityUtils.getUserId());
        return shortLinkDao.updateLink(updateTShortLinkDto);

    }

    @Override
    public int updateStatus(Long id, boolean status, Long userId) {

        return shortLinkDao.updateStatus(id, status, userId);
    }


    //插入短链访问记录
    public int insertAccessLog(LinkAccessStatist linkAccessStatist, TShortLink tShortLink) {
        linkAccessStatist.setCreateTime(DateUtils.getNowDate());
        linkAccessStatist.setAddress(getCity(linkAccessStatist.getIp()));

        long num = tShortLink.getVisitsNum() + 1;

        shortLinkDao.updateNum(tShortLink.getId(), num);

        return linkAccessStatisticsMapper.insertLog(linkAccessStatist);
    }


    // 长链接缩短方法
    private String regenerateOnHashConflict(String domainKey, String longLink, long longLinkHash) {
        // 这个工具类是 雪花算法的工具类
        SnowFlakeUtils snowFlakeUtil = new SnowFlakeUtils();
        // 雪花算法 生成主键id
        long id = snowFlakeUtil.nextId();
        long uniqueIdHash = Hashing.murmur3_32().hashLong(id).padToLong();
        // 相减主要是为了让哈希值更小
        String urlKey = Base62Utils.encodeToBase62String(Math.abs(longLinkHash - uniqueIdHash));

        log.info("缩短后key:{}", urlKey);

        TShortLink selectUrl = shortLinkDao.selectByOneself(urlKey, domainKey);

        if (selectUrl == null) {
            return urlKey;
        }
        // 如果有 短链接 重复 再走一遍
        return regenerateOnHashConflict(domainKey, longLink, longLinkHash);
    }


    //  http/https链接校验
    public static boolean isValidURL(String url) {
        String regex = "https?://.+";
        return url.matches(regex);
    }

    //第三方api通过ip获取城市信息
    public String getCity(String ip) {
         String city = null;

        String url = "http://ip-api.com/json/" + ip + "?lang=zh-CN";
        String postData = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .execute().body();
        log.info("请求响应结果：{}", postData);

        JSONObject postDataResult = JSONUtil.parseObj(postData);

        if (postDataResult.get("status").toString().equals("success")) {

            city =   String.join(",",postDataResult.get("country").toString(), postDataResult.get("regionName").toString(), postDataResult.get("city").toString());
        }
        return city;
    }


}
