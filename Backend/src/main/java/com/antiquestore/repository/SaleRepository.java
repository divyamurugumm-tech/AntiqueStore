package com.antiquestore.repository;

import com.antiquestore.model.Sale;
import com.antiquestore.model.dto.SaleDetailDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class SaleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Sale> saleRowMapper = (rs, rowNum) -> new Sale(
            rs.getInt("sale_id"),
            rs.getInt("item_id"),
            rs.getString("buyer_name"),
            rs.getBigDecimal("sale_price"),
            rs.getDate("sale_date").toLocalDate()
    );

    private final RowMapper<SaleDetailDto> saleDetailRowMapper = (rs, rowNum) -> new SaleDetailDto(
            rs.getInt("sale_id"),
            rs.getInt("item_id"),
            rs.getString("item_name"),
            rs.getString("buyer_name"),
            rs.getBigDecimal("sale_price"),
            rs.getDate("sale_date").toLocalDate()
    );

    public SaleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsByItemId(Integer itemId) {
        String sql = "SELECT COUNT(*) FROM sale WHERE item_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, itemId);
        return count != null && count > 0;
    }

    public Sale insert(Sale sale) {
        String sql = "INSERT INTO sale (item_id, buyer_name, sale_price, sale_date) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, sale.getItemId());
            ps.setString(2, sale.getBuyerName());
            ps.setBigDecimal(3, sale.getSalePrice());
            ps.setDate(4, Date.valueOf(sale.getSaleDate()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            sale.setSaleId(key.intValue());
        }
        return sale;
    }

    public List<SaleDetailDto> findAllSalesWithItemName() {
        String sql = "SELECT s.sale_id, s.item_id, i.name AS item_name, s.buyer_name, s.sale_price, s.sale_date " +
                     "FROM sale s " +
                     "JOIN item i ON s.item_id = i.item_id " +
                     "ORDER BY s.sale_date DESC, s.sale_id DESC";
        return jdbcTemplate.query(sql, saleDetailRowMapper);
    }

    public BigDecimal calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(sale_price), 0.00) FROM sale";
        BigDecimal total = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return total != null ? total : BigDecimal.ZERO;
    }

    public int countSales() {
        String sql = "SELECT COUNT(*) FROM sale";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
