package com.antiquestore.repository;

import com.antiquestore.model.Valuation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class ValuationRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Valuation> valuationRowMapper = (rs, rowNum) -> new Valuation(
            rs.getInt("val_id"),
            rs.getInt("item_id"),
            rs.getBigDecimal("appraised_value"),
            rs.getDate("val_date").toLocalDate(),
            rs.getString("appraiser")
    );

    public ValuationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Valuation> findByItemId(Integer itemId) {
        String sql = "SELECT val_id, item_id, appraised_value, val_date, appraiser FROM valuation WHERE item_id = ? ORDER BY val_date DESC, val_id DESC";
        return jdbcTemplate.query(sql, valuationRowMapper, itemId);
    }

    public Valuation insert(Valuation valuation) {
        String sql = "INSERT INTO valuation (item_id, appraised_value, val_date, appraiser) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, valuation.getItemId());
            ps.setBigDecimal(2, valuation.getAppraisedValue());
            ps.setDate(3, Date.valueOf(valuation.getValDate()));
            ps.setString(4, valuation.getAppraiser());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            valuation.setValId(key.intValue());
        }
        return valuation;
    }
}
