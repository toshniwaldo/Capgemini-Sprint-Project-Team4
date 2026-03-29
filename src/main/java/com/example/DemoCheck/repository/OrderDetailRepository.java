package com.example.DemoCheck.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import com.example.DemoCheck.entity.OrderDetailId;
import com.example.DemoCheck.entity.OrderDetails;

@Repository
@RepositoryRestResource(path = "orderdetails")
public interface OrderDetailRepository extends JpaRepository<OrderDetails, OrderDetailId> {
    List<OrderDetails> findByProduct_ProductCode(String productCode);
}
