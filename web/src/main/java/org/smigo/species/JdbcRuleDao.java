package org.smigo.species;

import kga.Family;
import kga.Species;
import kga.rules.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcRuleDao implements RuleDao {
    private static final String SELECT = "SELECT * FROM rules LEFT JOIN families ON families.id = rules.causerfamily";
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Rule> getRules() {
        final String sql = String.format(SELECT);
        return jdbcTemplate.query(sql, new RowMapper<Rule>() {
            @Override
            public Rule mapRow(ResultSet rs, int rowNum) throws SQLException {
                int ruleId = rs.getInt("rule_id");
                int type = rs.getInt("type");
                int gap = rs.getInt("gap");
                Species causer = Species.create(rs.getInt("causer"));
                Species host = Species.create(rs.getInt("host"));
                Family family = new Family(rs.getInt("causerfamily"), rs.getString("families.name"));

                if (RuleType.goodcompanion.getId() == type)
                    return new CompanionRule(ruleId, host, RuleType.goodcompanion, causer, "hint.goodcompanion");
                else if (RuleType.badcompanion.getId() == type)
                    return new CompanionRule(ruleId, host, RuleType.badcompanion, causer, "hint.badcompanion");
                else if (RuleType.fightdisease.getId() == type)
                    return new CompanionRule(ruleId, host, RuleType.fightdisease, causer, "hint.fightdisease");
                else if (RuleType.repelpest.getId() == type)
                    return new CompanionRule(ruleId, host, RuleType.repelpest, causer, "hint.repelpest");
                else if (RuleType.improvesflavor.getId() == type)
                    return new CompanionRule(ruleId, host, RuleType.improvesflavor, causer, "hint.improvesflavor");
                else if (RuleType.goodcroprotation.getId() == type)
                    return new CropRotationRule(ruleId, host, RuleType.goodcroprotation, family, "hint.goodcroprotation");
                else if (RuleType.badcroprotation.getId() == type)
                    return new CropRotationRule(ruleId, host, RuleType.badcroprotation, family, "hint.badcroprotation");
                else if (RuleType.speciesrepetition.getId() == type)
                    return new RepetitionRule(ruleId, host, RuleType.speciesrepetition, gap, "hint.speciesrepetition");

                throw new RuntimeException("No such type of rule:" + type);
            }
        });
    }
}