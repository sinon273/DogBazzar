package com.example.DogBazzar.Listing;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListingRepository extends JpaRepository<ListingEntity,Long> {

    @Query("""
              select l from ListingEntity l
              where (:status is null or l.status = :status)
              and (:date is null or l.createdAt >= :date)
              and (:minPrice is null or l.price >= :minPrice)
              and (:maxPrice is null or l.price <= :maxPrice)
""")
    public List<ListingEntity> searchAllByFilter(
            @Param("status")ListingStatus status,
            @Param("date")LocalDateTime date,
            @Param("maxPrice")BigDecimal maxPrice,
            @Param("minPrice")BigDecimal minPrice,
            Pageable pageable
            );

    List<ListingEntity> findBySellerId(Long id);
}
