package com.netdisk.cloudserver.controller;

import cn.hutool.json.JSONUtil;
import com.netdisk.context.BaseContext;
import com.netdisk.entity.UserFiles;
import com.netdisk.result.Result;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/search")
public class FileSearchController {

    private RestHighLevelClient esClient; // 新版本已弃用

    public FileSearchController(RestHighLevelClient restHighLevelClient) {
        this.esClient = restHighLevelClient;
    }

    @GetMapping("/keyword")
    public Result<List<UserFiles>> searchItemsByKeyword(@RequestParam String keyword) throws IOException {
        // 获取用户id
        Integer userId = BaseContext.getCurrentId();
        // 1. 创建对象
        SearchRequest request = new SearchRequest("user_files");
        // 2. 配置参数
        // 创建 bool 查询构建器
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 添加关键字匹配条件
        MatchQueryBuilder keywordQuery = QueryBuilders.matchQuery("itemName", keyword);
        boolQuery.must(keywordQuery);

        // 添加 userId 匹配条件
        MatchQueryBuilder userIdQuery = QueryBuilders.matchQuery("userId", userId);
        boolQuery.must(userIdQuery);

        // 配置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("itemName"); // 指定要高亮的字段
        highlightBuilder.preTags("<em>"); // 高亮前缀标签
        highlightBuilder.postTags("</em>"); // 高亮后缀标签

        request.source()
                .query(boolQuery)
                .highlighter(highlightBuilder);


//        request.source()
//                .query(QueryBuilders.matchQuery("itemName", keyword)); // matchAll呢?
        // 3. 发送请求
        SearchResponse response = esClient.search(request, RequestOptions.DEFAULT);

        // 4. 解析结果
        SearchHits searchHits = response.getHits();
        // 4.1 总条数
        long totalCount = searchHits.getTotalHits().value;
        // 4.2 命中数据
        SearchHit[] hits = searchHits.getHits();
        List<UserFiles> userItemList = new ArrayList<>();
        for (SearchHit hit : hits) {
            // 4.2.1 获取source
            String json = hit.getSourceAsString();
            // 4.2.2 转成 UserFiles
            UserFiles userItems = JSONUtil.toBean(json, UserFiles.class);

            // 处理高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField itemNameHighlight = highlightFields.get("itemName");
            if (itemNameHighlight != null) {
                String highlightedItemName = itemNameHighlight.getFragments()[0].toString();
                userItems.setItemName(highlightedItemName);
            }

            userItemList.add(userItems);
        }
        return Result.success(userItemList);
    }
}
