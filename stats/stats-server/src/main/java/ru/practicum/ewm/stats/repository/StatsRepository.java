package ru.practicum.ewm.stats.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.dto.stats.ViewStatsRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StatsRepository {
    private final JdbcTemplate jdbcTemplate;

    public StatsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveHit(EndpointHit endpointHit) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("stats")
                .usingGeneratedKeyColumns("id");

        Long id = simpleJdbcInsert.executeAndReturnKey(toMapEndpointHit(endpointHit)).longValue();
        log.debug("Добавлен EndpointHit c id {}", id);
    }

    private Map<String, Object> toMapEndpointHit(EndpointHit endpointHit) {
        Map<String, Object> values = new HashMap<>();
        values.put("app", endpointHit.getApp());
        values.put("uri", endpointHit.getUri());
        values.put("ip", endpointHit.getIp());
        values.put("created", endpointHit.getTimestamp());
        return values;
    }

    public List<ViewStats> getIntervalStats(ViewStatsRequest vSR) {
        if (vSR.getUris() == null || vSR.getUris().isEmpty()) {
            if (vSR.isUnique()) {
                String sqlViewStats = "select s.app, s.uri, count(distinct s.ip) as hits " +
                        "from stats as s " +
                        "where s.created between ? and ? " +
                        "group by s.app, s.uri " +
                        "order by count(distinct s.ip) desc";
                return jdbcTemplate.query(sqlViewStats, (rs, rowNum) -> makeViewStats(rs), vSR.getStart(), vSR.getEnd());
            } else {
                String sqlViewStats = "select s.app, s.uri, count(s.ip) as hits " +
                        "from stats as s " +
                        "where s.created between ? and ? " +
                        "group by s.app, s.uri " +
                        "order by count(s.ip) desc";
                return jdbcTemplate.query(sqlViewStats, (rs, rowNum) -> makeViewStats(rs), vSR.getStart(), vSR.getEnd());
            }
        } else {

            String allUris = listToString(vSR.getUris());

            if (vSR.isUnique()) {
                String sqlViewStats = "select s.app, s.uri, count(distinct s.ip) as hits " +
                        "from stats as s " +
                        "where s.created between ? and ? and (?) like '%' || s.uri || ',%' " +
                        "group by s.app, s.uri " +
                        "order by count(distinct s.ip) desc";
                return jdbcTemplate.query(sqlViewStats, (rs, rowNum) -> makeViewStats(rs), vSR.getStart(), vSR.getEnd(), allUris);
            } else {
                String sqlViewStats = "select s.app, s.uri, count(s.ip) as hits " +
                        "from stats as s " +
                        "where s.created between ? and ? and (?) like '%' || s.uri || ',%' " +
                        "group by s.app, s.uri " +
                        "order by count(s.ip) desc";
                return jdbcTemplate.query(sqlViewStats, (rs, rowNum) -> makeViewStats(rs), vSR.getStart(), vSR.getEnd(), allUris);
            }
        }
    }

    private ViewStats makeViewStats(ResultSet rs) throws SQLException {
        String app = rs.getString("app");
        String uri = rs.getString("uri");
        Long hits = rs.getLong("hits");
        return new ViewStats(app, uri, hits);
    }

    private String listToString(List<String> stringList) {
        StringBuilder allUris = new StringBuilder();
        for (String currentUri : stringList) {
            allUris.append(currentUri).append(",");
        }
        return allUris.toString();
    }
}
