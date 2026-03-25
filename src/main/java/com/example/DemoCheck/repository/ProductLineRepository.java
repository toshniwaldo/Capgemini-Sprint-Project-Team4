package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.ProductLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "productlines")
public interface ProductLineRepository extends JpaRepository<ProductLine, String> {
}