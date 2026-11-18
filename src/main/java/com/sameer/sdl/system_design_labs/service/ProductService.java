package com.sameer.sdl.system_design_labs.service;

import com.sameer.sdl.system_design_labs.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final JdbcTemplate jdbc;

    public ProductService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Cacheable(cacheNames = "products", sync = true)
    public Product findById(long id) {
        log.info("findById: Cache miss for product {}. Querying PostgreSQL.", id);
        return jdbc.query(
                "SELECT id, name FROM products WHERE id = ?",
                (rs, rowNum) -> new Product(rs.getLong("id"), rs.getString("name")),
                id
        ).stream().findFirst().orElse(null);
    }

    @CachePut(cacheNames = "products", key = "#p0", unless = "#result == null")
    public Optional<Product> updateName(long id, String name) {
        int updatedRows = jdbc.update(
                "UPDATE products SET name = ? WHERE id = ?",
                name, id
        );

        if (updatedRows == 0) {
            return Optional.empty();
        }

        log.info("updateName: Updated product {} in PostgreSQL. Will refresh its Redis cache entry.", id);

        return Optional.of(new Product(id, name));
    }
}
