package org.soso.ledger;

public class singleThreadTea {
    public static void main(String[] agrs) throws InterruptedException {
        long start = System.currentTimeMillis();
        System.out.print("startTime" + start);
        for (int i = 0; i <= 3; i++) {
            boilWater(i);
            makeTea(i);     // 1 秒
        }
    }

    static void boilWater(int i) throws InterruptedException {
        System.out.println("烧水中..." + i);
        Thread.sleep(3000);   // 模拟耗时 3 秒
    }

    static void makeTea(int i) throws InterruptedException {
        System.out.println("泡茶..." + i);
        Thread.sleep(1000);
    }
}
