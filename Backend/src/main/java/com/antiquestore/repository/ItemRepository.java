package com.antiquestore.repository;

import com.antiquestore.model.Item;
import com.antiquestore.model.ItemStatus;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Item> itemRowMapper = (rs, rowNum) -> {
        Item item = new Item(
                rs.getInt("item_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getInt("era_id"),
                rs.getString("item_condition"),
                rs.getBigDecimal("price"),
                ItemStatus.fromString(rs.getString("status"))
        );
        // If era_name column is present in query result
        try {
            item.setEraName(rs.getString("era_name"));
        } catch (Exception ignored) {
        }
        return item;
    };

    public ItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Item> findAll() {
        String sql = "SELECT i.item_id, i.name, i.category, i.era_id, i.item_condition, i.price, i.status, e.era_name " +
                     "FROM item i " +
                     "LEFT JOIN era e ON i.era_id = e.era_id " +
                     "ORDER BY i.item_id ASC";
        return jdbcTemplate.query(sql, itemRowMapper);
    }

    public List<Item> findByStatus(ItemStatus status) {
        String sql = "SELECT i.item_id, i.name, i.category, i.era_id, i.item_condition, i.price, i.status, e.era_name " +
                     "FROM item i " +
                     "LEFT JOIN era e ON i.era_id = e.era_id " +
                     "WHERE i.status = ? " +
                     "ORDER BY i.item_id ASC";
        return jdbcTemplate.query(sql, itemRowMapper, status.name());
    }

    public Optional<Item> findById(Integer id) {
        String sql = "SELECT i.item_id, i.name, i.category, i.era_id, i.item_condition, i.price, i.status, e.era_name " +
                     "FROM item i " +
                     "LEFT JOIN era e ON i.era_id = e.era_id " +
                     "WHERE i.item_id = ?";
        try {
            Item item = jdbcTemplate.queryForObject(sql, itemRowMapper, id);
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM item WHERE item_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public Item insert(Item item) {
        String sql = "INSERT INTO item (name, category, era_id, item_condition, price, status) VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getEraId());
            ps.setString(4, item.getItemCondition());
            ps.setBigDecimal(5, item.getPrice());
            ps.setString(6, item.getStatus() != null ? item.getStatus().name() : ItemStatus.AVAILABLE.name());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            item.setItemId(key.intValue());
        }
        return item;
    }

    public boolean update(Item item) {
        String sql = "UPDATE item SET name = ?, category = ?, era_id = ?, item_condition = ?, price = ?, status = ? WHERE item_id = ?";
        int rows = jdbcTemplate.update(
                sql,
                item.getName(),
                item.getCategory(),
                item.getEraId(),
                item.getItemCondition(),
                item.getPrice(),
                item.getStatus().name(),
                item.getItemId()
        );
        return rows > 0;
    }

    public boolean updateStatus(Integer itemId, ItemStatus status) {
        String sql = "UPDATE item SET status = ? WHERE item_id = ?";
        int rows = jdbcTemplate.update(sql, status.name(), itemId);
        return rows > 0;
    }

    public boolean deleteById(Integer itemId) {
        String sql = "DELETE FROM item WHERE item_id = ?";
        int rows = jdbcTemplate.update(sql, itemId);
        return rows > 0;
    }
}
