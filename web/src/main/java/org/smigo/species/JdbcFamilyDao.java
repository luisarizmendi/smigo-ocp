package org.smigo.species;

import com.fasterxml.jackson.databind.ObjectMapper;
import kga.Family;
import org.smigo.config.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
class JdbcFamilyDao implements FamilyDao {
    private static final String SELECT = "SELECT * FROM families";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @Cacheable(Cache.FAMILIES)
    public List<Family> getFamilies() {
        final String sql = String.format(SELECT);
        return jdbcTemplate.query(sql, new RowMapper<Family>() {
            @Override
            public Family mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Family(rs.getInt("id"), rs.getString("name"));
            }
        });
    }
}