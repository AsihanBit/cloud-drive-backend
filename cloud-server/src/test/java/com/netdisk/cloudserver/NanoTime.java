package com.netdisk.cloudserver;

public class NanoTime {
    public static void main(String[] args) throws InterruptedException {
        long time_0 = System.nanoTime();
        System.out.println(time_0);


        Thread.sleep(1);
        long time_1 = System.nanoTime();
        System.out.println(time_1);
        System.out.println("睡1毫秒" + time_1 + "差值" + ((time_1 - time_0) / 1000000));

        Thread.sleep(3);
        long time_3 = System.nanoTime();
        System.out.println(time_3);
        System.out.println("睡3毫秒" + time_3 + "差值" + ((time_3 - time_1) / 1000000));


        Thread.sleep(10);
        long time_10 = System.nanoTime();
        System.out.println(time_10);
        System.out.println("睡10毫秒" + time_10 + "差值" + ((time_10 - time_3) / 1000000));

        Thread.sleep(300);
        long time_300 = System.nanoTime();
        System.out.println(time_300);
        System.out.println("睡300毫秒" + time_300 + "差值" + ((time_300 - time_10) / 1000000));

        System.out.println("同一行代码连续获取" + System.nanoTime() + "," + System.nanoTime() + "," + System.nanoTime() + "," + System.nanoTime() + "," + System.nanoTime());


        System.out.println(System.nanoTime() == System.nanoTime());
        System.out.println(System.nanoTime() == System.nanoTime());
        System.out.println(System.nanoTime() == System.nanoTime());
        System.out.println(System.nanoTime() == System.nanoTime());
        System.out.println(System.nanoTime() == System.nanoTime());
        System.out.println(System.nanoTime() == System.nanoTime());


    }
}
