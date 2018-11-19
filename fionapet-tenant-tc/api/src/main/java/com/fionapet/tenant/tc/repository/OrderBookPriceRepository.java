package com.fionapet.tenant.tc.repository;

import com.fionapet.tenant.tc.entity.OrderBookPrice;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderBookPriceRepository
        extends PagingAndSortingRepository<OrderBookPrice, Long>, JpaSpecificationExecutor<OrderBookPrice> {

    List<OrderBookPrice> findByArbitrageLogId(Long arbitrageLogId);
}
