package com.yunzen.jijuaner.search.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.to.FundListTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.search.config.SearchConfig;
import com.yunzen.jijuaner.search.feign.FundFeignService;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("searchService")
@Slf4j
public class SearchService {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    FundFeignService fundFeignService;

    private static final String JIJUANER_FUNDLIST = "jijuaner_fundlist";

    public void upAll() throws IOException {
        R allListResp = fundFeignService.getAllList();
        if (allListResp.getCode() != 0) {
            throw JiJuanerException.FEIGN_EXCEPTION;
        }
        List<FundListTo> allList = JSON.parseObject(JSON.toJSONString(allListResp.getData()),
                new TypeReference<List<FundListTo>>() {
                });
        BulkRequest bulkRequest = new BulkRequest();
        for (FundListTo fundListTo : allList) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index(JIJUANER_FUNDLIST);
            indexRequest.id(fundListTo.getFundCode());
            indexRequest.source(JSON.toJSONString(fundListTo), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse resp = client.bulk(bulkRequest, SearchConfig.COMM_OPTIONS);
        if (resp.hasFailures()) {
            List<String> errorFundcodes = Arrays.stream(resp.getItems()).map(BulkItemResponse::getId).toList();
            String message = "upAll错误，fundCode: " + errorFundcodes;
            log.error(message);
            throw JiJuanerException.UP_FUND_EXCEPTION.putMessage(message);
        }
    }

    /**
     * 根据基金代码或基金名称或基金类型查找, 默认返回前 10 条
     */
    public List<FundListTo> search(String input) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(JIJUANER_FUNDLIST);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.should(QueryBuilders.matchQuery("fundCode", input));
        boolQuery.should(QueryBuilders.matchQuery("fundName", input));
        boolQuery.should(QueryBuilders.matchQuery("fundType", input));
        sourceBuilder.query(boolQuery);

        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        var result = new ArrayList<FundListTo>();
        for (SearchHit searchHit : searchHits) {
            String sourceAsString = searchHit.getSourceAsString();
            FundListTo fundListTo = JSON.parseObject(sourceAsString, FundListTo.class);
            result.add(fundListTo);
        }

        return result;
    }
}
