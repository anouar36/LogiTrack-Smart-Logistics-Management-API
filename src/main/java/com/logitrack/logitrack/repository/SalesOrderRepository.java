package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    public List<SalesOrder> findAllByClient(Client client);
    @Query("SELECT so FROM SalesOrder so " +
            "LEFT JOIN FETCH so.lines sol " +
            "LEFT JOIN FETCH sol.product " +
            "WHERE so.id = :orderId")
    Optional<SalesOrder> findByIdWithLinesAndProducts(@Param("orderId") Long orderId);

}
