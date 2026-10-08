package com.dental.oms.pricing.price;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListEntryRepository extends JpaRepository<PriceListEntry, String> {
}
