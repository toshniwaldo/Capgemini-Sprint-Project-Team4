    package com.example.DemoCheck.repository;

    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.repository.query.Param;
    import org.springframework.data.rest.core.annotation.RepositoryRestResource;
    import org.springframework.data.rest.core.annotation.RestResource;
    import org.springframework.validation.annotation.Validated;

    import com.example.DemoCheck.entity.Product;
    import com.example.DemoCheck.projection.ProductProjection;

    @Validated
    @RepositoryRestResource(path = "products")
    // @RepositoryRestResource(path = "products", excerptProjection = ProductProjection.class)
    public interface ProductRepository extends JpaRepository<Product, String> {
        @RestResource(path = "searchByNameOrLine")
        Page<Product> findByProductNameContainingIgnoreCaseOrProductLine_ProductLineContainingIgnoreCase(
            @Param("name") String name,
            @Param("line") String line,
            Pageable pageable
        );
    }
