package com.atangle.shortcode.repository;

import com.atangle.shortcode.common.PiException;
import com.atangle.shortcode.entity.ShortUrlMapping;
import com.atangle.shortcode.routing.RouteTarget;
import com.atangle.shortcode.routing.SchemaRoutingExecutor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcShortUrlRepository implements ShortUrlRepository {

    private static final RowMapper<ShortUrlMapping> ROW_MAPPER = (rs, rowNum) -> {
        ShortUrlMapping mapping = new ShortUrlMapping();
        mapping.setShortCode(rs.getString("short_code").trim());
        mapping.setOriginUrl(rs.getString("origin_url"));
        mapping.setOriginUrlHash(rs.getString("origin_url_hash").trim());
        mapping.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        mapping.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
        mapping.setExpireDays((Integer) rs.getObject("expire_days"));
        mapping.setAccessCount(rs.getLong("access_count"));
        mapping.setStatus(rs.getShort("status"));
        mapping.setCreator(rs.getString("creator"));
        return mapping;
    };

    private final SchemaRoutingExecutor schemaRoutingExecutor;
    private final JdbcTemplate jdbcTemplate;

    public JdbcShortUrlRepository(SchemaRoutingExecutor schemaRoutingExecutor, JdbcTemplate jdbcTemplate) {
        this.schemaRoutingExecutor = schemaRoutingExecutor;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(RouteTarget routeTarget, ShortUrlMapping shortUrlMapping) {
        String sql = """
                INSERT INTO %s
                (short_code, origin_url, origin_url_hash, create_time, update_time, expire_days, access_count, status, creator)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.formatted(routeTarget.tableName());

        try {
            schemaRoutingExecutor.executeOn(routeTarget, ignored -> {
                jdbcTemplate.update(connection -> {
                    var ps = connection.prepareStatement(sql);
                    ps.setString(1, shortUrlMapping.getShortCode());
                    ps.setString(2, shortUrlMapping.getOriginUrl());
                    ps.setString(3, shortUrlMapping.getOriginUrlHash());
                    ps.setTimestamp(4, Timestamp.valueOf(shortUrlMapping.getCreateTime()));
                    ps.setTimestamp(5, Timestamp.valueOf(shortUrlMapping.getUpdateTime()));
                    ps.setObject(6, shortUrlMapping.getExpireDays());
                    ps.setLong(7, shortUrlMapping.getAccessCount());
                    ps.setShort(8, shortUrlMapping.getStatus());
                    ps.setString(9, shortUrlMapping.getCreator());
                    return ps;
                });
                return null;
            });
        } catch (DuplicateKeyException ex) {
            throw new PiException(4002, "短链已存在", ex);
        }
    }

    @Override
    public Optional<ShortUrlMapping> findByShortCode(RouteTarget routeTarget, String shortCode) {
        String sql = "SELECT * FROM %s WHERE short_code = ?".formatted(routeTarget.tableName());
        return schemaRoutingExecutor.executeOn(routeTarget, ignored -> {
            List<ShortUrlMapping> results = jdbcTemplate.query(connection -> {
                var ps = connection.prepareStatement(sql);
                ps.setString(1, shortCode);
                return ps;
            }, ROW_MAPPER);
            return results.stream().findFirst();
        });
    }

    @Override
    public void increaseAccessCount(RouteTarget routeTarget, String shortCode) {
        String sql = "UPDATE %s SET access_count = access_count + 1 WHERE short_code = ?".formatted(routeTarget.tableName());
        schemaRoutingExecutor.executeOn(routeTarget, ignored -> {
            jdbcTemplate.update(connection -> {
                var ps = connection.prepareStatement(sql);
                ps.setString(1, shortCode);
                return ps;
            });
            return null;
        });
    }
}
