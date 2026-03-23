package com.example.DogBazzar.Dog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DogRepository extends JpaRepository<DogEntity,Long> {

    @Query("""
    SELECT d FROM DogEntity d
    LEFT JOIN d.listing l
    WHERE (:breed IS NULL OR d.breed = :breed)
    AND (:rarity IS NULL OR d.rarity = :rarity)
    AND (:minPrice IS NULL OR d.price >= :minPrice)
    AND (:maxPrice IS NULL OR d.price <= :maxPrice)
    AND (:hasOwner IS NULL OR
         (:hasOwner = true AND d.user IS NOT NULL) OR
         (:hasOwner = false AND d.user IS NULL))
    AND (:forSale IS NULL OR
         (:forSale = true AND l.status = 'ACTIVE') OR
         (:forSale = false AND (l IS NULL OR l.status != 'ACTIVE')))
""")
        List<DogEntity> searchAllByFilter(
                @Param("breed") String breed,
                @Param("rarity") Rarity rarity,
                @Param("minPrice")BigDecimal minPrice,
                @Param("maxPrice")BigDecimal maxPrice,
                @Param("hasOwner") Boolean hasOwner,
                @Param("forSale") Boolean forSale,
                Pageable pageable
                );

    List<DogEntity> findAllByUserEmail(String email);

    List<DogEntity> findByUserId(Long userId);
}
