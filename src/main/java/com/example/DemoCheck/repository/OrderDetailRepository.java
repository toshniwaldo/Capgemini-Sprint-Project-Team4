package com.example.DemoCheck.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import com.example.DemoCheck.entity.OrderDetailId;
import com.example.DemoCheck.entity.OrderDetails;

@Repository
@RepositoryRestResource(path = "orderdetails")
public interface OrderDetailRepository extends JpaRepository<OrderDetails, OrderDetailId> {
    // @RestResource(path = "searchByProductCode")
    Page<OrderDetails> findByProduct_ProductCode(@Param("productCode") String productCode, Pageable pageable);
}