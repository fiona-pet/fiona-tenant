package com.fionapet.tenant.tc.repository;

import com.fionapet.tenant.tc.entity.Exchange;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository
        extends PagingAndSortingRepository<Exchange, Long>, JpaSpecificationExecutor<Exchange> {

    List<Exchange> findAllBy();

}
