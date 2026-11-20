package com.antiquestore.repository;

import com.antiquestore.model.Era;
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
public class EraRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Era> eraRowMapper = (rs, rowNum) -> new Era(
            rs.getInt("era_id"),
            rs.getString("era_name"),
            rs.getInt("start_year"),
            rs.getInt("end_year")
    );

    public EraRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Era> findAll() {
        String sql = "SELECT era_id, era_name, start_year, end_year FROM era ORDER BY era_id ASC";
        return jdbcTemplate.query(sql, eraRowMapper);
    }

    public Optional<Era> findById(Integer id) {
        String sql = "SELECT era_id, era_name, start_year, end_year FROM era WHERE era_id = ?";
        try {
            Era era = jdbcTemplate.queryForObject(sql, eraRowMapper, id);
            return Optional.ofNullable(era);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Era> findByName(String name) {
        String sql = "SELECT era_id, era_name, start_year, end_year FROM era WHERE era_name = ?";
        try {
            Era era = jdbcTemplate.queryForObject(sql, eraRowMapper, name);
            return Optional.ofNullable(era);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM era WHERE era_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public Era insert(Era era) {
        String sql = "INSERT INTO era (era_name, start_year, end_year) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, era.getEraName());
            ps.setInt(2, era.getStartYear());
            ps.setInt(3, era.getEndYear());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            era.setEraId(key.intValue());
        }
        return era;
    }
}
