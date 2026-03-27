package com.example.DemoCheck.repository;

import com.example.DemoCheck.entity.Office;
import com.example.DemoCheck.projection.OfficeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "offices",collectionResourceRel = "offices")
public interface OfficeRepository extends JpaRepository<Office, String> {

    @RestResource(path = "by-cities")
    List<Office> findByCityIn(@Param("cities") List<String> cities);
}