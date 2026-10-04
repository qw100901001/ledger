package org.soso.ledger;

public class SimpleBST {
    public static void main(String[] args) {
        SimpleBST tree = new SimpleBST();

        // 故意按顺序插入，制造普通BST的“退化”惨案
        tree.insert(10);
        tree.insert(50);
        tree.insert(20);
        tree.insert(40);
        tree.insert(90);

        System.out.print("中序遍历结果（刚好是从小到大排序）：");
        tree.inorder();
        // 输出：10 20 30 40 50

        System.out.println("查找 30 是否存在：" + tree.search(30)); // true

    }

    static class Node {
        int value;
        Node left;
        Node right;

        public Node(int value) {
            this.value = value;
        }

    }

    //整棵树根节点
    private Node root;

    //插入操作
    public void insert(int value) {
        root = insertRec(root, value);
    }

    private Node insertRec(Node current, int value) {
        //如果当前的位置为空，直接插入
        if (current == null) {
            return new Node(value);
        }
        // 左小右大，递归寻找位置
        if (value < current.value) {
            current.left = insertRec(current.left, value);
        } else if (value > current.value) {
            current.right = insertRec(current.right, value);
        } else {
            // 值相等，通常不插入重复值，直接返回
            return current;
        }
        return current;
    }

    // ================= 查找操作 =================
    public boolean search(int value) {
        return searchRec(root, value);
    }

    private boolean searchRec(Node current, int value) {
        // 走到空节点还没找到，说明不存在
        if (current == null) {
            return false;
        }
        // 找到了
        if (current.value == value) {
            return true;
        }
        // 左小右大，决定去哪边找
        if (value < current.value) {
            return searchRec(current.left, value);
        } else {
            return searchRec(current.right, value);
        }
    }

    // ================= 中序遍历（左 -> 根 -> 右） =================
    public void inorder() {
        inorderRec(root);
        System.out.println();
    }

    private void inorderRec(Node current) {
        if (current != null) {
            inorderRec(current.left);             // 1. 访问左子树
            System.out.print(current.value + " "); // 2. 访问根节点（在中间！）
            inorderRec(current.right);            // 3. 访问右子树
        }
    }
}
