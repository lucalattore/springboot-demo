package com.waveinformatica.demo.services;

import com.waveinformatica.demo.dto.MarketDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Primary
@Slf4j
public class DBMarketService implements MarketService {

    @Autowired
    private DataSource ds;

    @Override
    public Collection<MarketDTO> listMarkets() {
        return findMarkets(null);
    }

    @Override
    public Collection<MarketDTO> findMarkets(String prefix) {
        final List<MarketDTO> markets = new ArrayList<>();
        try (Connection conn = ds.getConnection()) {
            String sql = "select code,name from markets" + (prefix == null ? "" : " where name like ?");
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (prefix != null) {
                    stmt.setString(1, prefix + "%");
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        MarketDTO m = new MarketDTO();
                        m.setId(rs.getLong("code"));
                        m.setName(rs.getString("name"));
                        markets.add(m);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return markets;
    }

    @Override
    public MarketDTO getMarket(long id) {
        try (Connection conn = ds.getConnection()) {
            String sql = "select code,name from markets where code = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        MarketDTO m = new MarketDTO();
                        m.setId(rs.getLong("code"));
                        m.setName(rs.getString("name"));
                        return m;
                    }

                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean addMarket(MarketDTO m) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "insert into markets (code,name) values (?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, m.getId());
                    stmt.setString(2, m.getName());
                    stmt.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLIntegrityConstraintViolationException e) {
                log.error("Got error inserting market {}: {}", m.getId(), e.getMessage());
                conn.rollback();
                return false;
            } catch (Throwable e) {
                log.error("Got error inserting market {}: {}", m.getId(), e.getMessage());
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteMarket(long id) {
        log.debug("Deleting market {}", id);
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String sql = "delete from markets where code = ?";
                boolean deleted;
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, id);
                    deleted = stmt.executeUpdate() != 0;
                }

                conn.commit();
                return deleted;
            } catch (Throwable e) {
                log.error("Got error deleting market {}: {}", id, e.getMessage());
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}