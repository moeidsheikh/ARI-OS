package com.arindustries.repository;

import com.arindustries.entity.PriceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {
    @Query("SELECT p FROM PriceRecord p WHERE p.active = true " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:thickness IS NULL OR p.thickness = :thickness) " +
           "AND (:productType IS NULL OR p.productType = :productType) " +
           "AND (:search IS NULL OR LOWER(p.size) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(p.productType) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(p.priceCode) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<PriceRecord> findWithFilters(@Param("category") String category,
                                       @Param("thickness") String thickness,
                                       @Param("productType") String productType,
                                       @Param("search") String search,
                                       Pageable pageable);

    Optional<PriceRecord> findFirstByThicknessAndProductTypeAndSizeAndActiveTrueOrderByIdDesc(
            String thickness, String productType, String size);

    default Optional<PriceRecord> findByThicknessAndProductTypeAndSizeAndActiveTrue(
            String thickness, String productType, String size) {
        return findFirstByThicknessAndProductTypeAndSizeAndActiveTrueOrderByIdDesc(thickness, productType, size);
    }

    Optional<PriceRecord> findByPriceCode(String priceCode);
}
