package com.antiquestore.ds;

import com.antiquestore.model.Item;
import com.antiquestore.model.ItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {

    private BinarySearchTree bst;

    private Item createItem(int id, String name, double price) {
        return new Item(id, name, "Category", 1, "Good", BigDecimal.valueOf(price), ItemStatus.AVAILABLE);
    }

    @BeforeEach
    void setUp() {
        bst = new BinarySearchTree();
    }

    @Test
    @DisplayName("Test insert and search on BST")
    void testInsertAndSearch() {
        assertTrue(bst.isEmpty());

        Item item1 = createItem(5, "Item 5", 500.0);
        Item item2 = createItem(2, "Item 2", 200.0);
        Item item3 = createItem(8, "Item 8", 800.0);

        bst.insert(item1);
        bst.insert(item2);
        bst.insert(item3);

        assertEquals(3, bst.size());
        assertEquals("Item 5", bst.search(5).getName());
        assertEquals("Item 2", bst.search(2).getName());
        assertEquals("Item 8", bst.search(8).getName());
        assertNull(bst.search(99));
        assertNull(bst.search(null));
    }

    @Test
    @DisplayName("Test updating item on duplicate key insert")
    void testInsertDuplicateKeyUpdatesValue() {
        Item item1 = createItem(5, "Original Name", 100.0);
        bst.insert(item1);
        assertEquals(1, bst.size());
        assertEquals("Original Name", bst.search(5).getName());

        Item updated = createItem(5, "Updated Name", 150.0);
        bst.insert(updated);
        assertEquals(1, bst.size());
        assertEquals("Updated Name", bst.search(5).getName());
        assertEquals(BigDecimal.valueOf(150.0), bst.search(5).getPrice());
    }

    @Test
    @DisplayName("Test in-order traversal sorts items by item_id ascending")
    void testInorderTraversal() {
        bst.insert(createItem(50, "Item 50", 100));
        bst.insert(createItem(20, "Item 20", 200));
        bst.insert(createItem(70, "Item 70", 300));
        bst.insert(createItem(10, "Item 10", 400));
        bst.insert(createItem(30, "Item 30", 500));
        bst.insert(createItem(60, "Item 60", 600));
        bst.insert(createItem(80, "Item 80", 700));

        List<Item> items = bst.inorder();
        assertEquals(7, items.size());
        assertEquals(10, items.get(0).getItemId());
        assertEquals(20, items.get(1).getItemId());
        assertEquals(30, items.get(2).getItemId());
        assertEquals(50, items.get(3).getItemId());
        assertEquals(60, items.get(4).getItemId());
        assertEquals(70, items.get(5).getItemId());
        assertEquals(80, items.get(6).getItemId());
    }

    @Test
    @DisplayName("Test delete leaf node (0 children)")
    void testDeleteLeafNode() {
        bst.insert(createItem(50, "Item 50", 100));
        bst.insert(createItem(20, "Item 20", 200));
        bst.insert(createItem(70, "Item 70", 300));

        assertTrue(bst.delete(20));
        assertEquals(2, bst.size());
        assertNull(bst.search(20));
        assertEquals(50, bst.inorder().get(0).getItemId());
        assertEquals(70, bst.inorder().get(1).getItemId());
    }

    @Test
    @DisplayName("Test delete node with 1 child")
    void testDeleteNodeWithOneChild() {
        bst.insert(createItem(50, "Item 50", 100));
        bst.insert(createItem(20, "Item 20", 200));
        bst.insert(createItem(10, "Item 10", 300)); // 20 has left child 10

        assertTrue(bst.delete(20));
        assertEquals(2, bst.size());
        assertNull(bst.search(20));
        assertNotNull(bst.search(10));
        assertEquals(10, bst.inorder().get(0).getItemId());
        assertEquals(50, bst.inorder().get(1).getItemId());
    }

    @Test
    @DisplayName("Test delete node with 2 children")
    void testDeleteNodeWithTwoChildren() {
        bst.insert(createItem(50, "Item 50", 100));
        bst.insert(createItem(20, "Item 20", 200));
        bst.insert(createItem(70, "Item 70", 300));
        bst.insert(createItem(60, "Item 60", 400));
        bst.insert(createItem(80, "Item 80", 500));

        // Delete node 70 which has children 60 and 80
        assertTrue(bst.delete(70));
        assertEquals(4, bst.size());
        assertNull(bst.search(70));
        assertNotNull(bst.search(60));
        assertNotNull(bst.search(80));

        List<Item> inorder = bst.inorder();
        assertEquals(4, inorder.size());
        assertEquals(20, inorder.get(0).getItemId());
        assertEquals(50, inorder.get(1).getItemId());
        assertEquals(60, inorder.get(2).getItemId());
        assertEquals(80, inorder.get(3).getItemId());
    }

    @Test
    @DisplayName("Test delete root node")
    void testDeleteRootNode() {
        bst.insert(createItem(50, "Item 50", 100));
        bst.insert(createItem(20, "Item 20", 200));
        bst.insert(createItem(80, "Item 80", 300));

        assertTrue(bst.delete(50));
        assertEquals(2, bst.size());
        assertNull(bst.search(50));
        assertEquals(20, bst.inorder().get(0).getItemId());
        assertEquals(80, bst.inorder().get(1).getItemId());
    }

    @Test
    @DisplayName("Test delete non-existent key")
    void testDeleteNonExistentKey() {
        bst.insert(createItem(10, "Item 10", 100));
        assertFalse(bst.delete(99));
        assertFalse(bst.delete(null));
        assertEquals(1, bst.size());
    }

    @Test
    @DisplayName("Test price range search on BST")
    void testRangeSearchByPrice() {
        bst.insert(createItem(1, "Gauntlet", 310000.0));
        bst.insert(createItem(2, "Watch", 42000.0));
        bst.insert(createItem(3, "Lamp", 58000.0));
        bst.insert(createItem(4, "Desk", 245000.0));
        bst.insert(createItem(5, "Doll", 18500.0));

        // Range between 40000 and 250000
        List<Item> results = bst.rangeSearch(BigDecimal.valueOf(40000.0), BigDecimal.valueOf(250000.0));
        assertEquals(3, results.size());
        assertTrue(results.stream().anyMatch(i -> i.getName().equals("Watch")));
        assertTrue(results.stream().anyMatch(i -> i.getName().equals("Lamp")));
        assertTrue(results.stream().anyMatch(i -> i.getName().equals("Desk")));

        // Range with min only
        List<Item> above200k = bst.rangeSearch(BigDecimal.valueOf(200000.0), null);
        assertEquals(2, above200k.size());

        // Range with max only
        List<Item> below50k = bst.rangeSearch(null, BigDecimal.valueOf(50000.0));
        assertEquals(2, below50k.size());

        // Range with no matches
        List<Item> none = bst.rangeSearch(BigDecimal.valueOf(500000.0), BigDecimal.valueOf(600000.0));
        assertTrue(none.isEmpty());
    }

    @Test
    @DisplayName("Test clear BST")
    void testClear() {
        bst.insert(createItem(1, "Item 1", 100));
        bst.insert(createItem(2, "Item 2", 200));
        assertEquals(2, bst.size());

        bst.clear();
        assertEquals(0, bst.size());
        assertTrue(bst.isEmpty());
        assertTrue(bst.inorder().isEmpty());
    }
}
