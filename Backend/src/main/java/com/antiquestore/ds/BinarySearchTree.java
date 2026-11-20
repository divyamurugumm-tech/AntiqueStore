package com.antiquestore.ds;

import com.antiquestore.model.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Custom Binary Search Tree implementation keyed by item_id.
 * No java.util.TreeMap or TreeSet used.
 * Thread-safe using ReentrantReadWriteLock.
 */
@Component
public class BinarySearchTree {

    private TreeNode root;
    private int size = 0;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public BinarySearchTree() {
    }

    /**
     * Inserts an item into the BST keyed by item_id.
     * If an item with the same item_id exists, its value is updated.
     */
    public void insert(Item item) {
        if (item == null || item.getItemId() == null) {
            throw new IllegalArgumentException("Item and item_id must not be null");
        }

        rwLock.writeLock().lock();
        try {
            root = insertRec(root, item);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private TreeNode insertRec(TreeNode current, Item item) {
        if (current == null) {
            size++;
            return new TreeNode(item.getItemId(), item);
        }

        int cmp = item.getItemId().compareTo(current.getKey());
        if (cmp < 0) {
            current.setLeft(insertRec(current.getLeft(), item));
        } else if (cmp > 0) {
            current.setRight(insertRec(current.getRight(), item));
        } else {
            // Key already exists -> update value
            current.setValue(item);
        }
        return current;
    }

    /**
     * Searches for an item by item_id.
     * Returns the Item if found, or null otherwise.
     */
    public Item search(Integer itemId) {
        if (itemId == null) {
            return null;
        }

        rwLock.readLock().lock();
        try {
            TreeNode current = root;
            while (current != null) {
                int cmp = itemId.compareTo(current.getKey());
                if (cmp < 0) {
                    current = current.getLeft();
                } else if (cmp > 0) {
                    current = current.getRight();
                } else {
                    return current.getValue();
                }
            }
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Deletes an item by item_id.
     * Returns true if the item was found and deleted, false otherwise.
     */
    public boolean delete(Integer itemId) {
        if (itemId == null || root == null) {
            return false;
        }

        rwLock.writeLock().lock();
        try {
            int oldSize = size;
            root = deleteRec(root, itemId);
            return size < oldSize;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private TreeNode deleteRec(TreeNode current, Integer key) {
        if (current == null) {
            return null;
        }

        int cmp = key.compareTo(current.getKey());
        if (cmp < 0) {
            current.setLeft(deleteRec(current.getLeft(), key));
        } else if (cmp > 0) {
            current.setRight(deleteRec(current.getRight(), key));
        } else {
            // Node to be deleted found
            size--;

            // Case 1: No child
            if (current.getLeft() == null && current.getRight() == null) {
                return null;
            }

            // Case 2: One child
            if (current.getLeft() == null) {
                return current.getRight();
            }
            if (current.getRight() == null) {
                return current.getLeft();
            }

            // Case 3: Two children -> find in-order successor (min node in right subtree)
            TreeNode minNode = findMin(current.getRight());
            current.setKey(minNode.getKey());
            current.setValue(minNode.getValue());

            // Delete in-order successor from right subtree
            // Increment size back temporarily because deleteRec on minNode will decrement size again
            size++;
            current.setRight(deleteRec(current.getRight(), minNode.getKey()));
        }
        return current;
    }

    private TreeNode findMin(TreeNode node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node;
    }

    /**
     * Performs an in-order traversal (left, root, right) of the BST.
     * Returns items sorted ascending by item_id.
     */
    public List<Item> inorder() {
        rwLock.readLock().lock();
        try {
            List<Item> result = new ArrayList<>();
            inorderRec(root, result);
            return result;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void inorderRec(TreeNode node, List<Item> result) {
        if (node != null) {
            inorderRec(node.getLeft(), result);
            result.add(node.getValue());
            inorderRec(node.getRight(), result);
        }
    }

    /**
     * Searches for items in the BST whose price falls within [minPrice, maxPrice].
     * Traverses the BST and checks item prices.
     */
    public List<Item> rangeSearch(BigDecimal minPrice, BigDecimal maxPrice) {
        rwLock.readLock().lock();
        try {
            List<Item> result = new ArrayList<>();
            rangeSearchRec(root, minPrice, maxPrice, result);
            return result;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void rangeSearchRec(TreeNode node, BigDecimal minPrice, BigDecimal maxPrice, List<Item> result) {
        if (node != null) {
            rangeSearchRec(node.getLeft(), minPrice, maxPrice, result);

            Item item = node.getValue();
            if (item != null && item.getPrice() != null) {
                boolean minMatch = (minPrice == null || item.getPrice().compareTo(minPrice) >= 0);
                boolean maxMatch = (maxPrice == null || item.getPrice().compareTo(maxPrice) <= 0);
                if (minMatch && maxMatch) {
                    result.add(item);
                }
            }

            rangeSearchRec(node.getRight(), minPrice, maxPrice, result);
        }
    }

    /**
     * Clears all nodes from the BST.
     */
    public void clear() {
        rwLock.writeLock().lock();
        try {
            root = null;
            size = 0;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Returns the number of items stored in the BST.
     */
    public int size() {
        rwLock.readLock().lock();
        try {
            return size;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
