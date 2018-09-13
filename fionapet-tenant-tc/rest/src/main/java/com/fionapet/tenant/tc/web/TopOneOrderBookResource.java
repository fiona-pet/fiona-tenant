package com.fionapet.tenant.tc.web;

import com.fionapet.tenant.tc.entity.TopOneOrderBook;
import com.fionapet.tenant.tc.service.TopOneOrderBookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/topOneOrderBooks")
public class TopOneOrderBookResource {

    private static final Logger log = LoggerFactory.getLogger(TopOneOrderBookResource.class);

    @Autowired
    private TopOneOrderBookService service;

    @GetMapping("/{id}")
    public ResponseEntity<TopOneOrderBook> findById(@PathVariable Long id) {
    	log.debug("HTTP Get id={}", id);
        Optional<TopOneOrderBook> demo = service.findById(id);
        return demo.map(ResponseEntity::ok).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
