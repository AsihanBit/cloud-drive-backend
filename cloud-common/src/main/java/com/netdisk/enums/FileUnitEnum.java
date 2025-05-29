package com.netdisk.enums;

import lombok.Getter;

/**
 * 文件大小单位枚举
 * 用于表示和转换不同的文件大小单位
 */
@Getter
public enum FileUnitEnum {
    /**
     * 单位符号
     */
    B(1L, "B"),
    KB(1L << 10, "KB"),
    MB(1L << 20, "MB"),
    GB(1L << 30, "GB"),
    TB(1L << 40, "TB");

    /**
     * 单位对应的字节数
     */
    private final long bytes;

    /**
     * 单位符号
     */
    private final String symbol;

    /**
     * 构造函数
     *
     * @param bytes  单位对应的字节数
     * @param symbol 单位符号
     */
    FileUnitEnum(long bytes, String symbol) {
        this.bytes = bytes;
        this.symbol = symbol;
    }

    /**
     * 根据字节大小获取最合适的单位
     *
     * @param bytes 字节大小
     * @return 最合适的文件大小单位枚举
     */
    public static FileUnitEnum getUnitByBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("字节数不能为负值");
        }
        FileUnitEnum[] units = FileUnitEnum.values(); // = values(); 也可以
        FileUnitEnum unit = FileUnitEnum.B;

        for (int i = units.length - 1; i >= 0; i--) {
            if (bytes >= units[i].getBytes()) { // .bytes
                unit = units[i];
                break;
            }
        }
        return unit;
    }

    /**
     * 根据单位名称获取对应的枚举
     *
     * @param symbol 单位名称(例如: "KB", "MB")
     * @return 对应的文件大小单位枚举，如果未找到返回B
     */
    public static FileUnitEnum getUnitBySymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return B;
        }

        for (FileUnitEnum unit : FileUnitEnum.values()) { // values()
            if (unit.getSymbol().equalsIgnoreCase(symbol.trim().toUpperCase())) { // unit.symbol
                return unit;
            }
        }
        return B; // 默认返回B单位
    }

    /**
     * 根据字节大小获取格式化的文件大小字符串
     *
     * @param bytes 字节大小
     * @return 格式化后的文件大小字符串，如"1.5 MB"
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0 B";
        }

        FileUnitEnum unit = getUnitByBytes(bytes);
        double value = (double) bytes / unit.getBytes(); // .bytes

        // 如果值是整数，不显示小数部分
        if (value == (long) value) {
            return String.format("%d %s", (long) value, unit.symbol);
        }

        // 格式化，保留1位小数
        return String.format("%.1f %s", value, unit.getSymbol()); // unit.symbol
    }

    /**
     * 将特定单位的大小转换为字节
     *
     * @param value 数值
     * @param unit  单位枚举
     * @return 对应的字节大小
     */
    public static long toBytes(double value, FileUnitEnum unit) {
        if (value < 0) {
            throw new IllegalArgumentException("值不能为负数");
        }
        if (unit == null) {
            throw new IllegalArgumentException("单位不能为空");
        }
        return Math.round(value * unit.bytes);
//        return (long) (value * unit.getBytes());
    }

    /**
     * 将特定单位的大小转换为字节
     *
     * @param value  数值
     * @param symbol 单位名称(例如: "KB", "MB")
     * @return 对应的字节大小
     */
    public static long toBytes(double value, String symbol) {
        return toBytes(value, getUnitBySymbol(symbol));
    }

    /**
     * 将字节大小转换为指定单位的值
     *
     * @param bytes      字节大小
     * @param targetUnit 目标单位
     * @return 转换后的值
     */
    public static double convert(long bytes, FileUnitEnum targetUnit) {
        if (bytes < 0) {
            throw new IllegalArgumentException("字节数不能为负值");
        }
        if (targetUnit == null) {
            throw new IllegalArgumentException("目标单位不能为空");
        }
        return (double) bytes / targetUnit.bytes;
    }

    /**
     * 单位之间的转换
     *
     * @param value    原始值
     * @param fromUnit 原始单位
     * @param toUnit   目标单位
     * @return 转换后的值
     */
    public static double convert(double value, FileUnitEnum fromUnit, FileUnitEnum toUnit) {
        if (value < 0) {
            throw new IllegalArgumentException("值不能为负数");
        }
        if (fromUnit == null || toUnit == null) {
            throw new IllegalArgumentException("单位不能为空");
        }

        // 先转换为字节，再转换为目标单位
        long bytes = Math.round(value * fromUnit.bytes);
        return (double) bytes / toUnit.bytes;
    }

}
