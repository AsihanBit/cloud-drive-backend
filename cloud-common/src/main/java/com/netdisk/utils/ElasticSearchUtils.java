package com.netdisk.utils;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.netdisk.entity.UserFiles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ElasticSearchUtils {
    private RestHighLevelClient esClient;

    public ElasticSearchUtils(RestHighLevelClient esClient) {
        this.esClient = esClient;
    }

    /**
     * ElasticSearch 中新增用户条目
     *
     * @param item
     * @return
     */
    public boolean insertUserFilesDoc(UserFiles item) {
        // 1.创建请求对象
        IndexRequest request = new IndexRequest("user_files").id(item.getItemId().toString());

        // 配置 JSONUtil 忽略 null 字段
        JSONConfig config = JSONConfig.create().setIgnoreNullValue(true);

        // 2.准备json文档
        request.source(JSONUtil.toJsonStr(item, config), XContentType.JSON);

        // 3.发送请求
        try {
            esClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("ElasticSearch新增文档成功");

        return true;
    }

    // ElasticSearch 中新增用户条目 - 批量

    /**
     * ElasticSearch 批量插入用户文件文档
     *
     * @param items 用户文件列表
     * @return 是否全部插入成功
     */
    public boolean bulkInsertUserFiles(List<UserFiles> items) {
        if (items == null || items.isEmpty()) {
            log.warn("批量插入用户文件文档失败：列表为空");
            return false;
        }

        BulkRequest bulkRequest = new BulkRequest();
        JSONConfig config = JSONConfig.create().setIgnoreNullValue(true);

        // 构建批量请求
        items.forEach(item -> {
            IndexRequest request = new IndexRequest("user_files")
                    .id(item.getItemId().toString())
                    .source(JSONUtil.toJsonStr(item, config), XContentType.JSON);
            bulkRequest.add(request);
        });

        // 执行批量操作
        try {
            esClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("ElasticSearch批量新增文档成功，共 {} 条", items.size());
            return true;
        } catch (IOException e) {
            log.error("ElasticSearch批量新增文档失败", e);
            throw new RuntimeException("批量插入文档失败", e);
        }
    }


    /**
     * 根据itemId删除条目及其所有子条目
     *
     * @param itemId 要删除的条目ID
     * @return 是否删除成功
     */
    public boolean deleteItemAndChildren(Integer itemId) {
        try {
            // 1. 先删除所有子条目
            deleteChildrenItems(itemId);

            // 2. 再删除父条目
            DeleteRequest deleteRequest = new DeleteRequest("user_files", itemId.toString());
            DeleteResponse deleteResponse = esClient.delete(deleteRequest, RequestOptions.DEFAULT);

            log.info("成功删除条目ID: {} 及其所有子条目", itemId);
            return true;
        } catch (IOException e) {
            log.error("删除条目失败，ID: {}", itemId, e);
            throw new RuntimeException("删除条目失败", e);
        }
    }

    /**
     * 删除所有p_id等于指定itemId的子条目
     *
     * @param parentId 父条目ID
     */
    private void deleteChildrenItems(Integer parentId) throws IOException {
        // 构建查询条件：p_id = parentId
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("pId", parentId));

        SearchRequest searchRequest = new SearchRequest("user_files");
        searchRequest.source(sourceBuilder);

        // 执行查询
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

        // 遍历结果并删除
        searchResponse.getHits().forEach(hit -> {
            try {
                Integer childId = Integer.parseInt(hit.getId());
                DeleteRequest deleteRequest = new DeleteRequest("user_files", childId.toString());
                esClient.delete(deleteRequest, RequestOptions.DEFAULT);
                log.debug("已删除子条目ID: {}", childId);
            } catch (IOException e) {
                log.error("删除子条目失败，ID: {}", hit.getId(), e);
                throw new RuntimeException("删除子条目失败", e);
            }
        });
    }


}
