package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "productlines")
public interface ProductLineRepository extends JpaRepository<ProductLine, String> {
    @RestResource(path = "by-description")
    List<ProductLine> findByTextDescriptionContainingIgnoreCase(String keyword);
}