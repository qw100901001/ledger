package org.soso.ledger;

import java.util.Arrays;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        int[] nums = new int[100];
        Scanner input = new Scanner(System.in);

        System.out.print("请输入你要输入数字的个数 n：");
        int n = input.nextInt();

        System.out.println("请依次输入 " + n + " 个数字（按回车确认）：");

        // 修正1：循环条件改为 i < n，避免越界
        for (int i = 0; i < n; i++) {
            nums[i] = input.nextInt();
        }

        // 修正2：使用 Arrays.toString() 打印真实内容
        // 注意：这里只打印前 n 个有效元素，数组后面全是默认的 0
        int[] validNums = Arrays.copyOf(nums, n);
        System.out.println("你输入的数组是：" + Arrays.toString(validNums));

        // 修正3：初始化 max 和 min 应该从第一个有效元素开始
        if (n > 0) {
            int max = nums[0];
            int min = nums[0];

            // 补充逻辑：遍历数组寻找最大值和最小值
            for (int i = 1; i < n; i++) {
                if (nums[i] > max) {
                    max = nums[i];
                }
                if (nums[i] < min) {
                    min = nums[i];
                }
            }

            System.out.println("最大值是：" + max);
            System.out.println("最小值是：" + min);
        } else {
            System.out.println("没有输入任何数字。");
        }

        input.close(); // 养成好习惯，用完 Scanner 关掉它
    }
}
