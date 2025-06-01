package com.netdisk.utils;

public class StringUtils {


    /**
     * 使用指定分隔符拼接字符串
     *
     * @param delimiter 分隔符
     * @param parts     要拼接的字符串部分
     * @return 拼接后的字符串
     */
    public static String join(String delimiter, String... parts) {
        if (parts == null || parts.length == 0) {
            return "";
        }

        // 计算最终字符串长度以优化性能
        int capacity = 0;
        for (String part : parts) {
            capacity += (part != null) ? part.length() : 0;
        }
        capacity += delimiter.length() * (parts.length - 1);

        StringBuilder builder = new StringBuilder(capacity);
        boolean isFirst = true;

        for (String part : parts) {
            if (part != null) {
                if (!isFirst) {
                    builder.append(delimiter);
                } else {
                    isFirst = false;
                }
                builder.append(part);
            }
        }

        return builder.toString();
    }

    /**
     * 构建键值对字符串，格式为prefix:key1:key2:...
     *
     * @param prefix 前缀
     * @param keys   后续键值
     * @return 构建的字符串
     */
    public static String buildKey(String prefix, String... keys) {
        if (prefix == null) {
            prefix = "";
        }

        if (keys == null || keys.length == 0) {
            return prefix;
        }

        // 预估容量
        int capacity = prefix.length();
        for (String key : keys) {
            if (key != null) {
                capacity += key.length();
            }
        }
        capacity += keys.length;


        StringBuilder builder = new StringBuilder(capacity);
        builder.append(prefix);

        for (String key : keys) {
            if (key != null) {
                builder.append(":").append(key);
            }
        }

        return builder.toString();
    }

}
