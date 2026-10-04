package org.soso.ledger;

public class MultiThreadTea {
    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread[] workers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i;
            workers[i] = new Thread(() -> {
                try {
                    boilWater(id);
                    makeTea(id);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            workers[i].start();
        }
        for (Thread t : workers) {
            t.join();
        }
        System.out.println("三个线程总共花费的时间：" + (System.currentTimeMillis() - start) + "ms");
    }

    static void boilWater(int i) throws InterruptedException {
        System.out.println("烧水中..." + i + " [" + Thread.currentThread().getName() + "]");
        Thread.sleep(3000);
    }

    static void makeTea(int i) throws InterruptedException {
        System.out.println("泡茶..." + i + " [" + Thread.currentThread().getName() + "]");
        Thread.sleep(1000);
    }
}
