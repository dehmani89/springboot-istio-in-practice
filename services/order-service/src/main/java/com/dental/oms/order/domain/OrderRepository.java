package com.dental.oms.order.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = "lines")
    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "lines")
    List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
