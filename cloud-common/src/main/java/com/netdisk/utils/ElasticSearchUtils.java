package com.netdisk.utils;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.netdisk.entity.UserFiles;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;

@Data
@AllArgsConstructor
public class ElasticSearchUtils {
    private RestHighLevelClient esClient;

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

        return true;
    }

}
