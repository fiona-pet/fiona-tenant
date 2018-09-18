package com.fionapet.tenant.tc.repository;

import com.fionapet.tenant.tc.entity.ArbitrageLog;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArbitrageLogRepository
        extends PagingAndSortingRepository<ArbitrageLog, Long>, JpaSpecificationExecutor<ArbitrageLog> {

    void deleteByExchangeIdAndArbitrageLessThan(Long exchangeId, float arbitrage);
}
