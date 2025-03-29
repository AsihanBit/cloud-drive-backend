package com.netdisk.cloudserver.config;

import com.netdisk.properties.ElasticSearchProperties;
import com.netdisk.utils.ElasticSearchUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfiguration {
    private ElasticSearchProperties elasticSearchProperties;

    public ElasticSearchConfiguration(ElasticSearchProperties elasticSearchProperties) {
        this.elasticSearchProperties = elasticSearchProperties;
    }

    // TODO 'org.elasticsearch.client.RestHighLevelClient' 已弃用
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 创建认证信息
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(elasticSearchProperties.getUsername(), elasticSearchProperties.getPassword())
        );

        RestClientBuilder builder = RestClient.builder(
//                new HttpHost("localhost", 9200, "http") // Elasticsearch 地址和端口
                new HttpHost(elasticSearchProperties.getHost(),
                        elasticSearchProperties.getPort(),
                        elasticSearchProperties.getScheme()) // Elasticsearch 地址和端口
        ).setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        return new RestHighLevelClient(builder);
    }


    // 也可以直接 Utils里@Component
    @Bean
    @ConditionalOnMissingBean
    public ElasticSearchUtils elasticSearchUtils(RestHighLevelClient restHighLevelClient) {
        return new ElasticSearchUtils(restHighLevelClient);
    }
}
