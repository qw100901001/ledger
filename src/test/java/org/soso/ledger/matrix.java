package org.soso.ledger;

import java.util.Arrays;
import java.util.Scanner;

public class matrix {
    public static void main(String [] args) {
        int[][] matrix = {{1, 2, 3}, {4, 5, 6}};
        for (int i = 0; i < matrix.length; i++) {        // 遍历行
            for(int j=0;j<matrix[i].length;j++){
                System.out.print(matrix[i][j]);
            }
            System.out.println();
        }
    }
}
