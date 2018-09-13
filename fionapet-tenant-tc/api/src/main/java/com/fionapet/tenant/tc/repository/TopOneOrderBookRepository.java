package com.fionapet.tenant.tc.repository;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopOneOrderBookRepository
        extends PagingAndSortingRepository<TopOneOrderBook, Long>, JpaSpecificationExecutor<TopOneOrderBook> {

}
