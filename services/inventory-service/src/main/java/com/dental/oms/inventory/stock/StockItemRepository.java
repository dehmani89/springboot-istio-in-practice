package com.dental.oms.inventory.stock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, String> {

    /** Row lock so concurrent reservations for the same SKU serialize. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.sku = :sku")
    Optional<StockItem> findForUpdate(@Param("sku") String sku);
}
