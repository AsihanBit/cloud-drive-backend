package com.netdisk.cloudserver;

import cn.hutool.json.JSONConfig;
import cn.hutool.json.JSONUtil;
import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.constant.MessageConstant;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.FileChunkException;
import com.netdisk.properties.DiskProperties;
import com.netdisk.utils.CipherUtils;
import com.netdisk.utils.ElasticSearchUtils;
import com.netdisk.utils.UserInitializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootTest
class CloudServerApplicationTests {

    private static final Logger log = LoggerFactory.getLogger(CloudServerApplicationTests.class);
    @Autowired
    private DiskProperties diskProperties;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RestHighLevelClient esClient;

    @Autowired
    private UserFilesMapper userFilesMapper;

    private ElasticSearchUtils elasticSearchUtils;

//    private CipherUtils cipherUtils;

    private UserInitializer userInitializer;


    @Test
    public void getFileMD5() throws IOException {

        FileInputStream fis = new FileInputStream(new File("E:\\cloudfile\\butter.jpg"));
        String md5Str = DigestUtils.md5DigestAsHex(fis);
        System.out.println(md5Str);


    }

    @Test
    public void createSubDir() {

        // 使用哈希值的前两位作为子目录名
        String hashPrefix = "aaaaa";
        Path subDirectory = Paths.get(hashPrefix);

        // 存储文件块

        // 设置默认的临时目录路径
        Path defaultTempDir = Paths.get(diskProperties.getTempDir());

        // 创建完整的存储路径（defaultTempDir/tempDir）
        Path completeTempDir = defaultTempDir.resolve(subDirectory);

        if (!Files.exists(completeTempDir)) {
            // 创建子目录
            try {
                Files.createDirectories(completeTempDir);
            } catch (IOException e) {
                throw new FileChunkException(MessageConstant.FAILED_TO_CREATE_SUB_DIR);
            }
        }

        // 构建分片文件路径，名称为 "md5值+chunkNumber"
        Path chunkFilePath = completeTempDir.resolve(hashPrefix + "_" + String.format("%05d", "1"));


    }


    @Test
    public void testRedis() {
        System.out.println("测试redis");
        System.out.println(redisTemplate);
        redisTemplate.opsForValue().set("key1", "value1");
    }

    @Test
    public void testFileExtension() {
        String itemName = "l月外网搬运 - 副本 - 副本.zip";
        if (itemName == null) {
            System.out.println("");
        }
        int lastIndex = itemName.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == itemName.length() - 1) {
            System.out.println("");

        }
        System.out.println(itemName.substring(lastIndex + 1));
    }


    @Test
    public void importUserFileDataToEs() throws IOException {
        // 从 user_file 表中查询所有数据
        List<UserFiles> userFiles = userFilesMapper.selectAllUserItems();

        // 配置 JSONUtil 忽略 null 字段
        JSONConfig config = JSONConfig.create().setIgnoreNullValue(true);

        // 1. 请求对象
        BulkRequest request = new BulkRequest();
        // 2. 请求参数
        for (UserFiles item : userFiles) {
            request.add(
                    new IndexRequest("user_files")
                            .id(item.getItemId().toString())
                            .source(JSONUtil.toJsonStr(item, config), XContentType.JSON));

        }

        esClient.bulk(request, RequestOptions.DEFAULT);
    }


    // 测试加密算法
    @Test
    public void cipherTest() throws Exception {
        int shareId = 246810;
        log.info("分享id: {}", shareId);

        String shareStr = CipherUtils.encryptCBC(shareId);
        log.info("分享id-加密后: {}", shareStr);

        int decryptShareId = CipherUtils.decryptCBC(shareStr);
        log.info("分享id-解密后: {}", decryptShareId);


        String shareStrCBC = CipherUtils.encryptCBC(shareId);
        log.info("分享id-CBC加密后: {}", shareStrCBC);
        String shareStrCBC1 = CipherUtils.encryptCBC(shareId);
        log.info("分享id-CBC加密后1: {}", shareStrCBC1);
        String shareStrCBC2 = CipherUtils.encryptCBC(shareId);
        log.info("分享id-CBC加密后1: {}", shareStrCBC2);

        int decryptShareIdCBC = CipherUtils.decryptCBC(shareStrCBC);
        log.info("分享id-解密后: {}", decryptShareIdCBC);
        int decryptShareIdCBC1 = CipherUtils.decryptCBC(shareStrCBC1);
        log.info("分享id-解密后1: {}", decryptShareIdCBC1);
        int decryptShareIdCBC2 = CipherUtils.decryptCBC(shareStrCBC2);
        log.info("分享id-解密后2: {}", decryptShareIdCBC2);


    }

    // 测试url编码
    @Test
    public void base64UrlTest() {
        // 然后可以使用这个 url 进行导航或请求
        String shareStr = "KXxDsWhifz7e5FM+ug6d0w==";
        try {
            // 对 shareStr 进行 URL 编码
            String encodedShareStr = URLEncoder.encode(shareStr, StandardCharsets.UTF_8.toString());
            System.out.println("编码后的 shareStr: " + encodedShareStr);

            // 构造完整的 URL
            String baseUrl = "http://localhost:5173/#/sharelink";
            String shareCode = "e78K";
            String fullUrl = baseUrl + "?shareStr=" + encodedShareStr + "&shareCode=" + shareCode;
            System.out.println("完整的 URL: " + fullUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    // 测试初始化用户示例文档
    @Test
    public void initialUserFiles() {
//        userInitializer.insertDefaultFiles(14);
    }
}
