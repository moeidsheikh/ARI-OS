package com.arindustries.repository;

import com.arindustries.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByItemCode(String itemCode);

    @Query("SELECT i FROM Inventory i WHERE i.active = true AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:search IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "OR LOWER(i.itemCode) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Inventory> findWithFilters(@Param("category") String category,
                                     @Param("search") String search,
                                     Pageable pageable);

    @Query("SELECT i FROM Inventory i WHERE i.active = true AND i.currentStock <= i.minimumStock")
    List<Inventory> findLowStockItems();

    Optional<Inventory> findFirstByThicknessAndProductTypeAndSizeAndActiveTrueOrderByIdDesc(
            String thickness, String productType, String size);

    default Optional<Inventory> findByThicknessAndProductTypeAndSizeAndActiveTrue(
            String thickness, String productType, String size) {
        return findFirstByThicknessAndProductTypeAndSizeAndActiveTrueOrderByIdDesc(thickness, productType, size);
    }
}
