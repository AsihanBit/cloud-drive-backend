package com.netdisk.utils;

import com.netdisk.constant.FileConstant;
import com.netdisk.constant.TimeConstant;
import com.netdisk.enums.FileUnitEnum;
import lombok.extern.slf4j.Slf4j;

/**
 * 带宽工具类
 */
@Slf4j
public class BandwidthLimiter {

    /**
     * Redis工具类
     */
    private RedisUtil redisUtil;

    /**
     * 最大传输速率(单位/s)  默认 1024 单位/s
     */
    private long maxRate;

    /**
     * 每个文件分片在当前速率下需要的处理时间（毫秒）
     */
    private long timeCostPerChunk;

    /**
     * 当前时间窗口内已处理的字节数
     */
    private long currentWindowBytes;

    /**
     * 总传输字节数
     */
    private long totalBytesTransferred;

    /**
     * 上次限速检查的时间戳
     */
    private long lastCheckTime;

    /**
     * 分片字节大小
     */
    private long chunkSize = FileConstant.CHUNK_SIZE;

    /**
     * 创建指定最大速率的带宽限制器
     */
    public BandwidthLimiter(RedisUtil redisUtil) {
        timeCostPerChunk = (TimeConstant.MILLIS_PER_SECOND * this.chunkSize) / (FileConstant.DEFAULT_MAX_BANDWIDTH_RATE * FileUnitEnum.KB.getBytes());
        currentWindowBytes = 0;
        totalBytesTransferred = 0;
        lastCheckTime = System.currentTimeMillis();
        this.redisUtil = redisUtil;
    }
    /*
    System.nanoTime()
     */

    /**
     * 动态调整最大速率
     *
     * @param maxRate
     */
    public void setMaxRate(int maxRate) {
        if (maxRate < 0) {
            throw new IllegalArgumentException("最大传输速率不可以小于0");
        }
        this.maxRate = maxRate;
        if (maxRate == 0) {
            this.timeCostPerChunk = 0;
        } else {
            this.timeCostPerChunk = (TimeConstant.MILLIS_PER_SECOND * this.chunkSize) / (this.maxRate * FileUnitEnum.KB.getBytes());
        }
    }

    public synchronized void limitNextBytes(int len) {
        //累计已经发送/接收了多少字节数
        this.currentWindowBytes += len;
        this.totalBytesTransferred += len; // 累加总传输量

        //当累计的字节数大于定义的数据块大小时
        while (this.currentWindowBytes > this.chunkSize) {
            long nowTick = System.nanoTime();
            //计算积累数据期间消耗的时间
            long passTime = nowTick - this.lastCheckTime;
            //timeCostPerChunk表示单个块最多需要多少纳秒
            //如果missedTime大于0，说明此时流量进出的速率已经超过maxRate了，需要休眠来限制流量
            long missedTime = this.timeCostPerChunk - passTime;
            if (missedTime > 0) {
                try {
                    long sleepMill = missedTime / 1000000;
                    int sleepNano = (int) (missedTime % 1000000);

                    // 添加合理的上限防止过长休眠
                    if (sleepMill > 5000) { // 不超过5秒
                        sleepMill = 1000;
                        sleepNano = 0;
                    }
                    log.info("达到上限,开始限速,总传输量:{} byte,{} MB, 当前chunk传输量:{}, 准备休眠:{} 毫秒, {} 秒",
                            totalBytesTransferred, totalBytesTransferred / (1024 << 10), currentWindowBytes, sleepMill + sleepNano, (sleepMill + sleepNano) / 1000);
                    Thread.sleep(sleepMill, sleepNano);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
            //重置累计发送/接收的字节数
            this.currentWindowBytes -= this.chunkSize;
            //重置最后一次检查时间
            this.lastCheckTime = nowTick + (missedTime > 0 ? missedTime : 0);
        }
    }


    /*


    public synchronized void limitNextBytes(int len) {
        //累计已经发送/接收了多少字节数
        this.bytesProcessed += len;
        this.totalBytesProcessed += len; // 累加总传输量

        //当累计的字节数大于定义的数据块大小时
        while (this.bytesProcessed > CHUNK_LENGTH) {
            long nowTick = System.nanoTime();
            //计算积累数据期间消耗的时间
            long passTime = nowTick - this.lastCheckTime;
            //timeCostPerChunk表示单个块最多需要多少纳秒
            //如果missedTime大于0，说明此时流量进出的速率已经超过maxRate了，需要休眠来限制流量
            long missedTime = this.timePerChunk - passTime;
            if (missedTime > 0) {
                try {
                    long sleepMill = missedTime / 1000000;
                    int sleepNano = (int) (missedTime % 1000000);

                    // 添加合理的上限防止过长休眠
                    if (sleepMill > 5000) { // 不超过5秒
                        sleepMill = 1000;
                        sleepNano = 0;
                    }
                    log.info("达到上限,开始限速,总传输量:{} byte,{} MB, 当前chunk传输量:{}, 准备休眠:{} 毫秒, {} 秒",
                            totalBytesProcessed, totalBytesProcessed / (1024 << 10), bytesProcessed, sleepMill + sleepNano, (sleepMill + sleepNano) / 1000);
                    Thread.sleep(sleepMill, sleepNano);
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            //重置累计发送/接收的字节数
            this.bytesProcessed -= CHUNK_LENGTH;
            //重置最后一次检查时间
            this.lastCheckTime = nowTick + (missedTime > 0 ? missedTime : 0);
        }
    }


     */

}
