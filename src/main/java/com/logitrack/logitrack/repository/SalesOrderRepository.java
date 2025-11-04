package com.logitrack.logitrack.repository;

import com.logitrack.logitrack.entity.Client;
import com.logitrack.logitrack.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    public List<SalesOrder> findAllByClient(Client client);

}
