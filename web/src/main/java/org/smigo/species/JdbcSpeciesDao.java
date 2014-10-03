package org.smigo.species;

import kga.Family;
import org.smigo.SpeciesView;
import org.smigo.config.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
class JdbcSpeciesDao implements SpeciesDao {
    private static final String SELECT = "SELECT * FROM species WHERE species_id NOT IN (176,177,178,179,181,190,193,194,195,199,200,201)";
    private static final String WHERE = SELECT + " WHERE species_id = ?";
    private static final String DEFAULTICONNAME = "defaulticon.png";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert create;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.create = new SimpleJdbcInsert(dataSource).withTableName("species").usingGeneratedKeyColumns("id");
    }

    @Override
    @CacheEvict(value = Cache.SPECIES, allEntries = true)
    public int addSpecies(SpeciesFormBean species, int userId) {
        MapSqlParameterSource s = new MapSqlParameterSource();
        s.addValue("name", species.getScientificName(), Types.VARCHAR);
        s.addValue("item", species.isItem(), Types.BOOLEAN);
        s.addValue("annual", species.isAnnual(), Types.BOOLEAN);
        s.addValue("family", species.getFamily(), Types.INTEGER);
        s.addValue("creator", userId, Types.INTEGER);
        s.addValue("vernacularname", species.getVernacularName());
        Number update = create.executeAndReturnKey(s);
        return update.intValue();
    }

    @Override
    @Cacheable(Cache.SPECIES)
    public List<SpeciesView> getSpecies(final Map<Integer, Family> familyMap) {
        return jdbcTemplate.query(SELECT, new Object[]{}, new SpeciesViewRowMapper(familyMap));
    }

    @Override
    public SpeciesView getSpecies(int id) {
        return jdbcTemplate.queryForObject(WHERE, new Object[]{id}, new SpeciesViewRowMapper(new HashMap<Integer, Family>()));
    }

    private static class SpeciesViewRowMapper implements RowMapper<SpeciesView> {
        private final Map<Integer, Family> familyMap;

        public SpeciesViewRowMapper(Map<Integer, Family> familyMap) {
            this.familyMap = familyMap;
        }

        @Override
        public SpeciesView mapRow(ResultSet rs, int rowNum) throws SQLException {
            SpeciesView ret = new SpeciesView();
            ret.setId(rs.getInt("species_id"));
            ret.setScientificName(rs.getString("name"));
            ret.setItem(rs.getBoolean("item"));
            ret.setAnnual(rs.getBoolean("annual"));
            ret.setFamily(familyMap.get(rs.getInt("family")));
            String iconfilename = rs.getString("iconfilename");
            ret.setIconFileName(iconfilename == null ? DEFAULTICONNAME : iconfilename);
            ret.setVernacularName(rs.getString("vernacularName"));
            return ret;
        }
    }
}