package com.antiquestore.ds;

import com.antiquestore.model.Item;

public class TreeNode {
    private Integer key; // item_id
    private Item value;
    private TreeNode left;
    private TreeNode right;

    public TreeNode(Integer key, Item value) {
        this.key = key;
        this.value = value;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public Item getValue() {
        return value;
    }

    public void setValue(Item value) {
        this.value = value;
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }
}
